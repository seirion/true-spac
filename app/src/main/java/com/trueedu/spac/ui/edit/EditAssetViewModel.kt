package com.trueedu.spac.ui.edit

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.api.model.dto.firebase.UserAsset
import com.trueedu.spac.data.stocks.StockPool
import com.trueedu.spac.data.user.ManualAssets
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.util.decreasePrice
import com.trueedu.spac.util.decreaseQuantity
import com.trueedu.spac.util.increasePrice
import com.trueedu.spac.util.increaseQuantity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAssetViewModel @Inject constructor(
    private val userCycle: UserCycle,
    private val trueAnalytics: TrueAnalytics,
    private val manualAssets: ManualAssets,
    val stockPool: StockPool,
) : ViewModel() {

    // 주문 입력 (숫자만)
    val priceInput = mutableStateOf(TextFieldValue(""))
    val quantityInput = mutableStateOf(TextFieldValue("0"))
    val memoInput = mutableStateOf("")

    val buttonEnabled = mutableStateOf(false)

    val editMode = mutableStateOf(false)

    val deleteConfirmDialogVisible = mutableStateOf(false)

    fun init(code: String) {
        manualAssets.assets.value.firstOrNull { it.code == code }?.let { myAsset ->
            // 이 종목을 이미 보유한 경우 원래 값을 입력 해 줌
            priceInput.value = myAsset.price.toInt().toString().let { TextFieldValue(it) }
            quantityInput.value = myAsset.quantity.toInt().toString().let { TextFieldValue(it) }
            memoInput.value = myAsset.memo
            editMode.value = true // 편집 모드임
        }

        viewModelScope.launch {
            merge(
                snapshotFlow { priceInput.value.text },
                snapshotFlow { quantityInput.value.text }
            )
                .collectLatest {
                    checkButtonEnabled()
                }
        }
    }

    private fun checkButtonEnabled() {
        val price = priceInput.value.text.toDoubleOrNull() ?: 0.0
        val quantity = quantityInput.value.text.toDoubleOrNull() ?: 0.0
        buttonEnabled.value = price > 0 && quantity > 0
    }

    fun increasePrice() {
        priceInput.value = priceInput.value.copy(
            text = increasePrice(priceInput.value.text)
        )
    }

    fun decreasePrice() {
        priceInput.value = priceInput.value.copy(
            text = decreasePrice(priceInput.value.text)
        )
    }

    fun increaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = increaseQuantity(quantityInput.value.text)
        )
    }

    fun decreaseQuantity() {
        quantityInput.value = quantityInput.value.copy(
            text = decreaseQuantity(quantityInput.value.text)
        )
    }

    fun showDeleteConfirmDialog() {
        deleteConfirmDialogVisible.value = true
    }

    fun hideDeleteConfirmDialog() {
        deleteConfirmDialogVisible.value = false
    }

    fun delete(code: String, onSuccess: () -> Unit) {
        trueAnalytics.clickButton("asset__delete__click")
        manualAssets.deleteAsset(code, onSuccess)
    }

    fun save(code: String, onSuccess: () -> Unit) {
        trueAnalytics.clickButton("asset__save__click")
        val stock = stockPool.get(code)
        manualAssets.addAsset(
            asset = UserAsset(
                code = code,
                nameKr = stock?.nameKr ?: "",
                price = priceInput.value.text.toDouble(),
                quantity = quantityInput.value.text.toDouble(),
                memo = memoInput.value,
            ),
            onSuccess = onSuccess,
        )
    }
}
