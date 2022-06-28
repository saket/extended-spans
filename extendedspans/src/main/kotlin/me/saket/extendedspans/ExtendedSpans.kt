@file:OptIn(ExperimentalTextApi::class)

package me.saket.extendedspans

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString

@Stable
class ExtendedSpans(
  private vararg val painters: ExtendedSpanPainter
) {
  private var drawInstructions = emptyList<SpanDrawInstructions>()

  fun extend(text: AnnotatedString): AnnotatedString {
    return buildAnnotatedString {
      append(text.text)
      addStringAnnotation(EXTENDED_SPANS_MARKER_TAG, annotation = "ignored", start = 0, end = 0)

      text.spanStyles.forEach {
        var updated = it.item
        painters.forEach { painter ->
          updated = painter.decorate(updated, it.start, it.end, this)
        }
        addStyle(updated, it.start, it.end)
      }
      text.paragraphStyles.forEach {
        addStyle(it.item, it.start, it.end)
      }
      text.getStringAnnotations(start = 0, end = text.length).forEach {
        addStringAnnotation(tag = it.tag, annotation = it.item, start = it.start, end = it.end)
      }
      text.getTtsAnnotations(start = 0, end = text.length).forEach {
        addTtsAnnotation(it.item, it.start, it.end)
      }
    }
  }

  fun onTextLayout(layoutResult: TextLayoutResult) {
    val wasExtendCalled = layoutResult.layoutInput.text.getStringAnnotations(
      tag = EXTENDED_SPANS_MARKER_TAG,
      start = 0,
      end = 0
    ).isNotEmpty()
    check(wasExtendCalled) {
      "ExtendedSpans#extend(AnnotatedString) wasn't called."
    }

    drawInstructions = painters.map {
      it.drawInstructionsFor(layoutResult)
    }
  }

  fun draw(scope: DrawScope) {
    drawInstructions.forEach { instructions ->
      with(instructions) {
        scope.draw()
      }
    }
  }

  companion object {
    private const val EXTENDED_SPANS_MARKER_TAG = "extended_spans_marker"
  }
}
