package me.saket.extendedspans.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.saket.extendedspans.AnimatedLineThroughSpanPainter
import me.saket.extendedspans.ExtendedSpans
import me.saket.extendedspans.RoundRectSpanPainter
import me.saket.extendedspans.SquigglyUnderlineSpanPainter
import me.saket.extendedspans.rememberLineThroughAnimator
import me.saket.extendedspans.rememberSquigglyUnderlineAnimator

class SampleActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      AppTheme {
        Surface(Modifier.fillMaxSize()) {
          ExtendedSpansPreview()
        }
      }
    }
  }
}

@Preview
@Composable
fun ExtendedSpansPreview() {
  val style = MaterialTheme.typography.headlineLarge.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 40.sp,
    lineHeight = 56.sp,
  )

  ProvideTextStyle(style) {
    Column(
      modifier = Modifier.padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("Give your ")
          withStyle(SpanStyle(background = MaterialTheme.colorScheme.primaryContainer)) {
            append("heart and soul")
          }
          append(" to me")
        }
      )
      ExtendedSpansText(
        text = buildAnnotatedString {
          append("And life will always be ")
          withStyle(
            SpanStyle(
              textDecoration = TextDecoration.Underline,
              color = MaterialTheme.colorScheme.error
            )
          ) {
            append("la vie en rose")
          }
        }
      )

      var boughtMilk by remember { mutableStateOf(false) }

      ExtendedSpansText(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { boughtMilk = !boughtMilk },
        text = buildAnnotatedString {
          withStyle(SpanStyle(textDecoration = if (boughtMilk) TextDecoration.None else TextDecoration.LineThrough)) {
            append("Buy milk, mangoes and peaches")
          }
        }
      )
    }
  }
}

@Composable
fun ExtendedSpansText(
  text: AnnotatedString,
  modifier: Modifier = Modifier,
) {
  val underlineAnimator = rememberSquigglyUnderlineAnimator()
  val lineThroughAnimator = rememberLineThroughAnimator()
  val extendedSpans = remember {
    ExtendedSpans(
      RoundRectSpanPainter(
        cornerRadius = 8.dp,
        padding = PaddingValues(horizontal = 4.dp),
        topMargin = 4.dp,
        bottomMargin = 2.dp,
        stroke = RoundRectSpanPainter.Stroke(
          color = Color.White.copy(alpha = 0.2f)
        ),
      ),
      SquigglyUnderlineSpanPainter(
        width = 4.sp,
        wavelength = 20.sp,
        amplitude = 2.sp,
        bottomOffset = 2.sp,
        animator = underlineAnimator
      ),
      AnimatedLineThroughSpanPainter(
        width = 6.sp,
        animator = lineThroughAnimator
      )
    )
  }

  Text(
    modifier = modifier.drawBehind {
      extendedSpans.draw(this)
    },
    text = remember(text) {
      extendedSpans.extend(text)
    },
    onTextLayout = { result ->
      extendedSpans.onTextLayout(result)
    }
  )
}
