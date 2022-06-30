@file:Suppress("NAME_SHADOWING")

package me.saket.extendedspans

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextDecoration.Companion.LineThrough
import androidx.compose.ui.text.style.TextDecoration.Companion.None
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.saket.extendedspans.internal.deserializeToColor
import me.saket.extendedspans.internal.fastFirstOrNull
import me.saket.extendedspans.internal.fastForEach
import me.saket.extendedspans.internal.serialize

class AnimatedLineThroughSpanPainter(
  private val width: TextUnit = 2.sp,
  private val animator: LineThroughAnimator,
) : ExtendedSpanPainter() {

  override fun decorate(
    span: SpanStyle,
    start: Int,
    end: Int,
    text: AnnotatedString,
    builder: AnnotatedString.Builder
  ): SpanStyle {
    val textDecoration = span.textDecoration
    return if (textDecoration == null || LineThrough !in textDecoration) {
      span
    } else {
      val textColor = text.spanStyles.fastFirstOrNull {
        // I don't think this predicate will work for text annotated with overlapping
        // multiple colors, but I'm not too interested in solving for that use case.
        it.start <= start && it.end >= end && it.item.color.isSpecified
      }?.item?.color ?: Color.Unspecified

      builder.addStringAnnotation(TAG, annotation = textColor.serialize(), start = start, end = end)
      span.copy(textDecoration = if (Underline in textDecoration) Underline else None)
    }
  }

  override fun drawInstructionsFor(layoutResult: TextLayoutResult): SpanDrawInstructions {
    val text = layoutResult.layoutInput.text
    val annotations = text.getStringAnnotations(TAG, start = 0, end = text.length)
    animator.onNewAnnotation(annotations)

    return SpanDrawInstructions {
      animator.progresses.forEach { annotation, progress ->
        val boxes = layoutResult.getBoundingBoxes(
          startOffset = annotation.start,
          endOffset = annotation.end
        )
        val textColor = annotation.item.deserializeToColor() ?: layoutResult.layoutInput.style.color
        boxes.fastForEach { box ->
          val box = box.copy(
            right = box.left + (box.width * progress)
          )
          drawLine(
            color = textColor,
            start = box.centerLeft,
            end = box.centerRight,
            strokeWidth = width.toPx(),
            cap = StrokeCap.Round,
          )
        }
      }
    }
  }

  companion object {
    private const val TAG = "animated_line_through_span"
  }
}

@Composable
fun rememberLineThroughAnimator(
  animationSpec: AnimationSpec<Float> = tween(1_000)
): LineThroughAnimator {
  val animationProgresses = remember { mutableStateMapOf<AnnotatedString.Range<String>, Float>() }
  val latestAnnotations = remember { mutableStateListOf<AnnotatedString.Range<String>>() }

  LaunchedEffect(latestAnnotations.toList()) {
    val latest = latestAnnotations.toList()
    val added = latest.filter { it !in animationProgresses }
    val removed = animationProgresses.keys.filter { it !in latest }

    println("--------------------------------")
    println("added = ${added.map { it.start..it.end }}")
    println("removed = ${removed.map { it.start..it.end }}")

    added.forEach {
      launch {
        Animatable(initialValue = animationProgresses[it] ?: 0f).animateTo(
          targetValue = 1f,
          animationSpec = animationSpec
        ) {
          animationProgresses[it] = value
        }
      }
    }
    removed.forEach {
      launch {
        Animatable(initialValue = animationProgresses[it] ?: 1f).animateTo(
          targetValue = 0f,
          animationSpec = animationSpec
        ) {
          animationProgresses[it] = value
        }
        animationProgresses.remove(it)
      }
    }
  }

  return remember {
    LineThroughAnimator(
      progresses = animationProgresses,
      onNewAnnotation = {
        println("================================")
        println("Updating latest to ${it.map { it.start..it.end }}")
        latestAnnotations.clear()
        latestAnnotations.addAll(it)
      }
    )
  }
}

@Stable
class LineThroughAnimator internal constructor(
  internal val progresses: SnapshotStateMap<AnnotatedString.Range<String>, Float>,
  internal val onNewAnnotation: (List<AnnotatedString.Range<String>>) -> Unit,
) {

}
