package com.trueedu.spac.ui.profile.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trueedu.spac.R
import com.trueedu.spac.ui.common.HeaderTitle
import com.trueedu.spac.ui.components.TouchIcon32

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreTopBar(
    email: String,
    profileImageUrl: String,
    onClick: () -> Unit = {},
) {
    val loggedIn = email.isNotEmpty()
    TopAppBar(
        navigationIcon = {
            if (loggedIn) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape),
                )
            } else {
                TouchIcon32(icon = Icons.Outlined.AccountCircle, onClick = onClick)
            }
        },
        actions = {
        },
        title = {
            if (loggedIn) {
                HeaderTitle(s = email)
            } else {
                Column(
                    modifier = Modifier.clickable { onClick() },
                ) {
                    HeaderTitle(s = stringResource(R.string.login_with_google))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

