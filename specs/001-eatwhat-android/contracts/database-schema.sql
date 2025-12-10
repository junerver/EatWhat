-- Database Schema for 吃点啥 (EatWhat) Android App
-- Version: 1
-- Date: 2025-12-10
-- Database: Room (SQLite)

-- ============================================================================
-- Table: recipes (菜谱)
-- ============================================================================
CREATE TABLE IF NOT EXISTS recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    sync_id TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    type TEXT NOT NULL,
    icon TEXT NOT NULL,
    difficulty TEXT NOT NULL,
    estimated_time INTEGER NOT NULL,
    created_at INTEGER NOT NULL,
    last_modified INTEGER NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_recipe_type ON recipes(type);
CREATE INDEX idx_recipe_sync_id ON recipes(sync_id);
CREATE INDEX idx_recipe_deleted ON recipes(is_deleted);

-- ============================================================================
-- Table: ingredients (食材)
-- ============================================================================
CREATE TABLE IF NOT EXISTS ingredients (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    recipe_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    amount TEXT NOT NULL,
    unit TEXT NOT NULL,
    order_index INTEGER NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE INDEX idx_ingredient_recipe_id ON ingredients(recipe_id);

-- ============================================================================
-- Table: cooking_steps (烹饪步骤)
-- ============================================================================
CREATE TABLE IF NOT EXISTS cooking_steps (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    recipe_id INTEGER NOT NULL,
    step_number INTEGER NOT NULL,
    description TEXT NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE INDEX idx_step_recipe_id ON cooking_steps(recipe_id);

-- ============================================================================
-- Table: tags (自定义标签)
-- ============================================================================
CREATE TABLE IF NOT EXISTS tags (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    name TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL
);

CREATE INDEX idx_tag_name ON tags(name);

-- ============================================================================
-- Table: recipe_tag_cross_ref (菜谱-标签关联)
-- ============================================================================
CREATE TABLE IF NOT EXISTS recipe_tag_cross_ref (
    recipe_id INTEGER NOT NULL,
    tag_id INTEGER NOT NULL,
    PRIMARY KEY (recipe_id, tag_id),
    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX idx_recipe_tag_recipe_id ON recipe_tag_cross_ref(recipe_id);
CREATE INDEX idx_recipe_tag_tag_id ON recipe_tag_cross_ref(tag_id);

-- ============================================================================
-- Table: history_records (历史记录)
-- ============================================================================
CREATE TABLE IF NOT EXISTS history_records (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    sync_id TEXT NOT NULL UNIQUE,
    timestamp INTEGER NOT NULL,
    total_count INTEGER NOT NULL,
    meat_count INTEGER NOT NULL,
    veg_count INTEGER NOT NULL,
    soup_count INTEGER NOT NULL,
    summary TEXT NOT NULL,
    last_modified INTEGER NOT NULL,
    is_deleted INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_history_timestamp ON history_records(timestamp);
CREATE INDEX idx_history_sync_id ON history_records(sync_id);

-- ============================================================================
-- Table: history_recipe_cross_ref (历史-菜谱关联，保存快照)
-- ============================================================================
CREATE TABLE IF NOT EXISTS history_recipe_cross_ref (
    history_id INTEGER NOT NULL,
    recipe_id INTEGER NOT NULL,
    recipe_name TEXT NOT NULL,
    recipe_type TEXT NOT NULL,
    recipe_icon TEXT NOT NULL,
    recipe_difficulty TEXT NOT NULL,
    recipe_time INTEGER NOT NULL,
    PRIMARY KEY (history_id, recipe_id),
    FOREIGN KEY (history_id) REFERENCES history_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_history_recipe_history_id ON history_recipe_cross_ref(history_id);

-- ============================================================================
-- Table: prep_items (备菜清单项)
-- ============================================================================
CREATE TABLE IF NOT EXISTS prep_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    history_id INTEGER NOT NULL,
    ingredient_name TEXT NOT NULL,
    is_checked INTEGER NOT NULL DEFAULT 0,
    order_index INTEGER NOT NULL,
    FOREIGN KEY (history_id) REFERENCES history_records(id) ON DELETE CASCADE
);

CREATE INDEX idx_prep_history_id ON prep_items(history_id);

-- ============================================================================
-- Sample Data (Optional - for development/testing)
-- ============================================================================

-- Sample Recipe 1: 宫保鸡丁
INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
VALUES ('sample-recipe-1', '宫保鸡丁', 'meat', '🍗', 'medium', 30, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, 0);

INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
VALUES
    (1, '鸡胸肉', '200', 'g', 1),
    (1, '花生米', '50', 'g', 2),
    (1, '干辣椒', '10', '个', 3),
    (1, '花椒', '1', '勺', 4);

INSERT INTO cooking_steps (recipe_id, step_number, description)
VALUES
    (1, 1, '鸡肉切丁，加料酒腌制'),
    (1, 2, '热油炒花生米至金黄'),
    (1, 3, '爆香干辣椒和花椒'),
    (1, 4, '下鸡丁炒至变色'),
    (1, 5, '加入调味料翻炒均匀');

-- Sample Recipe 2: 清炒西兰花
INSERT INTO recipes (sync_id, name, type, icon, difficulty, estimated_time, created_at, last_modified, is_deleted)
VALUES ('sample-recipe-2', '清炒西兰花', 'veg', '🥦', 'easy', 15, strftime('%s', 'now') * 1000, strftime('%s', 'now') * 1000, 0);

INSERT INTO ingredients (recipe_id, name, amount, unit, order_index)
VALUES
    (2, '西兰花', '1', '个', 1),
    (2, '大蒜', '3', '瓣', 2),
    (2, '盐', '适量', 'moderate', 3);

INSERT INTO cooking_steps (recipe_id, step_number, description)
VALUES
    (2, 1, '西兰花切小朵焯水'),
    (2, 2, '蒜切片爆香'),
    (2, 3, '下西兰花快速翻炒'),
    (2, 4, '加盐调味出锅');

-- Sample Tags
INSERT INTO tags (name, created_at)
VALUES
    ('快手菜', strftime('%s', 'now') * 1000),
    ('川菜', strftime('%s', 'now') * 1000),
    ('家常菜', strftime('%s', 'now') * 1000);

-- Associate tags with recipes
INSERT INTO recipe_tag_cross_ref (recipe_id, tag_id)
VALUES
    (1, 2), -- 宫保鸡丁 -> 川菜
    (1, 3), -- 宫保鸡丁 -> 家常菜
    (2, 1), -- 清炒西兰花 -> 快手菜
    (2, 3); -- 清炒西兰花 -> 家常菜
