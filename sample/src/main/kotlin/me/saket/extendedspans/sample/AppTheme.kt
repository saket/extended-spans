package me.saket.extendedspans.sample

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  val colorScheme = when {
    darkTheme -> darkColorScheme()
    else -> lightColorScheme()
  }
  MaterialTheme(colorScheme) {
    content()
  }
}
