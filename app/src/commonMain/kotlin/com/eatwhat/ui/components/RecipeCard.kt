package com.eatwhat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagDefaults
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
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
                PText(
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
                    RecipeType.STAPLE -> "主食" to StapleOrange
                    RecipeType.OTHER -> "其他" to OtherPurple
                  }

                  PTag(
                    text = typeLabel,
                    size = TagSize.Small,
                    colors = TagDefaults.colors(typeColor)
                  )

                    PText(
                        text = when (recipe.difficulty.name) {
                            "EASY" -> "简单"
                            "MEDIUM" -> "中等"
                            "HARD" -> "困难"
                            else -> recipe.difficulty.name
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PText(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    PText(
                        text = "${recipe.estimatedTime}分钟",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
