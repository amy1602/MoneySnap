package com.moneysnap.domain.model

import androidx.annotation.DrawableRes
import com.moneysnap.R

data class AvatarOption(
    val id: String,
    @DrawableRes val drawableRes: Int,
    val label: String
)

object AvatarConstants {
    val avatars = listOf(
        AvatarOption("cat_1", R.drawable.avatar_1, "HAPPY"),
        AvatarOption("cat_2", R.drawable.avatar_2, "CURIOUS"),
        AvatarOption("cat_3", R.drawable.avatar_3, "SLEEPY"),
        AvatarOption("cat_4", R.drawable.avatar_4, "GRUMPY"),
        AvatarOption("cat_5", R.drawable.avatar_5, "SURPRISED"),
        AvatarOption("cat_6", R.drawable.avatar_6, "WINK"),
        AvatarOption("cat_7", R.drawable.avatar_7, "HEART-EYES"),
        AvatarOption("cat_8", R.drawable.avatar_8, "LAUGHING"),
        AvatarOption("cat_9", R.drawable.avatar_9, "THINKING"),
        AvatarOption("cat_10", R.drawable.avatar_10, "SAD"),
        AvatarOption("cat_11", R.drawable.avatar_11, "PLAYFUL"),
        AvatarOption("cat_12", R.drawable.avatar_12, "SHY"),
        AvatarOption("cat_13", R.drawable.avatar_13, "BORED"),
        AvatarOption("cat_14", R.drawable.avatar_14, "EXCITED"),
        AvatarOption("cat_15", R.drawable.avatar_15, "CALM"),
        AvatarOption("cat_16", R.drawable.avatar_16, "SKEPTICAL"),
        AvatarOption("cat_17", R.drawable.avatar_17, "PROUD"),
        AvatarOption("cat_18", R.drawable.avatar_18, "WORRIED")
    )

    fun getAvatarById(id: String?): AvatarOption {
        return avatars.find { it.id == id } ?: avatars.first()
    }
}
