package org.isomorphisms.pdffillerspike

import android.content.Context
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale

object PdfFixtures {
    fun form(context: Context): File =
        write(context, "form-fixture.pdf", formPdf())

    fun flat(context: Context): File =
        write(context, "flat-fixture.pdf", flatPdf())

    private fun write(context: Context, name: String, bytes: ByteArray): File {
        val directory = File(context.filesDir, "fixtures").apply { mkdirs() }
        return File(directory, name).also { it.writeBytes(bytes) }
    }

    private fun formPdf(): ByteArray {
        val pageContent =
            "BT /Helv 14 Tf 72 665 Td (Name:) Tj ET\n" +
                "BT /Helv 10 Tf 72 620 Td (AndroidX AcroForm gate fixture) Tj ET"
        return buildPdf(
            listOf(
                "<< /Type /Catalog /Pages 2 0 R /AcroForm 6 0 R >>",
                "<< /Type /Pages /Count 1 /Kids [3 0 R] >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                    "/Resources << /Font << /Helv 7 0 R >> >> /Annots [5 0 R] " +
                    "/Contents 4 0 R >>",
                stream(pageContent),
                "<< /Type /Annot /Subtype /Widget /FT /Tx /T (spike_name) " +
                    "/Rect [125 650 500 680] /V () /DA (/Helv 14 Tf 0 g) " +
                    "/MK << /BC [0 0 0] /BG [1 1 1] >> /BS << /W 1 /S /S >> " +
                    "/P 3 0 R /F 4 >>",
                "<< /Fields [5 0 R] /NeedAppearances true " +
                    "/DR << /Font << /Helv 7 0 R >> >> /DA (/Helv 14 Tf 0 g) >>",
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
            )
        )
    }

    private fun flatPdf(): ByteArray {
        val pageContent =
            "0.8 w 72 560 468 100 re S\n" +
                "BT /F1 16 Tf 86 625 Td (Flat PDF gate fixture) Tj ET\n" +
                "BT /F1 10 Tf 86 590 Td (The spike will place outlined text and an X above.) Tj ET"
        return buildPdf(
            listOf(
                "<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Count 1 /Kids [3 0 R] >>",
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                    "/Resources << /Font << /F1 5 0 R >> >> /Contents 4 0 R >>",
                stream(pageContent),
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
            )
        )
    }

    private fun stream(content: String): String {
        val length = content.toByteArray(StandardCharsets.ISO_8859_1).size
        return "<< /Length $length >>\nstream\n$content\nendstream"
    }

    private fun buildPdf(objects: List<String>): ByteArray {
        val output = ByteArrayOutputStream()
        fun append(value: String) {
            output.write(value.toByteArray(StandardCharsets.ISO_8859_1))
        }

        append("%PDF-1.7\n%\u00e2\u00e3\u00cf\u00d3\n")
        val offsets = IntArray(objects.size + 1)
        objects.forEachIndexed { index, body ->
            val objectNumber = index + 1
            offsets[objectNumber] = output.size()
            append("$objectNumber 0 obj\n$body\nendobj\n")
        }

        val xrefOffset = output.size()
        append("xref\n0 ${objects.size + 1}\n")
        append("0000000000 65535 f \n")
        for (objectNumber in 1..objects.size) {
            append(String.format(Locale.ROOT, "%010d 00000 n \n", offsets[objectNumber]))
        }
        append("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\n")
        append("startxref\n$xrefOffset\n%%EOF\n")
        return output.toByteArray()
    }
}
