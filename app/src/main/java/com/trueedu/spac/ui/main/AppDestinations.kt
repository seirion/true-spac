package com.trueedu.spac.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
    data object More : AppDestinations {
        override val label: String = "더보기"
        override val icon: ImageVector = Icons.Default.MoreVert
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
    data object Dart : AppDestinations {
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

    @Serializable
    data object RefundSchedule : AppDestinations {
        override val label: String = "스팩 청산 일정"
        @Transient
        override val icon: ImageVector = Icons.Default.BarChart
    }

    companion object {
        val tabs = listOf(Home, Following, More)
    }
}
