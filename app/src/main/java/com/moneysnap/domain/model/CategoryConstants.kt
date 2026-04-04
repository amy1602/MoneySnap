package com.moneysnap.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryConstants {
    val DEFAULT_COLORS = listOf(
        "#FD3C4A", // Red
        "#F48FB1", // Peach
        "#8D6E63", // Brown
        "#00A86B", // Green
        "#FCAC12", // Yellow
        "#7F3DFF", // Purple
        "#4B5563"  // Blue-grey
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
        "ShoppingCart" to Icons.Default.ShoppingCart,
        "Home" to Icons.Default.Home,
        "MonetizationOn" to Icons.Default.MonetizationOn
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
