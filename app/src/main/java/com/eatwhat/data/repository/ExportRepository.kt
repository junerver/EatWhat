package com.eatwhat.data.repository

import com.eatwhat.data.sync.ConflictStrategy
import com.eatwhat.data.sync.ExportData
import com.eatwhat.data.sync.ImportPreview
import com.eatwhat.data.sync.ImportResult

/**
 * 导入导出仓库接口
 * 负责数据的导出和导入逻辑
 */
interface ExportRepository {

    /**
     * 导出所有数据（菜谱 + 历史记录）
     */
    suspend fun exportAll(): ExportData

    /**
     * 仅导出菜谱
     */
    suspend fun exportRecipes(): ExportData

    /**
     * 仅导出 AI 供应商配置
     */
    suspend fun exportAIProviders(): ExportData

    /**
     * 预览导入数据（不实际执行导入）
     * @param data 待导入的数据
     * @return 导入预览，包含将新增/更新的数量
     */
    suspend fun previewImport(data: ExportData): ImportPreview

    /**
     * 执行数据导入
     * @param data 待导入的数据
     * @param strategy 冲突处理策略
     * @return 导入结果
     */
    suspend fun importData(data: ExportData, strategy: ConflictStrategy): ImportResult

    /**
     * 获取当前数据统计
     * @return Triple<菜谱数量, 历史数量, AI供应商数量>
     */
    suspend fun getDataCount(): Triple<Int, Int, Int>
}
