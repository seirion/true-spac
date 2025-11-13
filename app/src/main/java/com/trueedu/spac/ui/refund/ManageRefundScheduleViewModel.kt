package com.trueedu.spac.ui.refund

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.api.model.dto.firebase.StockInfo
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.stocks.SpacManager
import com.trueedu.spac.repo.firebase.FirebaseRefundDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageRefundScheduleViewModel @Inject constructor(
    private val refundDatabase: FirebaseRefundDatabase,
    private val spacManager: SpacManager
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var schedules by mutableStateOf<List<RefundSchedule>>(emptyList())
        private set

    var nameKr by mutableStateOf("")
    var code by mutableStateOf("")
    var date by mutableStateOf("")
    var refundAmount by mutableStateOf("")
    var fixed by mutableStateOf(false)

    var suggestions by mutableStateOf<List<StockInfo>>(emptyList())
        private set

    var showSuggestions by mutableStateOf(false)
        private set

    var saveSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var editingSchedule by mutableStateOf<RefundSchedule?>(null)
        private set

    var scrollToTop by mutableStateOf(false)
        private set

    init {
        loadRefundSchedules()
    }

    private fun loadRefundSchedules() {
        viewModelScope.launch {
            loading = true
            try {
                schedules = refundDatabase.loadRefundSchedule()
                    .sortedBy { it.date }
                logD("Loaded ${schedules.size} refund schedules")
            } catch (e: Exception) {
                logD("Failed to load refund schedules: ${e.message}")
                schedules = emptyList()
            } finally {
                loading = false
            }
        }
    }

    fun addSchedule() {
        if (editingSchedule != null) {
            updateSchedule()
            return
        }

        if (!validateInputs()) {
            return
        }

        if (schedules.any { it.code == code.trim().uppercase() && it.date == date }) {
            errorMessage = "이미 등록된 종목의 청산 일정입니다"
            return
        }

        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                val amount = refundAmount.toDoubleOrNull()
                val newSchedule = RefundSchedule(
                    nameKr = nameKr.trim(),
                    code = code.trim().uppercase(),
                    date = date,
                    refundAmount = amount,
                    fixed = fixed
                )

                val updatedList = (schedules + newSchedule).sortedBy { it.date }
                refundDatabase.writeRefundSchedule(updatedList)

                schedules = updatedList
                saveSuccess = true

                // 입력 필드 초기화
                clearInputs()

                logD("Successfully added refund schedule: $newSchedule")
            } catch (e: Exception) {
                logD("Failed to add refund schedule: ${e.message}")
                errorMessage = "저장 중 오류가 발생했습니다: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun startEdit(schedule: RefundSchedule) {
        editingSchedule = schedule
        nameKr = schedule.nameKr
        code = schedule.code
        date = schedule.date
        refundAmount = schedule.refundAmount?.toString() ?: ""
        fixed = schedule.fixed
        scrollToTop = true
    }

    fun cancelEdit() {
        editingSchedule = null
        clearInputs()
    }

    private fun updateSchedule() {
        val editing = editingSchedule ?: return

        if (!validateInputs()) {
            return
        }

        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                val amount = refundAmount.toDoubleOrNull()
                val updatedSchedule = RefundSchedule(
                    nameKr = nameKr.trim(),
                    code = code.trim().uppercase(),
                    date = date,
                    refundAmount = amount,
                    fixed = fixed
                )

                val updatedList = schedules.map {
                    if (it == editing) updatedSchedule else it
                }.sortedBy { it.date }

                refundDatabase.writeRefundSchedule(updatedList)

                schedules = updatedList
                saveSuccess = true
                editingSchedule = null

                // 입력 필드 초기화
                clearInputs()

                logD("Successfully updated refund schedule: $updatedSchedule")
            } catch (e: Exception) {
                logD("Failed to update refund schedule: ${e.message}")
                errorMessage = "수정 중 오류가 발생했습니다: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun deleteSchedule(schedule: RefundSchedule) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                val updatedList = schedules.filter { it != schedule }
                refundDatabase.writeRefundSchedule(updatedList)
                schedules = updatedList
                logD("Successfully deleted refund schedule: $schedule")
            } catch (e: Exception) {
                logD("Failed to delete refund schedule: ${e.message}")
                errorMessage = "삭제 중 오류가 발생했습니다: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun clearInputs() {
        nameKr = ""
        code = ""
        date = ""
        refundAmount = ""
        fixed = false
        hideSuggestions()
    }

    fun clearMessages() {
        saveSuccess = false
        errorMessage = null
    }

    fun resetScrollToTop() {
        scrollToTop = false
    }

    private fun validateInputs(): Boolean {
        if (nameKr.isBlank() || code.isBlank() || date.isBlank()) {
            errorMessage = "종목명, 종목코드, 입금일을 모두 입력해주세요"
            return false
        }

        if (date.length != 8 || date.toIntOrNull() == null) {
            errorMessage = "입금일은 YYYYMMDD 형식으로 입력해주세요 (예: 20250315)"
            return false
        }

        return true
    }

    fun updateNameKr(value: String) {
        try {
            if (value.isNotBlank()) {
                val searchResults = spacManager.search(value).take(10)
                suggestions = searchResults
                showSuggestions = true
            } else {
                suggestions = emptyList()
                showSuggestions = false
            }
        } catch (e: Exception) {
            logD("Failed to search stocks: ${e.message}")
            suggestions = emptyList()
            showSuggestions = false
        }
    }

    fun selectSuggestion(stockInfo: StockInfo) {
        nameKr = stockInfo.nameKr
        code = stockInfo.code
        hideSuggestions()
    }

    fun hideSuggestions() {
        showSuggestions = false
        suggestions = emptyList()
    }
}

