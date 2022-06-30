package me.saket.extendedspans

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams.RenderingMode
import com.squareup.burst.BurstJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(BurstJUnit4::class)
class AllSpanPaintersTest(
  private val layoutDirection: LayoutDirection
) {

  @get:Rule val paparazzi = Paparazzi(
    theme = "Theme.NoTitleBar.Fullscreen",
    deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false, screenHeight = 0),
    renderingMode = RenderingMode.V_SCROLL,
  )

  @Test fun snapshot() {
    paparazzi.snapshot {
      val spanPainters = listOf(
        SquigglyUnderlineSpanPainter(
          width = 4.sp,
          wavelength = 20.sp,
          amplitude = 2.sp,
          bottomOffset = 2.sp,
          animator = SquigglyUnderlineAnimator.NoOp
        ),
        RoundRectSpanPainter(
          cornerRadius = 4.dp,
          padding = PaddingValues(horizontal = 4.dp),
          topMargin = 4.dp,
          bottomMargin = 2.dp,
          stroke = RoundRectSpanPainter.Stroke(
            color = Color.Black.copy(alpha = 0.2f)
          ),
        )
      )

      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          background(Color.SkyBlue) {
            append("ipsum ")
            underlined(Color.Black) {
              append("dolor")
            }
          }
          append(" sit amet, consectetur adipiscing elit, ")
          background(Color.SkyBlue) {
            append("sed do eiusmod tempor.")
          }
          append("\n\n")

          background(Color.SkyBlue) {
            append(
              """
                |@Composable
                |fun Greeting() {
                |  Text("Hello world")
                |}
                """.trimMargin()
            )
          }

          append("\n\nincididunt ut labore et ")
          underlined(Color.RosePink) {
            append("dolore magna aliqua")
          }
        },
        spanPainter = spanPainters,
        layoutDirection = layoutDirection,
      )
    }
  }
}
