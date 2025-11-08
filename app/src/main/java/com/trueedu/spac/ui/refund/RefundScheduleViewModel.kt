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
class RefundScheduleViewModel @Inject constructor(
    private val refundDatabase: FirebaseRefundDatabase
) : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var schedules by mutableStateOf<List<RefundSchedule>>(emptyList())
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
}

