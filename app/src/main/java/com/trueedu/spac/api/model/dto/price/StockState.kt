package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 종목 상태 코드
 */
enum class StockState(val code: String, val description: String) {
    NORMAL("00", "정상"),
    MANAGED("51", "관리종목"),
    INVESTMENT_RISK("52", "투자위험"),
    INVESTMENT_WARNING("53", "투자경고"),
    INVESTMENT_CAUTION("54", "투자주의"),
    CREDIT_AVAILABLE("55", "신용가능"),
    MARGIN_100("57", "증거금 100%"),
    TRADING_HALT("58", "거래정지"),
    SHORT_TERM_OVERHEATING("59", "단기과열"),
    UNKNOWN("99", "알 수 없음");

    companion object {
        fun from(code: String): StockState {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }
}

object StockStateSerializer : KSerializer<StockState> {
    override val descriptor = PrimitiveSerialDescriptor("StockState", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StockState) {
        encoder.encodeString(value.code)
    }

    override fun deserialize(decoder: Decoder): StockState {
        return StockState.from(decoder.decodeString())
    }
}

