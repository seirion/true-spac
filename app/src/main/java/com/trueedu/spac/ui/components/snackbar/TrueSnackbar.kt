package com.trueedu.spac.ui.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class TrueSnackbarState {
    var message by mutableStateOf<String?>(null)
        private set
    var icon by mutableStateOf<ImageVector?>(null)
        private set
    var iconTintColor by mutableStateOf<Color?>(null)
        private set
    var onDismiss by mutableStateOf<(() -> Unit)?>(null)
        private set
    var bottomPadding by mutableStateOf(32.dp)
        private set

    private var isTopToast: Boolean = true

    suspend fun show(
        message: String,
        icon: ImageVector? = null,
        iconTintColor: Color? = null,
        onDismiss: () -> Unit = {}
    ) {
        this.message = message
        this.icon = icon
        this.iconTintColor = iconTintColor
        this.onDismiss = onDismiss
    }

    fun setBottomPadding(padding: Int) {
        bottomPadding = padding.dp
    }

    fun setTopToast(isTop: Boolean) {
        isTopToast = isTop
    }

    fun dismiss() {
        message = null
        icon = null
        iconTintColor = null
        onDismiss = null
    }

    @Composable
    fun Host() {
        Box(
            modifier = Modifier.fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = if (isTopToast) Alignment.TopCenter else Alignment.BottomCenter
        ) {
            AnimatedVisibility(
                visible = message != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .imePadding()
                    .then(
                        if (isTopToast) Modifier.padding(top = 32.dp) else Modifier.padding(bottom = bottomPadding)
                    )
            ) {
                message?.let { msg ->
                    TrueToastContent(
                        text = msg,
                        icon = icon,
                        iconTintColor = iconTintColor
                    )

                    LaunchedEffect(Unit) {
                        delay(2500)
                        onDismiss?.invoke()
                        dismiss()
                    }
                }
            }
        }
    }
}

enum class SnackbarType {
    NORMAL, INFO, ERROR
}

@Composable
fun rememberTrueSnackbarState(): TrueSnackbarState {
    return remember { TrueSnackbarState() }
}

@Composable
fun rememberShowSnackbar(
    state: TrueSnackbarState,
    scope: CoroutineScope,
    type: SnackbarType
): (String, () -> Unit) -> Unit {
    val color = when (type) {
        SnackbarType.NORMAL -> MaterialTheme.colorScheme.primary
        SnackbarType.INFO -> MaterialTheme.colorScheme.primary
        SnackbarType.ERROR -> MaterialTheme.colorScheme.error
    }
    val icon = when (type) {
        SnackbarType.NORMAL -> Icons.Default.CheckCircle
        SnackbarType.INFO -> Icons.Default.Info
        SnackbarType.ERROR -> Icons.Default.FlagCircle
    }

    return remember(color) {
        { message, onDismiss ->
            scope.launch {
                state.show(
                    message = message,
                    icon = icon,
                    iconTintColor = color,
                    onDismiss = onDismiss
                )
            }
        }
    }
}
