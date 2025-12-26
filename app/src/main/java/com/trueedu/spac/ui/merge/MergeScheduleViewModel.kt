package com.trueedu.spac.ui.merge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.spac.data.log.logE
import com.trueedu.spac.repo.etc.readMergeSchedule
import com.trueedu.spac.ui.merge.model.MergeSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MergeScheduleViewModel @Inject constructor() : ViewModel() {

    var loading by mutableStateOf(false)
        private set

    var schedules by mutableStateOf<List<MergeSchedule>>(emptyList())
        private set

    init {
        loadMergeSchedules()
    }

    private fun loadMergeSchedules() {
        viewModelScope.launch {
            loading = true
            try {
                schedules = readMergeSchedule()
            } catch (e: Exception) {
                logE(e, "Failed to load merge schedules")
                schedules = emptyList()
            } finally {
                loading = false
            }
        }
    }
}


