@file:Suppress("TestFunctionName")

package me.saket.extendedspans

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import com.squareup.burst.BurstJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(BurstJUnit4::class)
class RoundRectSpanPainterTest(
  private val layoutDirection: LayoutDirection
) {

  @get:Rule val paparazzi = Paparazzi(
    theme = "Theme.NoTitleBar.Fullscreen",
    deviceConfig = DeviceConfig.PIXEL_5.copy(softButtons = false, screenHeight = 0),
    renderingMode = SessionParams.RenderingMode.V_SCROLL,
  )

  @Composable
  private fun roundRectPainter(
    padding: PaddingValues = PaddingValues(horizontal = 4.dp)
  ) = listOf(
    RoundRectSpanPainter(
      cornerRadius = 4.dp,
      padding = padding,
      topMargin = 4.dp,
      bottomMargin = 2.dp,
      stroke = RoundRectSpanPainter.Stroke(
        color = Color.Black.copy(alpha = 0.2f)
      ),
    )
  )

  @Test fun `single line`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          background(Color.SkyBlue) {
            append("ipsum dolor")
          }
          append(" sit amet, consectetur adipiscing elit, sed do eiusmod tempor.")
        },
        spanPainter = roundRectPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }

  @Test fun `two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          background(Color.SkyBlue) {
            append("ipsum dolor sit amet,")
          }
          append(" consectetur adipiscing elit, sed do eiusmod tempor.")
        },
        spanPainter = roundRectPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }

  @Test fun `more than two lines`() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Lorem ")
          background(Color.SkyBlue) {
            append("ipsum dolor sit amet, consectetur adipiscing")
          }
          append(" elit, sed do eiusmod tempor.")
        },
        spanPainter = roundRectPainter(),
        layoutDirection = layoutDirection,
      )
    }
  }

  @Test fun paragraph() {
    paparazzi.snapshot {
      ExtendedSpansText(
        text = buildAnnotatedString {
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
        },
        spanPainter = roundRectPainter(padding = PaddingValues(8.dp)),
        fontSize = 20.sp,
        fontFamily = FontFamily(Typeface(android.graphics.Typeface.MONOSPACE)),
        layoutDirection = layoutDirection,
      )
    }
  }
}

