package me.saket.extendedspans

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.saket.extendedspans.internal.fastForEach

/**
 * Draws rectangle spans with rounded corners.
 *
 * [topMargin] and [bottomMargin] are placeholder values that will be automatically calculated from font metrics
 * in the future once Compose UI starts exposing them ([Issue tracker](https://issuetracker.google.com/issues/173648606)).
 * In the meantime, you can calculate these depending upon your text's font size and line height.
 */
class RoundRectSpanPainter(
  private val cornerRadius: Dp = 8.dp,
  private val stroke: Stroke? = null,
  private val padding: PaddingValues = PaddingValues(2.dp),
  private val topMargin: Dp = 0.dp,
  private val bottomMargin: Dp = 0.dp,
) : ExtendedSpanPainter {
  private val path = Path()

  override fun decorate(span: SpanStyle, start: Int, end: Int, text: AnnotatedString.Builder): SpanStyle {
    return if (span.background == Color.Unspecified) {
      span
    } else {
      text.addStringAnnotation(TAG, annotation = "${span.background.toArgb()}", start = start, end = end)
      span.copy(background = Color.Unspecified)
    }
  }

  override fun drawInstructionsFor(layoutResult: TextLayoutResult): SpanDrawInstructions {
    val text = layoutResult.layoutInput.text
    val annotations = text.getStringAnnotations(TAG, start = 0, end = text.length)

    return SpanDrawInstructions {
      val cornerRadius = CornerRadius(cornerRadius.toPx())

      annotations.fastForEach { annotation ->
        val backgroundColor = Color(annotation.item.toInt())
        val boxes = layoutResult.getBoundingBoxes(
          scope = this,
          startOffset = annotation.start,
          endOffset = annotation.end,
          autoExpandToFullParagraph = true
        )
        boxes.forEachIndexed { index, box ->
          path.reset()
          path.addRoundRect(
            RoundRect(
              rect = box.copy(
                left = box.left - padding.calculateStartPadding(this.layoutDirection).toPx(),
                right = box.right + padding.calculateEndPadding(this.layoutDirection).toPx(),
                top = box.top - padding.calculateTopPadding().toPx() + topMargin.toPx(),
                bottom = box.bottom + padding.calculateBottomPadding().toPx() - bottomMargin.toPx(),
              ),
              topLeft = if (index == 0) cornerRadius else CornerRadius.Zero,
              bottomLeft = if (index == 0) cornerRadius else CornerRadius.Zero,
              topRight = if (index == boxes.lastIndex) cornerRadius else CornerRadius.Zero,
              bottomRight = if (index == boxes.lastIndex) cornerRadius else CornerRadius.Zero
            )
          )
          drawPath(
            path = path,
            color = backgroundColor,
            style = Fill
          )
          if (stroke != null) {
            drawPath(
              path = path,
              color = stroke.color(backgroundColor),
              style = Stroke(
                width = stroke.width.toPx(),
              )
            )
          }
        }
      }
    }
  }

  data class Stroke(
    val color: (background: Color) -> Color,
    val width: Dp = 1.dp
  ) {
    constructor(color: Color, width: Dp = 1.dp) : this(
      color = { color },
      width = width
    )
  }

  companion object {
    private const val TAG = "rounded_corner_span"
  }
}
