package com.eatwhat.domain.model

import kotlinx.schema.Description
import kotlinx.schema.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema
data class RecipeAIResult(
  @Description("菜名")
  val name: String,
  @Description("类型: MEAT(荤菜), VEG(素菜), SOUP(汤), STAPLE(主食), OTHER(其他蘸汁酱料等辅助配方)")
  val type: String,
  @Description("难度: EASY, MEDIUM, HARD")
  val difficulty: String,
  @Description("预计烹饪时间(分钟), 1-300")
  val estimatedTime: Int,
  @Description("食材列表")
  val ingredients: List<IngredientAI>,
  @Description("烹饪步骤")
  val steps: List<String>,
  @Description("标签")
  val tags: List<String>,
  @Description("代表菜品的 Emoji 图标")
  val icon: String,
  @Description("输入图片是否为成品食物照片")
  val isFoodImage: Boolean = false
)

@Serializable
@Schema
data class IngredientAI(
  @Description("食材名称")
  val name: String,
  @Description("数量")
  val amount: String,
  @Description("单位: G(克), ML(毫升), PIECE(个), SPOON(勺), MODERATE(适量)")
  val unit: String
)
