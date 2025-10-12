package com.trueedu.spac.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

sealed interface AppDestinations {
    @Transient
    val label: String

    @Transient
    val icon: ImageVector

    @Serializable
    data object Home : AppDestinations {
        override val label: String = "홈"
        override val icon: ImageVector = Icons.Default.Home
    }

    @Serializable
    data object Following : AppDestinations {
        override val label: String = "관심"
        override val icon: ImageVector = Icons.Default.Favorite
    }

    @Serializable
    data object Profile : AppDestinations {
        override val label: String = "프로필"
        override val icon: ImageVector = Icons.Default.AccountBox
    }

    // @Serializable
    // data class StockDetail(val stockId: String) : AppDestinations {
    //     override val label: String = "Stock Detail"
    //     override val icon: ImageVector = Icons.Default.ShowChart
    // }

    companion object {
        val tabs = listOf(Home, Following, Profile)
    }
}
