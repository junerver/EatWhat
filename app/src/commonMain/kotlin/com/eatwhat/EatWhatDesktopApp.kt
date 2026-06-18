package com.eatwhat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.text.PText
import xyz.junerver.compose.palette.core.theme.PaletteTheme

@Composable
fun EatWhatDesktopApp() {
  PaletteTheme {
    MaterialTheme {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        PCard {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            PText(
              text = "EatWhat",
              style = MaterialTheme.typography.headlineMedium
            )
            PText(
              text = "Compose Multiplatform desktop target is ready for Palette migration.",
              style = MaterialTheme.typography.bodyMedium
            )
            PButton(
              text = "Palette",
              size = ButtonSize.MEDIUM
            )
          }
        }
      }
    }
  }
}
