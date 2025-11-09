package com.trueedu.spac.ui.refund

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.api.model.dto.firebase.RefundSchedule
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.repo.firebase.FirebaseRefundDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManageRefundScheduleViewModel @Inject constructor(
    private val refundDatabase: FirebaseRefundDatabase
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var schedules by mutableStateOf<List<RefundSchedule>>(emptyList())
        private set

    var nameKr by mutableStateOf("")
    var code by mutableStateOf("")
    var date by mutableStateOf("")
    var refundAmount by mutableStateOf("")

    var saveSuccess by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
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
        if (nameKr.isBlank() || code.isBlank() || date.isBlank()) {
            errorMessage = "종목명, 종목코드, 입금일을 모두 입력해주세요"
            return
        }

        if (date.length != 8 || date.toIntOrNull() == null) {
            errorMessage = "입금일은 YYYYMMDD 형식으로 입력해주세요 (예: 20250315)"
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
                val amount = refundAmount.toIntOrNull()
                val newSchedule = RefundSchedule(
                    nameKr = nameKr.trim(),
                    code = code.trim().uppercase(),
                    date = date,
                    refundAmount = amount
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
    }

    fun clearMessages() {
        saveSuccess = false
        errorMessage = null
    }
}

