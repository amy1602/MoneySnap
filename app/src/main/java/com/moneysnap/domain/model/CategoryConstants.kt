package com.moneysnap.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryConstants {
    val DEFAULT_COLORS = listOf(
        "#FD3C4A", // Red
        "#F48FB1", // Peach
        "#FCAC12", // Orange/Yellow
        "#00A86B", // Green
        "#00D1FF", // Cyan
        "#7F3DFF", // Purple
        "#8D6E63", // Brown
        "#4B5563", // Blue-grey
        "#FF9AD5", // Pink
        "#D4E157", // Lime
        "#5D11F7"  // Deep Indigo
    )

    val ICON_MAP = mapOf(
        "Restaurant" to Icons.Default.Restaurant,
        "DirectionsCar" to Icons.Default.DirectionsCar,
        "ShoppingBag" to Icons.Default.ShoppingBag,
        "Receipt" to Icons.Default.Receipt,
        "Favorite" to Icons.Default.Favorite,
        "Flight" to Icons.Default.Flight,
        "Movie" to Icons.Default.Movie,
        "School" to Icons.Default.School,
        "CardGiftcard" to Icons.Default.CardGiftcard,
        "Home" to Icons.Default.Home,
        "MonetizationOn" to Icons.Default.MonetizationOn,
        "Groups" to Icons.Default.Groups,         // Family
        "People" to Icons.Default.People,         // Friends
        "HealthAndSafety" to Icons.Default.HealthAndSafety, // Health
        "Pets" to Icons.Default.Pets             // Pet
    )

    fun getIconByName(name: String): ImageVector {
        return ICON_MAP[name] ?: Icons.Default.Category
    }

    val INITIAL_CATEGORIES = listOf(
        Category(id = "food", name = "Food", type = TransactionType.EXPENSE, icon = "Restaurant", color = "#FD3C4A"),
        Category(id = "transport", name = "Transport", type = TransactionType.EXPENSE, icon = "DirectionsCar", color = "#00A86B"),
        Category(id = "shopping", name = "Shopping", type = TransactionType.EXPENSE, icon = "ShoppingBag", color = "#FCAC12"),
        Category(id = "home", name = "Rent & Housing", type = TransactionType.EXPENSE, icon = "Home", color = "#7F3DFF"),
        Category(id = "salary", name = "Salary", type = TransactionType.INCOME, icon = "MonetizationOn", color = "#00A86B")
    )
}
