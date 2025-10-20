package com.trueedu.spac.dart.repository.remote

import com.trueedu.spac.di.NormalService
import com.trueedu.spac.network.apiCallFlow

class DartRemoteImpl(
    @NormalService
    private val dartService: DartService
): DartRemote {
    override fun list(corpCode: String, fromDate: String) = apiCallFlow {
        val queries = mapOf(
            "corp_code" to corpCode,
            "bgn_de" to fromDate,
        )
        dartService.list(emptyMap(), queries)
    }
}
