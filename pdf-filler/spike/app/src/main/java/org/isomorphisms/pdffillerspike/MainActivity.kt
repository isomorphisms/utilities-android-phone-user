package org.isomorphisms.pdffillerspike

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import androidx.pdf.EditablePdfDocument
import androidx.pdf.ExperimentalPdfApi
import androidx.pdf.MutableEditsDraft
import androidx.pdf.PdfDocument
import androidx.pdf.annotation.content.StampAnnotation
import androidx.pdf.models.FormEditInfo
import androidx.pdf.models.FormWidgetInfo
import java.io.File
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@OptIn(ExperimentalPdfApi::class)
class MainActivity : AppCompatActivity(), GateFragmentHost {
    private lateinit var report: GateReport
    private lateinit var reportView: TextView
    private lateinit var compatibilityView: TextView
    private var runState: RunState = RunState.IDLE
    private var busy = false

    private val resultsDirectory: File by lazy {
        File(filesDir, "results").apply { mkdirs() }
    }
    private val formResult: File by lazy { File(resultsDirectory, "form-result.pdf") }
    private val flatResult: File by lazy { File(resultsDirectory, "flat-result.pdf") }

    private val exportReceipt =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                contentResolver.openOutputStream(uri, "wt")?.use {
                    it.write(report.asJson().toByteArray())
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = GateReport(this)
        runState =
            savedInstanceState?.getString(STATE_RUN)?.let(RunState::valueOf) ?: RunState.IDLE
        setContentView(createContentView())
        installViewer()
        if (!report.editingAvailable) report.markEditingUnavailable()
        refreshReport()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_RUN, runState.name)
        super.onSaveInstanceState(outState)
    }

    private fun createContentView(): View {
        val root =
            LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(12)
            }

        compatibilityView =
            TextView(this).apply {
                textSize = 13f
                setPadding(6)
            }
        root.addView(compatibilityView)

        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        controls.addView(button("Run FORM") { runFormGate() })
        controls.addView(button("Run FLAT_TEXT + CHECK") { runFlatGates() })
        controls.addView(button("View form result") { viewExternally(formResult) })
        controls.addView(button("Record SAVE") { recordExternal(Gate.SAVE) })
        controls.addView(button("View flat result") { viewExternally(flatResult) })
        controls.addView(button("Record FLAT_TEXT") { recordExternal(Gate.FLAT_TEXT) })
        controls.addView(button("Record CHECK") { recordExternal(Gate.CHECK) })
        controls.addView(button("Export receipt") { exportReceipt.launch("androidx-pdf-gate.json") })
        root.addView(
            HorizontalScrollView(this).apply { addView(controls) },
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        reportView =
            TextView(this).apply {
                textSize = 10f
                setTextIsSelectable(true)
                setPadding(6)
            }
        root.addView(
            reportView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                resources.displayMetrics.density.times(190).toInt(),
            ),
        )

        root.addView(
            FrameLayout(this).apply { id = VIEWER_ID },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f),
        )
        return root
    }

    private fun button(label: String, action: () -> Unit): Button =
        Button(this).apply {
            text = label
            isAllCaps = false
            setOnClickListener { action() }
        }

    private fun installViewer() {
        if (supportFragmentManager.findFragmentByTag(VIEWER_TAG) != null) return
        val fragment =
            if (report.editingAvailable) EditableGateFragment() else ReadOnlyGateFragment()
        supportFragmentManager.beginTransaction()
            .replace(VIEWER_ID, fragment, VIEWER_TAG)
            .commitNow()
    }

    private fun runFormGate() {
        busy = false
        report.record(Gate.OPEN, Outcome.SKIP, "form fixture not loaded yet")
        report.record(Gate.FORM, Outcome.SKIP, "form fixture has not completed")
        report.record(Gate.SAVE, Outcome.SKIP, "independent viewer confirmation not recorded")
        if (!report.editingAvailable) report.markEditingUnavailable()
        runState = if (report.editingAvailable) RunState.FORM_SOURCE else RunState.OPEN_ONLY
        openInViewer(Uri.fromFile(PdfFixtures.form(this)))
        refreshReport()
    }

    private fun runFlatGates() {
        busy = false
        report.record(Gate.OPEN, Outcome.SKIP, "flat fixture not loaded yet")
        if (!report.editingAvailable) {
            report.markEditingUnavailable()
            runState = RunState.OPEN_ONLY
        } else {
            report.record(Gate.FLAT_TEXT, Outcome.SKIP, "flat fixture has not completed")
            report.record(Gate.CHECK, Outcome.SKIP, "flat fixture has not completed")
            runState = RunState.FLAT_SOURCE
        }
        openInViewer(Uri.fromFile(PdfFixtures.flat(this)))
        refreshReport()
    }

    private fun openInViewer(uri: Uri) {
        when (val fragment = supportFragmentManager.findFragmentByTag(VIEWER_TAG)) {
            is EditableGateFragment -> fragment.documentUri = uri
            is ReadOnlyGateFragment -> fragment.documentUri = uri
            else -> error("PDF viewer fragment is missing")
        }
    }

    override fun onDocumentLoaded(document: PdfDocument) {
        report.record(
            Gate.OPEN,
            Outcome.PASS,
            "AndroidX completed document loading and rendering",
            "uri=${document.uri}; pages=${document.pageCount}",
        )
        refreshReport()
        if (busy) return
        busy = true
        when (runState) {
            RunState.FORM_SOURCE -> lifecycleScope.launch { applyFormAndSave(document) }
            RunState.FORM_RESULT -> lifecycleScope.launch { verifyFormResult(document) }
            RunState.FLAT_SOURCE -> lifecycleScope.launch { applyFlatMarksAndSave(document) }
            RunState.FLAT_RESULT -> lifecycleScope.launch { verifyFlatResult(document) }
            RunState.IDLE, RunState.OPEN_ONLY -> busy = false
        }
    }

    override fun onDocumentLoadFailed(error: Throwable) {
        busy = false
        report.record(
            Gate.OPEN,
            Outcome.FAIL,
            "AndroidX document load failed",
            errorEvidence(error),
        )
        when (runState) {
            RunState.FORM_SOURCE, RunState.FORM_RESULT -> {
                report.record(Gate.FORM, Outcome.FAIL, "form fixture could not be processed")
                report.record(Gate.SAVE, Outcome.FAIL, "saved form could not be reopened")
            }
            RunState.FLAT_SOURCE, RunState.FLAT_RESULT -> {
                report.record(Gate.FLAT_TEXT, Outcome.FAIL, "flat fixture could not be processed")
                report.record(Gate.CHECK, Outcome.FAIL, "flat fixture could not be processed")
            }
            else -> Unit
        }
        refreshReport()
    }

    private suspend fun applyFormAndSave(document: PdfDocument) {
        try {
            val editable = requireEditable(document)
            val fields =
                editable.getFormWidgetInfos(0).filter {
                    it.widgetType == FormWidgetInfo.WIDGET_TYPE_TEXTFIELD
                }
            val field = fields.firstOrNull { !it.isReadOnly }
                ?: error("generated AcroForm contains no writable text field")
            editable.applyEdit(FormEditInfo.createSetText(0, field.widgetIndex, FORM_VALUE))
            val applied = editable.getFormWidgetInfos(0).firstOrNull {
                it.widgetIndex == field.widgetIndex && it.textValue == FORM_VALUE
            }
            check(applied != null) { "field value did not change in the editable document" }
            writeDocument(editable, formResult)
            runState = RunState.FORM_RESULT
            busy = false
            openInViewer(Uri.fromFile(formResult))
        } catch (error: Throwable) {
            busy = false
            report.record(Gate.FORM, Outcome.FAIL, "AcroForm enumeration or fill failed", errorEvidence(error))
            report.record(Gate.SAVE, Outcome.FAIL, "form result was not written", errorEvidence(error))
            refreshReport()
        }
    }

    private suspend fun verifyFormResult(document: PdfDocument) {
        try {
            val fields = document.getFormWidgetInfos(0)
            check(fields.any { it.textValue == FORM_VALUE }) {
                "saved AcroForm reopened without the marker value"
            }
            report.record(
                Gate.FORM,
                Outcome.PASS,
                "enumerated a writable AcroForm text field, set it, saved it, and read the same value after reopen",
                "value=$FORM_VALUE; widgets=${fields.size}",
            )
            report.record(
                Gate.SAVE,
                Outcome.SKIP,
                "AndroidX reopened the saved file with the marker, but an independent viewer has not been confirmed",
                "internal_reopen=PASS; file=${formResult.name}",
            )
        } catch (error: Throwable) {
            report.record(Gate.FORM, Outcome.FAIL, "saved form value verification failed", errorEvidence(error))
            report.record(Gate.SAVE, Outcome.FAIL, "saved form did not reopen with its marker", errorEvidence(error))
        } finally {
            runState = RunState.IDLE
            busy = false
            refreshReport()
        }
    }

    private suspend fun applyFlatMarksAndSave(document: PdfDocument) {
        try {
            val editable = requireEditable(document)
            check(document.formType == PdfDocument.PDF_FORM_TYPE_NONE) {
                "generated flat fixture unexpectedly reports a form"
            }
            val draft = MutableEditsDraft().apply {
                insert(VectorMarks.outlinedText(FLAT_VALUE))
                insert(VectorMarks.check())
            }
            val editIds = editable.applyEdits(draft.toEditsDraft())
            check(editIds.size == 2) { "AndroidX applied ${editIds.size} of 2 annotations" }
            writeDocument(editable, flatResult)
            runState = RunState.FLAT_RESULT
            busy = false
            openInViewer(Uri.fromFile(flatResult))
        } catch (error: Throwable) {
            busy = false
            report.record(Gate.FLAT_TEXT, Outcome.FAIL, "outlined text stamp failed", errorEvidence(error))
            report.record(Gate.CHECK, Outcome.FAIL, "X stamp failed", errorEvidence(error))
            refreshReport()
        }
    }

    private suspend fun verifyFlatResult(document: PdfDocument) {
        try {
            val stamps = document.getAnnotationsForPage(0).mapNotNull { it.annotation as? StampAnnotation }
            val textFound = stamps.any { VectorMarks.matches(it.bounds, VectorMarks.textBounds) }
            val checkFound = stamps.any { VectorMarks.matches(it.bounds, VectorMarks.checkBounds) }
            check(textFound) { "saved outlined text stamp was not found after reopen" }
            check(checkFound) { "saved X stamp was not found after reopen" }
            report.record(
                Gate.FLAT_TEXT,
                Outcome.SKIP,
                "AndroidX reopened the outlined-glyph stamp; independent viewer confirmation is still required",
                "internal_reopen=PASS; representation=vector glyph outlines in StampAnnotation",
            )
            report.record(
                Gate.CHECK,
                Outcome.SKIP,
                "AndroidX reopened the X stamp; independent viewer confirmation is still required",
                "internal_reopen=PASS; representation=vector paths in StampAnnotation",
            )
        } catch (error: Throwable) {
            report.record(Gate.FLAT_TEXT, Outcome.FAIL, "saved outlined text verification failed", errorEvidence(error))
            report.record(Gate.CHECK, Outcome.FAIL, "saved X verification failed", errorEvidence(error))
        } finally {
            runState = RunState.IDLE
            busy = false
            refreshReport()
        }
    }

    private fun requireEditable(document: PdfDocument): EditablePdfDocument =
        document as? EditablePdfDocument
            ?: error("AndroidX loaded a read-only PdfDocument on an edit-compatible device")

    private suspend fun writeDocument(document: EditablePdfDocument, output: File) {
        output.parentFile?.mkdirs()
        val mode =
            ParcelFileDescriptor.MODE_CREATE or
                ParcelFileDescriptor.MODE_TRUNCATE or
                ParcelFileDescriptor.MODE_READ_WRITE
        document.createWriteHandle().use { handle ->
            ParcelFileDescriptor.open(output, mode).use { descriptor ->
                handle.writeTo(descriptor)
            }
        }
    }

    private fun viewExternally(file: File) {
        if (!file.isFile) {
            Toast.makeText(this, "Run that gate first", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
        val intent =
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        try {
            startActivity(Intent.createChooser(intent, "Independent PDF viewer"))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "No other PDF viewer is installed", Toast.LENGTH_LONG).show()
        }
    }

    private fun recordExternal(gate: Gate) {
        val file = if (gate == Gate.SAVE) formResult else flatResult
        if (!file.isFile) {
            Toast.makeText(this, "Run that gate and view its result first", Toast.LENGTH_SHORT).show()
            return
        }
        val question =
            when (gate) {
                Gate.SAVE -> "Did another PDF viewer show ANDROIDX_FORM_GATE in the form field?"
                Gate.FLAT_TEXT -> "Did another PDF viewer show the outlined ANDROIDX FLAT TEXT?"
                Gate.CHECK -> "Did another PDF viewer show the X?"
                else -> error("no external confirmation for $gate")
            }
        AlertDialog.Builder(this)
            .setTitle(gate.name)
            .setMessage(question)
            .setPositiveButton("PASS") { _, _ ->
                report.record(
                    gate,
                    Outcome.PASS,
                    "independent PDF viewer displayed the saved result; manually confirmed",
                    "manual_confirmation=true; file=${file.name}",
                )
                refreshReport()
            }
            .setNegativeButton("FAIL") { _, _ ->
                report.record(
                    gate,
                    Outcome.FAIL,
                    "independent PDF viewer did not display the saved result; manually confirmed",
                    "manual_confirmation=true; file=${file.name}",
                )
                refreshReport()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun refreshReport() {
        compatibilityView.text =
            "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), " +
                "S extension ${report.extensionLevel}; editable=${report.editingAvailable}"
        reportView.text = report.asJson()
    }

    private fun errorEvidence(error: Throwable): String =
        "${error::class.java.name}: ${error.message ?: "no message"}"

    private enum class RunState {
        IDLE,
        OPEN_ONLY,
        FORM_SOURCE,
        FORM_RESULT,
        FLAT_SOURCE,
        FLAT_RESULT,
    }

    companion object {
        private const val VIEWER_TAG = "gate-pdf-viewer"
        private const val VIEWER_ID = 0x50444601
        private const val STATE_RUN = "run-state"
        private const val FORM_VALUE = "ANDROIDX_FORM_GATE"
        private const val FLAT_VALUE = "ANDROIDX FLAT TEXT"
    }
}
