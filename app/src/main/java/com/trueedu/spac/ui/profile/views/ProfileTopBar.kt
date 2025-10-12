package com.trueedu.spac.ui.profile.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.trueedu.spac.ui.common.HeaderTitle
import com.trueedu.spac.ui.components.TouchIcon32

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTopBar(
    onClick: () -> Unit = {},
) {
    TopAppBar(
        navigationIcon = {
            TouchIcon32(icon = Icons.Outlined.AccountCircle, onClick = onClick)
        },
        actions = {
        },
        title = {
            HeaderTitle(s = "로그인")
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
