package com.trueedu.spac.ui.components.snackbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * MainActivity 에서 간단히 사용하기 위함
 *
 * 예시:
 * ```
 * val simpleSnackbar = SimpleSnackbar(lifecycleScope)
 * simpleSnackbar.normal("작업이 완료되었습니다")
 * simpleSnackbar.error("오류가 발생했습니다")
 * ```
 */
class SimpleSnackbar(private val scope: CoroutineScope) {
    private val state = TrueSnackbarState()
    private var iconColor: Color? = null

    @Composable
    fun Host() {
        iconColor = MaterialTheme.colorScheme.inverseOnSurface
        state.Host()
    }

    fun normal(message: String) {
        scope.launch {
            state.show(
                message = message,
                icon = Icons.Default.CheckCircle,
                iconTintColor = iconColor,
            )
        }
    }

    fun info(message: String) {
        scope.launch {
            state.show(
                message = message,
                icon = Icons.Default.Info,
                iconTintColor = iconColor,
            )
        }
    }

    fun error(message: String) {
        scope.launch {
            state.show(
                message = message,
                icon = Icons.Default.FlagCircle,
                iconTintColor = iconColor,
            )
        }
    }

    fun setTopToast(isTop: Boolean = true) {
        state.setTopToast(isTop)
    }
}
