package org.isomorphisms.pdffillerspike

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.pdf.ExperimentalPdfApi
import androidx.pdf.PdfDocument
import androidx.pdf.ink.EditablePdfViewerFragment
import androidx.pdf.view.PdfView
import androidx.pdf.viewer.fragment.PdfViewerFragment

interface GateFragmentHost {
    fun onDocumentLoaded(document: PdfDocument)
    fun onFirstPageRendered()
    fun onDocumentLoadFailed(error: Throwable)
}

@OptIn(ExperimentalPdfApi::class)
class ReadOnlyGateFragment : PdfViewerFragment() {
    private val host: GateFragmentHost?
        get() = activity as? GateFragmentHost

    override fun onLoadDocumentSuccess(document: PdfDocument) {
        super.onLoadDocumentSuccess(document)
        host?.onDocumentLoaded(document)
    }

    override fun onLoadDocumentError(error: Throwable) {
        super.onLoadDocumentError(error)
        host?.onDocumentLoadFailed(error)
    }

    override fun onPdfViewCreated(pdfView: PdfView) {
        super.onPdfViewCreated(pdfView)
        pdfView.addOnFirstContentLoadListener { host?.onFirstPageRendered() }
    }
}

@RequiresExtension(extension = Build.VERSION_CODES.S, version = GateReport.REQUIRED_EXTENSION)
@OptIn(ExperimentalPdfApi::class)
class EditableGateFragment : EditablePdfViewerFragment() {
    private val host: GateFragmentHost?
        get() = activity as? GateFragmentHost

    override fun onPdfViewCreated(pdfView: PdfView) {
        super.onPdfViewCreated(pdfView)
        pdfView.isFormFillingEnabled = true
        pdfView.addOnFirstContentLoadListener { host?.onFirstPageRendered() }
    }

    override fun onLoadDocumentSuccess(document: PdfDocument) {
        super.onLoadDocumentSuccess(document)
        host?.onDocumentLoaded(document)
    }

    override fun onLoadDocumentError(error: Throwable) {
        super.onLoadDocumentError(error)
        host?.onDocumentLoadFailed(error)
    }
}
