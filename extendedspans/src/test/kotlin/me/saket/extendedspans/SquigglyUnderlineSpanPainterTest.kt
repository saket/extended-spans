@file:Suppress("TestFunctionName")

package me.saket.extendedspans

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.squareup.burst.BurstJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(BurstJUnit4::class)
class SquigglyUnderlineSpanPainterTest(
  private val layoutDirection: LayoutDirection
) {

  @get:Rule val paparazzi = Paparazzi(
    theme = "Theme.NoTitleBar.Fullscreen",
    deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false, screenHeight = 0),
    renderingMode = RenderingMode.V_SCROLL,
  )

  @Composable
  private fun squigglyPainter() = listOf(
    SquigglyUnderlineSpanPainter(
      width = 4.sp,
      wavelength = 20.sp,
      amplitude = 2.sp,
      bottomOffset = 2.sp,
      animator = SquigglyUnderlineAnimator.NoOp
    )
  )

  @Test fun `single line`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          colored(Color.DarkGray) {
            append("Lorem ")
          }
          underlined(Color.RosePink) {
            append("ipsum dolor")
          }
          append(" sit amet, consectetur adipiscing elit, sed do eiusmod tempor.")
        },
        spanPainter = squigglyPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }

  @Test fun `two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          underlined(Color.RosePink) {
            append("ipsum dolor sit amet,")
          }
          append(" consectetur adipiscing elit, sed do eiusmod tempor.")
        },
        spanPainter = squigglyPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }

  @Test fun `more than two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          underlined(Color.RosePink) {
            append("ipsum dolor sit amet, consectetur adipiscing")
          }
          append(" elit, sed do eiusmod tempor.")
        },
        spanPainter = squigglyPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }
}
