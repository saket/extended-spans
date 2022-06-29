@file:Suppress("TestFunctionName")

package me.saket.extendedspans

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import org.junit.Rule
import org.junit.Test

class SquigglyUnderlineSpanPainterTest {
  @get:Rule val paparazzi = Paparazzi(
    theme = "Theme.NoTitleBar.Fullscreen",
    deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false, screenHeight = 0),
    renderingMode = RenderingMode.V_SCROLL,
  )

  @Composable
  private fun squigglyPainter() = SquigglyUnderlineSpanPainter(
    color = Color(0xFFF97BB1),
    width = 4.sp,
    wavePeriod = 20.sp,
    amplitude = 2.sp,
    baselineOffset = 2.sp,
    animator = SquigglyUnderlineAnimator.NoOp
  )

  @Test fun `single line`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        spanPainter = squigglyPainter(),
        text = buildAnnotatedString {
          append("Lorem ")
          underlined {
            append("ipsum dolor")
          }
          append(" sit amet, consectetur adipiscing elit, sed do eiusmod tempor.")
        }
      )
    }
  }

  @Test fun `two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        spanPainter = squigglyPainter(),
        text = buildAnnotatedString {
          append("Lorem ")
          underlined {
            append("ipsum dolor sit amet,")
          }
          append(" consectetur adipiscing elit, sed do eiusmod tempor.")
        }
      )
    }
  }

  @Test fun `more than two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        spanPainter = squigglyPainter(),
        text = buildAnnotatedString {
          append("Lorem ")
          underlined {
            append("ipsum dolor sit amet, consectetur adipiscing")
          }
          append(" elit, sed do eiusmod tempor.")
        }
      )
    }
  }

  private fun AnnotatedString.Builder.underlined(block: AnnotatedString.Builder.() -> Unit) = apply {
    withStyle(SpanStyle(textDecoration = Underline), block)
  }
}
