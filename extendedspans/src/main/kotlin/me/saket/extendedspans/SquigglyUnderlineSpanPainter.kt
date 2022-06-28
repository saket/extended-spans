@file:Suppress("NAME_SHADOWING")

package me.saket.extendedspans

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.style.TextDecoration.Companion.None
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * TODO: draw ASCII art
 * @param animator See [rememberSquigglyUnderlineAnimator].
 */
class SquigglyUnderlineSpanPainter(
  private val color: Color,
  private val width: TextUnit = 2.sp,
  private val wavePeriod: TextUnit = 9.sp,
  private val amplitude: TextUnit = 1.sp,
  private val baselineOffset: TextUnit = 1.sp,
  private val animator: SquigglyUnderlineAnimator = SquigglyUnderlineAnimator.NoOp,
) : ExtendedSpanPainter {
  private val path = Path()

  override fun decorate(span: SpanStyle, start: Int, end: Int, text: AnnotatedString.Builder): SpanStyle {
    val textDecoration = span.textDecoration
    return if (textDecoration == null || Underline !in textDecoration) {
      span
    } else {
      text.addStringAnnotation(TAG, annotation = "ignored", start = start, end = end)
      span.copy(textDecoration = if (LineThrough in textDecoration) LineThrough else None)
    }
  }

  override fun drawInstructionsFor(layoutResult: TextLayoutResult): SpanDrawInstructions {
    val text = layoutResult.layoutInput.text
    val annotations = text.getStringAnnotations(TAG, start = 0, end = text.length)

    return SpanDrawInstructions {
      val pathStyle = Stroke(
        width = width.toPx(),
        join = StrokeJoin.Round,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(radius = wavePeriod.toPx()),
      )

      annotations.forEach { annotation ->
        val boxes = layoutResult.getBoundingBoxes(
          scope = this,
          startOffset = annotation.start,
          endOffset = annotation.end
        )
        boxes.forEach { box ->
          path.reset()
          path.buildPathFor(box, density = this)
          drawPath(
            path = path,
            color = color,
            style = pathStyle
          )
        }
      }
    }
  }

  /**
   * Inspired from [squigglyspans](https://github.com/samruston/squigglyspans).
   */
  private fun Path.buildPathFor(box: Rect, density: Density) = density.run {
    val lineStart = box.left + (width.toPx() / 2)
    val lineEnd = box.right - (width.toPx() / 2)
    val lineBaseline = box.bottom + baselineOffset.toPx()

    val segmentWidth = wavePeriod.toPx() / SEGMENTS_PER_WAVE_PERIOD
    val amountPoints = ceil((lineEnd - lineStart) / segmentWidth).toInt() + 1

    var pointX = lineStart
    (0 until amountPoints).forEach { point ->
      val proportionOfPeriod = (pointX - lineStart) / wavePeriod.toPx()
      val radiansX = proportionOfPeriod * TWO_PI + (TWO_PI * animator.animationProgress.value)
      val offsetY = lineBaseline + (sin(radiansX) * amplitude.toPx())

      when (point) {
        0 -> moveTo(pointX, offsetY)
        else -> lineTo(pointX, offsetY)
      }
      pointX = (pointX + segmentWidth).coerceAtMost(lineEnd)
    }
  }

  companion object {
    private const val TAG = "squiggly_underline_span"
    private const val SEGMENTS_PER_WAVE_PERIOD = 10
    private const val TWO_PI = 2 * Math.PI.toFloat()
  }
}

@Composable
fun rememberSquigglyUnderlineAnimator(duration: Duration = 1.seconds): SquigglyUnderlineAnimator {
  val animationProgress = rememberInfiniteTransition().animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(duration.inWholeMilliseconds.toInt(), easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    )
  )
  return remember {
    SquigglyUnderlineAnimator(animationProgress)
  }
}

@Stable
class SquigglyUnderlineAnimator internal constructor(internal val animationProgress: State<Float>) {
  companion object {
    val NoOp = SquigglyUnderlineAnimator(animationProgress = mutableStateOf(0f))
  }
}
