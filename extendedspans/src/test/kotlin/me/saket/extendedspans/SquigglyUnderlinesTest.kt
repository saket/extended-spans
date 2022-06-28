@file:Suppress("TestFunctionName")

package me.saket.extendedspans

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.androidHome
import app.cash.paparazzi.detectEnvironment
import org.junit.Rule
import org.junit.Test

class SquigglyUnderlinesTest {
  @get:Rule val paparazzi = Paparazzi(
    deviceConfig = DeviceConfig.PIXEL_5,
    environment = detectEnvironment().copy(
      platformDir = "${androidHome()}/platforms/android-31",
      compileSdkVersion = 31
    ),
  )

  @Test fun `empty actions`() {
    paparazzi.snapshot {
      Scaffold {
        TODO()
      }
    }
  }

  @Composable
  private fun Scaffold(content: @Composable BoxWithConstraintsScope.() -> Unit) {
    BoxWithConstraints(
      modifier = Modifier.fillMaxSize(),
      content = content,
      contentAlignment = Alignment.Center
    )
  }
}
