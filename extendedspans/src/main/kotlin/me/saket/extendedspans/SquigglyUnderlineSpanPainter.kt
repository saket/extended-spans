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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StampedPathEffectStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.style.TextDecoration.Companion.None
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import me.saket.extendedspans.internal.deserializeToColor
import me.saket.extendedspans.internal.fastFirstOrNull
import me.saket.extendedspans.internal.fastForEach
import me.saket.extendedspans.internal.serialize
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Draws squiggly underlines below text annotated using `SpanStyle(textDecoration = Underline)`.
 * Inspired from [Sam Ruston's BuzzKill app](https://twitter.com/saketme/status/1310073763019530242).
 *
 * ```
 *
 *       _....._                                     _....._         ▲
 *    ,="       "=.                               ,="       "=.   amplitude
 *  ,"             ".                           ,"             ".    │
 *,"                 ".,                     ,,"                 "., ▼
 *""""""""""|""""""""""|."""""""""|""""""""".|""""""""""|""""""""""|
 *                       ".               ."
 *                         "._         _,"
 *                            "-.....-"
 *◀─────────────── Wavelength ──────────────▶
 *
 * ```
 *
 * @param animator See [rememberSquigglyUnderlineAnimator].
 * @param bottomOffset Distance from a line's bottom coordinate.
 */
class SquigglyUnderlineSpanPainter(
  private val width: TextUnit = 2.sp,
  private val wavelength: TextUnit = 9.sp,
  private val amplitude: TextUnit = 1.sp,
  private val bottomOffset: TextUnit = 1.sp,
  private val animator: SquigglyUnderlineAnimator = SquigglyUnderlineAnimator.NoOp,
) : ExtendedSpanPainter() {

  override fun decorate(
    span: SpanStyle,
    start: Int,
    end: Int,
    text: AnnotatedString,
    builder: AnnotatedString.Builder
  ): SpanStyle {
    val textDecoration = span.textDecoration
    return if (textDecoration == null || Underline !in textDecoration) {
      span
    } else {
      val textColor = text.spanStyles.fastFirstOrNull {
        // I don't think this predicate will work for text annotated with overlapping
        // multiple colors, but I'm not too interested in solving for that use case.
        it.start <= start && it.end >= end && it.item.color.isSpecified
      }?.item?.color ?: Color.Unspecified

      builder.addStringAnnotation(TAG, annotation = textColor.serialize(), start = start, end = end)
      span.copy(textDecoration = if (LineThrough in textDecoration) LineThrough else None)
    }
  }

  override fun drawInstructionsFor(layoutResult: TextLayoutResult): SpanDrawInstructions {
    val text = layoutResult.layoutInput.text
    val annotations = text.getStringAnnotations(TAG, start = 0, end = text.length)
    var cachedSquiggle: Path? = null

    return SpanDrawInstructions {
      if (cachedSquiggle == null) {
        cachedSquiggle = buildSquigglePath()
      }

      annotations.fastForEach { annotation ->
        val boxes = layoutResult.getBoundingBoxes(
          startOffset = annotation.start,
          endOffset = annotation.end
        )
        val textColor = annotation.item.deserializeToColor() ?: layoutResult.layoutInput.style.color
        boxes.fastForEach { box ->
          drawLine(
            color = textColor,
            start = box.bottomLeft,
            end = box.bottomRight,
            strokeWidth = 0f, // ignored
            cap = StrokeCap.Round,
            pathEffect = PathEffect.stampedPathEffect(
              shape = cachedSquiggle!!,
              phase = 0f,
              advance = wavelength.toPx(),
              style = StampedPathEffectStyle.Morph,
            )
          )
        }
      }
    }
  }

  private fun DrawScope.buildSquigglePath(): Path {
    val numOfPoints = SEGMENTS_PER_WAVELENGTH
    var pointX = 0f

    return Path().apply {
      (0..numOfPoints).map { point ->
        val proportionOfWavelength = pointX / wavelength.toPx()
        val radiansX = proportionOfWavelength * TWO_PI
        val offsetY = sin(radiansX) * amplitude.toPx()

        when (point) {
          0 -> moveTo(pointX, offsetY)
          else -> lineTo(pointX, offsetY)
        }
        pointX += wavelength.toPx() / SEGMENTS_PER_WAVELENGTH
      }
    }
  }

  companion object {
    private const val TAG = "squiggly_underline_span"
    private const val SEGMENTS_PER_WAVELENGTH = 10
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
