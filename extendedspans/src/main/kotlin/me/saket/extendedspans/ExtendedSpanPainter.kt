package me.saket.extendedspans

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.ResolvedTextDirection.Ltr
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

    // Compose UI does not offer any API for reading paragraph direction for an entire line.
    // So this code assumes that all paragraphs in the text will have the same direction.
    // It also assumes that this paragraph does not contain bi-directional text.
    val isLtr = multiParagraph.getParagraphDirection(offset = layoutInput.text.lastIndex) == Ltr

    return fastMapRange(startLineNum, endLineNum) { lineNum ->
      Rect(
        top = getLineTop(lineNum),
        bottom = getLineBottom(lineNum),
        left = if (lineNum == startLineNum) {
          getHorizontalPosition(startOffset, usePrimaryDirection = isLtr)
        } else {
          getLineLeft(lineNum)
        },
        right = if (lineNum == endLineNum) {
          getHorizontalPosition(endOffset, usePrimaryDirection = isLtr)
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
