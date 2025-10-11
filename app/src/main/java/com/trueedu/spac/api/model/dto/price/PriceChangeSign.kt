package com.trueedu.spac.api.model.dto.price

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * 전일 대비 부호
 */
enum class PriceChangeSign(val code: String, val description: String) {
    UPPER_LIMIT("1", "상한"),
    RISE("2", "상승"),
    UNCHANGED("3", "보합"),
    LOWER_LIMIT("4", "하한"),
    FALL("5", "하락"),
    UNKNOWN("0", "알 수 없음");

    companion object {
        fun from(code: String): PriceChangeSign {
            return entries.find { it.code == code } ?: UNKNOWN
        }
    }

    fun isPositive() = this == UPPER_LIMIT || this == RISE
    fun isNegative() = this == LOWER_LIMIT || this == FALL
    fun isUnchanged() = this == UNCHANGED
}

object PriceChangeSignSerializer : KSerializer<PriceChangeSign> {
    override val descriptor = PrimitiveSerialDescriptor("PriceChangeSign", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: PriceChangeSign) {
        encoder.encodeString(value.code)
    }

    override fun deserialize(decoder: Decoder): PriceChangeSign {
        return PriceChangeSign.from(decoder.decodeString())
    }
}

