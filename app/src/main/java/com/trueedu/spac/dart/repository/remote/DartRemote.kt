package com.trueedu.spac.dart.repository.remote

import com.trueedu.spac.dart.model.DartListResponse
import kotlinx.coroutines.flow.Flow

interface DartRemote {
    /**
     * fromDate: yyyyMMdd
     */
    fun list(corpCode: String, fromDate: String): Flow<DartListResponse>
}
