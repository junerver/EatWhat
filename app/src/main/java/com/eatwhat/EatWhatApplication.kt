package com.eatwhat

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eatwhat.data.database.EatWhatDatabase
import com.eatwhat.data.database.entities.*
import com.eatwhat.data.database.relations.RecipeWithDetails
import com.eatwhat.data.repository.RecipeRepository
import com.eatwhat.data.repository.RollRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Application class for EatWhat app
 * Initializes database and other app-level dependencies
 */
class EatWhatApplication : Application() {

    lateinit var database: EatWhatDatabase
        private set

    lateinit var recipeRepository: RecipeRepository
        private set

    lateinit var rollRepository: RollRepository
        private set

    lateinit var historyRepository: com.eatwhat.data.repository.HistoryRepository
        private set

    // Temporary storage for current roll result (for navigation)
    var currentRollResult: com.eatwhat.domain.model.RollResult? = null

    // Temporary storage for current cooking recipes (for navigation)
    var currentCookingRecipes: List<com.eatwhat.data.database.relations.RecipeWithDetails>? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize Room database with sample data callback
        database = Room.databaseBuilder(
            applicationContext,
            EatWhatDatabase::class.java,
            "eatwhat.db"
        )
            .addCallback(DatabaseCallback())
            .addMigrations(
                EatWhatDatabase.MIGRATION_1_2,
                EatWhatDatabase.MIGRATION_2_3,
                EatWhatDatabase.MIGRATION_3_4,
                EatWhatDatabase.MIGRATION_4_5
            )
            .build()

        // Initialize repositories
        recipeRepository = RecipeRepository(database)
        rollRepository = RollRepository(recipeRepository)
        historyRepository = com.eatwhat.data.repository.HistoryRepository(database)
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Populate sample data on first database creation
            populateSampleData(db)
        }

        private fun populateSampleData(db: SupportSQLiteDatabase) {
            val currentTime = System.currentTimeMillis()

            // Sample Recipe 1: 宫保鸡丁 (荤菜)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '宫保鸡丁', 'MEAT', '🍗', 'MEDIUM', 30, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (1, '鸡胸肉', '200', 'G', 1),
                       (1, '花生米', '50', 'G', 2),
                       (1, '干辣椒', '10', 'PIECE', 3),
                       (1, '花椒', '1', 'SPOON', 4)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (1, 1, '鸡肉切丁，加料酒腌制'),
                       (1, 2, '热油炒花生米至金黄'),
                       (1, 3, '爆香干辣椒和花椒'),
                       (1, 4, '下鸡丁炒至变色'),
                       (1, 5, '加入调味料翻炒均匀')
            """)

            // Sample Recipe 2: 清炒西兰花 (素菜)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '清炒西兰花', 'VEG', '🥦', 'EASY', 15, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (2, '西兰花', '1', 'PIECE', 1),
                       (2, '大蒜', '3', 'PIECE', 2),
                       (2, '盐', '适量', 'MODERATE', 3)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (2, 1, '西兰花切小朵焯水'),
                       (2, 2, '蒜切片爆香'),
                       (2, 3, '下西兰花快速翻炒'),
                       (2, 4, '加盐调味出锅')
            """)

            // Sample Recipe 3: 红烧肉 (荤菜)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '红烧肉', 'MEAT', '🥩', 'HARD', 90, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (3, '五花肉', '500', 'G', 1),
                       (3, '冰糖', '30', 'G', 2),
                       (3, '生抽', '2', 'SPOON', 3),
                       (3, '老抽', '1', 'SPOON', 4)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (3, 1, '五花肉切块焯水去腥'),
                       (3, 2, '冰糖炒糖色'),
                       (3, 3, '下五花肉翻炒上色'),
                       (3, 4, '加水小火慢炖1小时'),
                       (3, 5, '大火收汁出锅')
            """)

            // Sample Recipe 4: 蒜蓉菠菜 (素菜)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '蒜蓉菠菜', 'VEG', '🥬', 'EASY', 10, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (4, '菠菜', '300', 'G', 1),
                       (4, '大蒜', '5', 'PIECE', 2),
                       (4, '盐', '适量', 'MODERATE', 3)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (4, 1, '菠菜洗净焯水'),
                       (4, 2, '蒜末爆香'),
                       (4, 3, '下菠菜翻炒加盐')
            """)

            // Sample Recipe 5: 番茄蛋汤 (汤)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '番茄蛋汤', 'SOUP', '🍅', 'EASY', 15, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (5, '番茄', '2', 'PIECE', 1),
                       (5, '鸡蛋', '2', 'PIECE', 2),
                       (5, '盐', '适量', 'MODERATE', 3)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (5, 1, '番茄切块炒出汁'),
                       (5, 2, '加水煮沸'),
                       (5, 3, '打入蛋花加盐调味')
            """)

            // Sample Recipe 6: 蒸米饭 (主食)
            db.execSQL("""
                INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
                VALUES ('${UUID.randomUUID()}', '蒸米饭', 'STAPLE', '🍚', 'EASY', 30, $currentTime, $currentTime, 0)
            """)

            db.execSQL("""
                INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
                VALUES (6, '大米', '2', 'PIECE', 1),
                       (6, '水', '适量', 'MODERATE', 2)
            """)

            db.execSQL("""
                INSERT INTO cooking_steps (recipe_id, step_number, description)
                VALUES (6, 1, '大米淘洗干净'),
                       (6, 2, '加水至刻度线'),
                       (6, 3, '电饭煲蒸煮')
            """)

            // Sample Tags
            db.execSQL("""
                INSERT INTO tags (name, created_at)
                VALUES ('快手菜', $currentTime),
                       ('川菜', $currentTime),
                       ('家常菜', $currentTime),
                       ('下饭菜', $currentTime)
            """)

            // Associate tags with recipes
            db.execSQL("""
                INSERT INTO recipe_tag_cross_ref (recipe_id, tag_id)
                VALUES (1, 2), (1, 3), (1, 4),
                       (2, 1), (2, 3),
                       (3, 3), (3, 4),
                       (4, 1), (4, 3),
                       (5, 1), (5, 3),
                       (6, 3)
            """)
        }
    }
}
