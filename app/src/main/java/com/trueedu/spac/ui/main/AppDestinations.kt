package com.trueedu.spac.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
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

    @Serializable
    data class Search(val page: Int): AppDestinations {
        override val label: String = ""
        @Transient
        override val icon: ImageVector = Icons.Default.Search
    }

    @Serializable
    data class StockDetail(val stockId: String) : AppDestinations {
        override val label: String = ""
        @Transient
        override val icon: ImageVector = Icons.Default.BarChart
    }

    @Serializable
    data class EditAsset(val stockId: String) : AppDestinations {
        override val label: String = ""
        @Transient
        override val icon: ImageVector = Icons.Default.Edit
    }

    @Serializable
    data object Admin: AppDestinations {
        override val label: String = "Admin"
        override val icon: ImageVector = Icons.Filled.Settings
    }

    companion object {
        val tabs = listOf(Home, Following, Profile)
    }
}
