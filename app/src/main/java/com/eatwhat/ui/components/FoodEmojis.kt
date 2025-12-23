package com.eatwhat.ui.components

/**
 * Food emoji constants for recipe icons
 * All food-related emojis organized by category
 * Used as fallback when user doesn't upload a custom image
 */
object FoodEmojis {

    /**
     * Emoji category with display name and emojis
     */
    data class EmojiCategory(
        val name: String,
        val emojis: List<String>
    )

    /**
     * All food emoji categories
     */
    val categories: List<EmojiCategory> = listOf(
        EmojiCategory(
            name = "肉类",
            emojis = listOf(
                "🍖", // Meat on bone
                "🍗", // Poultry leg
                "🥩", // Cut of meat
                "🥓", // Bacon
                "🌭", // Hot dog
                "🍔", // Hamburger
                "🧆", // Falafel
                "🥙", // Stuffed flatbread
            )
        ),
        EmojiCategory(
            name = "海鲜",
            emojis = listOf(
                "🦐", // Shrimp
                "🦞", // Lobster
                "🦀", // Crab
                "🦑", // Squid
                "🐙", // Octopus
                "🦪", // Oyster
                "🐟", // Fish
                "🐠", // Tropical fish
                "🍣", // Sushi
            )
        ),
        EmojiCategory(
            name = "蔬菜",
            emojis = listOf(
                "🥬", // Leafy green
                "🥦", // Broccoli
                "🥒", // Cucumber
                "🥕", // Carrot
                "🌽", // Corn
                "🌶️", // Hot pepper
                "🫑", // Bell pepper
                "🥔", // Potato
                "🍆", // Eggplant
                "🧄", // Garlic
                "🧅", // Onion
                "🍄", // Mushroom
                "🥗", // Green salad
                "🫛", // Pea pod
                "🫘", // Beans
            )
        ),
        EmojiCategory(
            name = "水果",
            emojis = listOf(
                "🍎", // Red apple
                "🍐", // Pear
                "🍊", // Orange
                "🍋", // Lemon
                "🍌", // Banana
                "🍉", // Watermelon
                "🍇", // Grapes
                "🍓", // Strawberry
                "🫐", // Blueberries
                "🍑", // Peach
                "🍒", // Cherries
                "🥭", // Mango
                "🍍", // Pineapple
                "🥝", // Kiwi
                "🥥", // Coconut
            )
        ),
        EmojiCategory(
            name = "主食",
            emojis = listOf(
                "🍚", // Cooked rice
                "🍙", // Rice ball
                "🍛", // Curry rice
                "🍜", // Steaming bowl (noodles)
                "🍝", // Spaghetti
                "🍞", // Bread
                "🥖", // Baguette
                "🥨", // Pretzel
                "🥯", // Bagel
                "🫓", // Flatbread
                "🥞", // Pancakes
                "🧇", // Waffle
                "🥐", // Croissant
                "🥟", // Dumpling
                "🫔", // Tamale
                "🌮", // Taco
                "🌯", // Burrito
                "🍕", // Pizza
            )
        ),
        EmojiCategory(
            name = "汤品",
            emojis = listOf(
                "🍲", // Pot of food
                "🥘", // Shallow pan of food
                "🍵", // Teacup without handle
                "🫕", // Fondue
                "🥣", // Bowl with spoon
            )
        ),
        EmojiCategory(
            name = "蛋奶",
            emojis = listOf(
                "🥚", // Egg
                "🍳", // Cooking (fried egg)
                "🧈", // Butter
                "🧀", // Cheese
                "🥛", // Glass of milk
            )
        ),
        EmojiCategory(
            name = "甜点",
            emojis = listOf(
                "🍰", // Shortcake
                "🎂", // Birthday cake
                "🧁", // Cupcake
                "🥧", // Pie
                "🍮", // Custard
                "🍩", // Doughnut
                "🍪", // Cookie
                "🍫", // Chocolate bar
                "🍬", // Candy
                "🍭", // Lollipop
                "🍡", // Dango
                "🍧", // Shaved ice
                "🍨", // Ice cream
                "🍦", // Soft ice cream
                "🥮", // Mooncake
            )
        ),
        EmojiCategory(
            name = "饮品",
            emojis = listOf(
                "☕", // Hot beverage
                "🍵", // Teacup
                "🧃", // Beverage box
                "🥤", // Cup with straw
                "🧋", // Bubble tea
                "🍺", // Beer
                "🍻", // Clinking beer mugs
                "🥂", // Clinking glasses
                "🍷", // Wine glass
                "🍶", // Sake
                "🧉", // Mate
            )
        ),
        EmojiCategory(
            name = "其他",
            emojis = listOf(
                "🍿", // Popcorn
                "🥜", // Peanuts
                "🌰", // Chestnut
                "🍯", // Honey pot
                "🥫", // Canned food
                "🧂", // Salt
                "🍱", // Bento box
                "🥡", // Takeout box
                "🥢", // Chopsticks
                "🍴", // Fork and knife
                "🍽️", // Fork and knife with plate
            )
        ),
    )

    /**
     * All emojis flattened into a single list
     */
    val allEmojis: List<String> = categories.flatMap { it.emojis }

    /**
     * Default emoji for new recipes
     */
    const val DEFAULT_EMOJI = "🍽️"

    /**
     * Get emojis by category name
     */
    fun getByCategory(categoryName: String): List<String> {
        return categories.find { it.name == categoryName }?.emojis ?: emptyList()
    }

    /**
     * Suggested emojis for each recipe type
     */
    val suggestionsByRecipeType: Map<String, List<String>> = mapOf(
        "MEAT" to listOf("🍖", "🍗", "🥩", "🥓", "🍔", "🌭"),
        "VEG" to listOf("🥬", "🥦", "🥒", "🥕", "🌽", "🥗", "🍄"),
        "SOUP" to listOf("🍲", "🥘", "🫕", "🥣", "🍵"),
        "STAPLE" to listOf("🍚", "🍙", "🍜", "🍝", "🍞", "🥟", "🍕")
    )

    /**
     * Get suggested emojis for a recipe type
     */
    fun getSuggestionsForType(recipeType: String): List<String> {
        return suggestionsByRecipeType[recipeType] ?: allEmojis.take(10)
    }
}