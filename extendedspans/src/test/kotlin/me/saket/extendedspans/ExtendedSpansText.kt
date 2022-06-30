@file:Suppress("TestFunctionName")

package me.saket.extendedspans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

@Composable
fun ExtendedSpansText(
  text: AnnotatedString,
  spanPainter: ExtendedSpanPainter,
  layoutDirection: LayoutDirection,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 32.sp,
  fontFamily: FontFamily? = null,
) {
  val extendedSpans = remember(spanPainter) {
    ExtendedSpans(spanPainter)
  }
  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Box(
      modifier
        .background(Color.White)
        .padding(24.dp),
    ) {
      BasicText(
        modifier = Modifier
          .fillMaxWidth()
          .drawBehind {
            extendedSpans.draw(this)
          },
        text = remember(text) {
          extendedSpans.extend(text)
        },
        onTextLayout = { result ->
          extendedSpans.onTextLayout(result)
        },
        style = TextStyle(
          color = Color.Black,
          fontSize = fontSize,
          fontFamily = fontFamily,
          lineHeight = 1.5.em,
        )
      )
    }
  }
}
