package org.isomorphisms.pdffillerspike

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.pdf.ExperimentalPdfApi
import androidx.pdf.annotation.content.PathPdfObject
import androidx.pdf.annotation.content.PathPdfObject.PathInput
import androidx.pdf.annotation.content.StampAnnotation
import kotlin.math.abs

@OptIn(ExperimentalPdfApi::class)
object VectorMarks {
    val textBounds = RectF(72f, 72f, 360f, 142f)
    val checkBounds = RectF(70f, 168f, 106f, 206f)

    fun outlinedText(text: String): StampAnnotation {
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = 28f
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
            }
        val path = Path()
        paint.getTextPath(text, 0, text.length, 72f, 120f, path)
        val raw = path.approximate(0.35f)
        val inputs = ArrayList<PathInput>(raw.size / 3)
        var previousFraction = -1f
        var index = 0
        while (index + 2 < raw.size) {
            val fraction = raw[index]
            val command =
                if (inputs.isEmpty() || fraction <= previousFraction) {
                    PathInput.MOVE_TO
                } else {
                    PathInput.LINE_TO
                }
            inputs += PathInput(raw[index + 1], raw[index + 2], command)
            previousFraction = fraction
            index += 3
        }
        require(inputs.isNotEmpty()) { "typeface produced no glyph path" }
        return StampAnnotation(
            pageNum = 0,
            bounds = textBounds,
            pdfObjects = listOf(PathPdfObject(Color.BLACK, 0.8f, inputs)),
        )
    }

    // beta01's @IntDef names internal PathOps constants while the public API exposes aliases.
    @SuppressLint("WrongConstant")
    fun check(): StampAnnotation {
        val inputs =
            listOf(
                PathInput(74f, 172f, PathInput.MOVE_TO),
                PathInput(102f, 202f, PathInput.LINE_TO),
                PathInput(102f, 172f, PathInput.MOVE_TO),
                PathInput(74f, 202f, PathInput.LINE_TO),
            )
        return StampAnnotation(
            pageNum = 0,
            bounds = checkBounds,
            pdfObjects = listOf(PathPdfObject(Color.BLACK, 2f, inputs)),
        )
    }

    fun matches(actual: RectF, expected: RectF): Boolean =
        abs(actual.left - expected.left) < 1f &&
            abs(actual.top - expected.top) < 1f &&
            abs(actual.right - expected.right) < 1f &&
            abs(actual.bottom - expected.bottom) < 1f
}
