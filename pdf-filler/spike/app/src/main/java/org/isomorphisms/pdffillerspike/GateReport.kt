package org.isomorphisms.pdffillerspike

import android.content.Context
import android.os.Build
import android.os.ext.SdkExtensions
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

enum class Gate {
    OPEN,
    FORM,
    SAVE,
    FLAT_TEXT,
    CHECK,
}

enum class Outcome {
    PASS,
    FAIL,
    SKIP,
}

data class Receipt(
    val outcome: Outcome,
    val reason: String,
    val evidence: String = "",
)

class GateReport(private val context: Context) {
    private val preferences = context.getSharedPreferences("gate-report", Context.MODE_PRIVATE)
    private val receipts =
        Gate.entries.associateWith { gate ->
            Receipt(
                Outcome.valueOf(preferences.getString("${gate.name}.outcome", "SKIP")!!),
                preferences.getString("${gate.name}.reason", "not run on this device")!!,
                preferences.getString("${gate.name}.evidence", "")!!,
            )
        }.toMutableMap()

    val extensionLevel: Int = sdkExtensionLevel()
    val editingAvailable: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && extensionLevel >= REQUIRED_EXTENSION

    fun record(gate: Gate, outcome: Outcome, reason: String, evidence: String = "") {
        receipts[gate] = Receipt(outcome, reason, evidence)
        preferences.edit()
            .putString("${gate.name}.outcome", outcome.name)
            .putString("${gate.name}.reason", reason)
            .putString("${gate.name}.evidence", evidence)
            .apply()
    }

    fun receipt(gate: Gate): Receipt = checkNotNull(receipts[gate])

    fun markEditingUnavailable() {
        val reason =
            "EditablePdfViewerFragment requires Android 12+ and SDK extension $REQUIRED_EXTENSION; " +
                "this device reports API ${Build.VERSION.SDK_INT} and extension $extensionLevel"
        listOf(Gate.FORM, Gate.SAVE, Gate.FLAT_TEXT, Gate.CHECK).forEach {
            record(it, Outcome.SKIP, reason)
        }
    }

    fun asJson(): String {
        val gateJson = JSONObject()
        Gate.entries.forEach { gate ->
            val receipt = receipt(gate)
            gateJson.put(
                gate.name,
                JSONObject()
                    .put("outcome", receipt.outcome.name)
                    .put("reason", receipt.reason)
                    .put("evidence", receipt.evidence),
            )
        }

        return JSONObject()
            .put("schema", "pdf-filler-androidx-engine-gate/v1")
            .put("recorded_at_utc", Instant.now().toString())
            .put(
                "device",
                JSONObject()
                    .put("manufacturer", Build.MANUFACTURER)
                    .put("model", Build.MODEL)
                    .put("android_release", Build.VERSION.RELEASE)
                    .put("api", Build.VERSION.SDK_INT)
                    .put("sdk_extension_s", extensionLevel)
                    .put("abis", JSONArray(Build.SUPPORTED_ABIS.toList())),
            )
            .put("engine", "androidx.pdf:1.0.0-beta01")
            .put("editable_fragment_available", editingAvailable)
            .put("gates", gateJson)
            .put(
                "meaning",
                "PASS means only the evidence stated for that gate. FLAT_TEXT is glyph outlines " +
                    "inside a stamp annotation, not searchable or re-editable PDF text.",
            )
            .toString(2)
    }

    @Suppress("NewApi")
    private fun sdkExtensionLevel(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S)
        } else {
            0
        }

    companion object {
        const val REQUIRED_EXTENSION = 18
    }
}
