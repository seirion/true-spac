package com.trueedu.spac.ui.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.trueedu.spac.ui.common.ActionDialog
import com.trueedu.spac.ui.common.BackTitleTopBar
import com.trueedu.spac.ui.common.Margin
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.edit.views.BottomBar
import com.trueedu.spac.ui.edit.views.InputSet
import com.trueedu.spac.ui.edit.views.MemoInput

@Composable
fun EditAssetScreen(
    stockId: String,
    simpleSnackbar: SimpleSnackbar,
    vm: EditAssetViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    LaunchedEffect(Unit) {
        if (stockId.isEmpty()) {
            onBack()
        } else {
            vm.init(stockId)
        }
    }

    Scaffold(
        topBar = {
            val nameKr = vm.stockPool.get(stockId)?.nameKr?: ""
            val onAction = if (vm.editMode.value) {
                vm::showDeleteConfirmDialog
            } else {
                null
            }
            BackTitleTopBar(
                title = nameKr,
                onBack = onBack,
                actionIcon = Icons.Outlined.Delete,
                onAction = onAction,
            )
        },
        bottomBar = { BottomBar("저장", vm.buttonEnabled.value, vm::onSave) },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.End,
        ) {
            Margin(24)
            InputSet("평단가", vm.priceInput, vm::increasePrice, vm::decreasePrice)
            Margin(24)
            InputSet("수량", vm.quantityInput, vm::increaseQuantity, vm::decreaseQuantity)
            Margin(24)
            MemoInput(vm.memoInput)
        }

        if (vm.deleteConfirmDialogVisible.value) {
            ActionDialog(
                title = "종목 삭제",
                description = "종목을 삭제합니다",
                confirmText = "삭제",
                dismissText = "취소",
                onConfirm = {
                    vm.delete(stockId) {
                        simpleSnackbar.normal("삭제되었습니다")
                        onBack()
                    }
                },
                onDismiss = vm::hideDeleteConfirmDialog
            )
        }
    }
}
