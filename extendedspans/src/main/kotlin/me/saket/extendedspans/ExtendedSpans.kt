@file:OptIn(ExperimentalTextApi::class)

package me.saket.extendedspans

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import me.saket.extendedspans.internal.fastFold
import me.saket.extendedspans.internal.fastForEach
import me.saket.extendedspans.internal.fastMap

@Stable
class ExtendedSpans(
  vararg painters: ExtendedSpanPainter
) {
  private val painters = painters.toList()
  internal var drawInstructions = emptyList<SpanDrawInstructions>()

  /**
   * Prepares [text] to be rendered by [painters]. [RoundedCornerSpanPainter] and [SquigglyUnderlineSpanPainter]
   * use this for removing background and underline spans so that they can be drawn manually.
   */
  fun extend(text: AnnotatedString): AnnotatedString {
    return buildAnnotatedString {
      append(text.text)

      // For onTextLayout to be called if a new instance of ExtendedSpans is applied with the same text.
      val uniqueKey = this@ExtendedSpans.hashCode().toString()
      addStringAnnotation(EXTENDED_SPANS_MARKER_TAG, annotation = uniqueKey, start = 0, end = 0)

      text.spanStyles.fastForEach {
        val decorated = painters.fastFold(initial = it.item) { updated, painter ->
          painter.decorate(updated, it.start, it.end, text = text, builder = this)
        }
        addStyle(decorated, it.start, it.end)
      }
      text.paragraphStyles.fastForEach {
        addStyle(it.item, it.start, it.end)
      }
      text.getStringAnnotations(start = 0, end = text.length).fastForEach {
        addStringAnnotation(tag = it.tag, annotation = it.item, start = it.start, end = it.end)
      }
      text.getTtsAnnotations(start = 0, end = text.length).fastForEach {
        addTtsAnnotation(it.item, it.start, it.end)
      }
    }
  }

  fun onTextLayout(layoutResult: TextLayoutResult) {
    layoutResult.checkIfExtendWasCalled()
    drawInstructions = painters.fastMap {
      it.drawInstructionsFor(layoutResult)
    }
  }

  private fun TextLayoutResult.checkIfExtendWasCalled() {
    val wasExtendCalled = layoutInput.text.getStringAnnotations(
      tag = EXTENDED_SPANS_MARKER_TAG,
      start = 0,
      end = 0
    ).isNotEmpty()
    check(wasExtendCalled) {
      "ExtendedSpans#extend(AnnotatedString) wasn't called for this Text()."
    }
  }

  companion object {
    private const val EXTENDED_SPANS_MARKER_TAG = "extended_spans_marker"
  }
}

fun Modifier.drawBehind(spans: ExtendedSpans): Modifier {
  return drawBehind {
    spans.drawInstructions.fastForEach { instructions ->
      with(instructions) {
        draw()
      }
    }
  }
}
