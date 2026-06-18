package com.eatwhat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatwhat.domain.model.RollConfig
import com.eatwhat.ui.screens.roll.RollPlannerContent
import xyz.junerver.compose.hooks.useState
import xyz.junerver.compose.palette.components.alert.AlertType
import xyz.junerver.compose.palette.components.alert.PAlert
import xyz.junerver.compose.palette.components.button.ButtonSize
import xyz.junerver.compose.palette.components.button.ButtonType
import xyz.junerver.compose.palette.components.button.PButton
import xyz.junerver.compose.palette.components.card.CardVariant
import xyz.junerver.compose.palette.components.card.PCard
import xyz.junerver.compose.palette.components.progress.PProgress
import xyz.junerver.compose.palette.components.segmented.PSegmented
import xyz.junerver.compose.palette.components.segmented.SegmentedOption
import xyz.junerver.compose.palette.components.statistic.PStatistic
import xyz.junerver.compose.palette.components.statistic.TrendType
import xyz.junerver.compose.palette.components.steps.PSteps
import xyz.junerver.compose.palette.components.steps.StepItem
import xyz.junerver.compose.palette.components.tag.PEditableTagGroup
import xyz.junerver.compose.palette.components.tag.PTag
import xyz.junerver.compose.palette.components.tag.TagDefaults
import xyz.junerver.compose.palette.components.tag.TagSize
import xyz.junerver.compose.palette.components.text.PText
import xyz.junerver.compose.palette.core.theme.PaletteTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EatWhatDesktopApp() {
  PaletteTheme {
    MaterialTheme {
      val (mode, setMode) = useState("balanced")
      val (pantryTags, setPantryTags) = useState(listOf("鸡蛋", "番茄", "青菜", "米饭"))
      val (showRollPlanner, setShowRollPlanner) = useState(false)
      val (lastRollSummary, setLastRollSummary) = useState("尚未 Roll")
      val progress = when (mode) {
        "quick" -> 75f
        "balanced" -> 55f
        else -> 35f
      }

      if (showRollPlanner) {
        RollPlannerContent(
          onRoll = { config ->
            setLastRollSummary(formatRollConfig(config))
            setShowRollPlanner(false)
          }
        )
      } else {
        Box(
        modifier = Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
          verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
          PAlert(
            message = "EatWhat 工作台",
            description = "冰箱、菜谱与备菜流程已同步，今天可以直接从这里开始安排。",
            type = AlertType.Success
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
          ) {
            PCard(
              modifier = Modifier.weight(1.3f),
              variant = CardVariant.Elevated
            ) {
              PText(
                text = "今天吃什么",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(8.dp))
              PText(
                text = "用同一套 common UI 在 Android 与 Desktop 展示菜谱、备菜、筛选和进度。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(20.dp))

              PSegmented(
                options = listOf(
                  SegmentedOption("quick", "快手"),
                  SegmentedOption("balanced", "均衡"),
                  SegmentedOption("fresh", "清爽")
                ),
                value = mode,
                onValueChange = setMode
              )

              Spacer(modifier = Modifier.height(20.dp))
              PProgress(
                percent = progress,
                modifier = Modifier.fillMaxWidth(),
                formatter = { "${it.toInt()}%" }
              )

              Spacer(modifier = Modifier.height(20.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PButton(
                  text = "随机一餐",
                  size = ButtonSize.MEDIUM,
                  type = ButtonType.PRIMARY,
                  onClick = { setShowRollPlanner(true) }
                )
                PButton(
                  text = "清空",
                  size = ButtonSize.MEDIUM,
                  type = ButtonType.PLAIN,
                  onClick = { setLastRollSummary("尚未 Roll") }
                )
              }
            }

            PCard(
              modifier = Modifier.weight(1f),
              variant = CardVariant.Outlined
            ) {
              PText(
                text = "样例指标",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(8.dp))
              PText(
                text = lastRollSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PStatistic(
                  title = "候选菜谱",
                  value = "18",
                  suffix = "道",
                  trend = TrendType.Up
                )
                PStatistic(
                  title = "预计耗时",
                  value = if (mode == "quick") "15" else "35",
                  suffix = "分钟"
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
          ) {
            PCard(
              modifier = Modifier.weight(1f),
              variant = CardVariant.Filled
            ) {
              PText(
                text = "冰箱标签",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(12.dp))
              PEditableTagGroup(
                tags = pantryTags,
                onTagsChange = setPantryTags,
                placeholder = "添加食材",
                size = TagSize.Medium,
                tagColors = { TagDefaults.pastelColors(it) }
              )
              Spacer(modifier = Modifier.height(16.dp))
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                PTag("荤菜", size = TagSize.Small, colors = TagDefaults.errorColors())
                PTag("素菜", size = TagSize.Small, colors = TagDefaults.successColors())
                PTag("汤", size = TagSize.Small, colors = TagDefaults.infoColors())
                PTag("主食", size = TagSize.Small, colors = TagDefaults.warningColors())
              }
            }

            PCard(
              modifier = Modifier.weight(1f),
              variant = CardVariant.Elevated
            ) {
              PText(
                text = "备菜流程",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(12.dp))
              PSteps(
                currentStep = when (mode) {
                  "quick" -> 2
                  "balanced" -> 1
                  else -> 0
                },
                items = listOf(
                  StepItem("选择口味", "根据模式过滤菜谱"),
                  StepItem("汇总食材", "合并相同单位的食材"),
                  StepItem("开始做菜", "进入步骤式烹饪指导")
                )
              )
            }
          }

          PCard(variant = CardVariant.Outlined) {
            PText(
              text = "今日模块",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              SampleSwatch("餐单", Color(0xFFFF6B35))
              SampleSwatch("食材", Color(0xFF4CAF50))
              SampleSwatch("进度", Color(0xFF2196F3))
              SampleSwatch("筛选", Color(0xFF9C27B0))
              SampleSwatch("流程", Color(0xFFFFC107))
            }
          }
        }
      }
      }
    }
  }
}

private fun formatRollConfig(config: RollConfig): String {
  val parts = buildList {
    if (config.meatCount > 0) add("${config.meatCount}荤")
    if (config.vegCount > 0) add("${config.vegCount}素")
    if (config.soupCount > 0) add("${config.soupCount}汤")
    if (config.stapleCount > 0) add("${config.stapleCount}主食")
    if (config.randomCount > 0) add("${config.randomCount}随机")
  }
  return if (parts.isEmpty()) {
    "随机一餐"
  } else {
    "最近一次：${parts.joinToString(" + ")}"
  }
}

@Composable
private fun SampleSwatch(
  label: String,
  color: Color
) {
  Row(
    modifier = Modifier.padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Box(
      modifier = Modifier
        .width(18.dp)
        .height(18.dp)
        .background(color)
    )
    PText(
      text = label,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
