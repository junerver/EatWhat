package com.eatwhat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eatwhat.domain.model.Recipe
import com.eatwhat.domain.model.RecipeType
import com.eatwhat.ui.theme.MeatRed
import com.eatwhat.ui.theme.OtherPurple
import com.eatwhat.ui.theme.SoftGreen
import com.eatwhat.ui.theme.SoupBlue
import com.eatwhat.ui.theme.StapleOrange

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
          .fillMaxWidth()
          .then(
            if (onClick != null) Modifier.clickable(onClick = onClick)
            else Modifier
          ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Use RecipeIcon component to display either image or emoji
            RecipeIcon(
                emoji = recipe.icon,
                imageBase64 = recipe.imageBase64,
                size = IconSize.MEDIUM,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  // Type Badge
                  val (typeLabel, typeColor) = when (recipe.type) {
                    RecipeType.MEAT -> "荤" to MeatRed
                    RecipeType.VEG -> "素" to SoftGreen
                    RecipeType.SOUP -> "汤" to SoupBlue
                    RecipeType.STAPLE -> "主" to StapleOrange
                    RecipeType.OTHER -> "他" to OtherPurple
                  }

                  Surface(
                    color = typeColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraSmall
                  ) {
                    Text(
                      text = typeLabel,
                      style = MaterialTheme.typography.labelSmall,
                      color = typeColor,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                  }

                    Text(
                        text = when (recipe.difficulty.name) {
                            "EASY" -> "简单"
                            "MEDIUM" -> "中等"
                            "HARD" -> "困难"
                            else -> recipe.difficulty.name
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "${recipe.estimatedTime}分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
