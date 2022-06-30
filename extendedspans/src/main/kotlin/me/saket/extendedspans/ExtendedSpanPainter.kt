package me.saket.extendedspans

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import me.saket.extendedspans.internal.fastMapRange

abstract class ExtendedSpanPainter {

  /**
   * Used for removing any existing spans from [text] so that they can be drawn manually.
   */
  abstract fun decorate(
    span: SpanStyle,
    start: Int,
    end: Int,
    text: AnnotatedString,
    builder: AnnotatedString.Builder
  ): SpanStyle

  abstract fun drawInstructionsFor(
    layoutResult: TextLayoutResult
  ): SpanDrawInstructions

  /**
   * When [flattenForFullParagraph] is available, the bounds for the
   * entire paragraph is returned instead of separate lines if [startOffset]
   * and [endOffset] represent the extreme ends of a paragraph.
   */
  protected fun TextLayoutResult.getBoundingBoxes(
    scope: DrawScope,
    startOffset: Int,
    endOffset: Int,
    flattenForFullParagraph: Boolean = false
  ): List<Rect> {
    val startLineNum = getLineForOffset(startOffset)
    val endLineNum = getLineForOffset(endOffset)

    if (flattenForFullParagraph) {
      val isFullParagraph = (startLineNum != endLineNum)
        && getLineStart(startLineNum) == startOffset
        && getLineEnd(endLineNum) == endOffset

      if (isFullParagraph) {
        return listOf(
          Rect(
            top = getLineTop(startLineNum),
            bottom = getLineBottom(endLineNum),
            left = 0f,
            right = scope.size.width
          )
        )
      }
    }

    return fastMapRange(startLineNum, endLineNum) { lineNum ->
      Rect(
        top = getLineTop(lineNum),
        bottom = getLineBottom(lineNum),
        left = if (lineNum == startLineNum) {
          getHorizontalPosition(startOffset, usePrimaryDirection = true)
        } else {
          getLineLeft(lineNum)
        },
        right = if (lineNum == endLineNum) {
          getHorizontalPosition(endOffset, usePrimaryDirection = true)
        } else {
          getLineRight(lineNum)
        }
      )
    }
  }

}

fun interface SpanDrawInstructions {
  fun DrawScope.draw()
}
