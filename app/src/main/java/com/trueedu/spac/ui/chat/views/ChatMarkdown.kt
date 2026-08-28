package com.trueedu.spac.ui.chat.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.trueedu.spac.ui.components.TrueText

/**
 * 모델 답변에 섞여 나오는 마크다운 표기를 화면 스타일로 바꾸기 위한 파서.
 *
 * 실사용 답변 40건을 전수 조사한 결과 쓰이는 표기는 굵게(**), 목록(*, 1.),
 * 헤더(#), 인라인 코드(`), 구분선(---), 기울임(*) 뿐이었다 — 표나 인용문은
 * 안 쓰여서 지원하지 않는다. 범용 마크다운 라이브러리 대신 이 앱이 실제로
 * 받는 표기만 다루는 가벼운 파서로 충분하다.
 */
sealed class MdBlock {
    data class Header(val level: Int, val text: String) : MdBlock()
    data class Bullet(val indent: Int, val text: String) : MdBlock()
    data class Numbered(val indent: Int, val number: String, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
    data object Rule : MdBlock()
}

private val RULE_RE = Regex("^-{3,}$")
private val HEADER_RE = Regex("^(#{1,6})\\s+(.*)$")
private val BULLET_RE = Regex("^(\\s*)\\*\\s+(.*)$")
private val NUMBERED_RE = Regex("^(\\s*)(\\d+)\\.\\s+(.*)$")

fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val paragraph = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks.add(MdBlock.Paragraph(paragraph.joinToString("\n")))
            paragraph.clear()
        }
    }

    for (line in raw.split("\n")) {
        val trimmed = line.trim()
        when {
            trimmed.isEmpty() -> flushParagraph()
            RULE_RE.matches(trimmed) -> {
                flushParagraph()
                blocks.add(MdBlock.Rule)
            }
            else -> {
                val header = HEADER_RE.find(line)
                val bullet = BULLET_RE.find(line)
                val numbered = NUMBERED_RE.find(line)
                when {
                    header != null -> {
                        flushParagraph()
                        blocks.add(MdBlock.Header(header.groupValues[1].length, header.groupValues[2]))
                    }
                    bullet != null -> {
                        flushParagraph()
                        blocks.add(MdBlock.Bullet(bullet.groupValues[1].length, bullet.groupValues[2]))
                    }
                    numbered != null -> {
                        flushParagraph()
                        blocks.add(
                            MdBlock.Numbered(
                                numbered.groupValues[1].length,
                                numbered.groupValues[2],
                                numbered.groupValues[3],
                            )
                        )
                    }
                    else -> paragraph.add(line)
                }
            }
        }
    }
    flushParagraph()
    return blocks
}

/**
 * 한 줄(또는 한 문단) 안의 굵게(**)·기울임(*)·인라인 코드(`)를 스팬으로 바꾼다.
 * 중첩은 다루지 않는다 — 실사용 답변에 중첩 표기가 없었다.
 */
fun inlineAnnotatedString(raw: String, codeBackground: Color): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < raw.length) {
            when {
                raw.startsWith("**", i) -> {
                    val end = raw.indexOf("**", i + 2)
                    if (end == -1) {
                        append(raw.substring(i))
                        i = raw.length
                    } else {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(raw.substring(i + 2, end))
                        }
                        i = end + 2
                    }
                }
                raw[i] == '`' -> {
                    val end = raw.indexOf('`', i + 1)
                    if (end == -1) {
                        append(raw.substring(i))
                        i = raw.length
                    } else {
                        withStyle(
                            SpanStyle(
                                fontFamily = FontFamily.Monospace,
                                background = codeBackground,
                            )
                        ) {
                            append(raw.substring(i + 1, end))
                        }
                        i = end + 1
                    }
                }
                raw[i] == '*' && i + 1 < raw.length && raw[i + 1] != '*' -> {
                    val end = raw.indexOf('*', i + 1)
                    if (end == -1 || end == i + 1) {
                        append(raw[i])
                        i += 1
                    } else {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(raw.substring(i + 1, end))
                        }
                        i = end + 1
                    }
                }
                else -> {
                    append(raw[i])
                    i += 1
                }
            }
        }
    }
}

private const val BASE_FONT_SIZE = 14
private const val INDENT_UNIT_DP = 16

/** 답변 텍스트를 블록으로 나눠 볼드·목록·헤더·구분선·인라인 코드를 스타일로 그린다. */
@Composable
fun MarkdownMessageText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val codeBackground = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

    Column(modifier = modifier) {
        blocks.forEachIndexed { index, block ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(4.dp))
            }
            when (block) {
                is MdBlock.Rule -> HorizontalDivider(color = color.copy(alpha = 0.3f))

                is MdBlock.Header -> TrueText(
                    s = inlineAnnotatedString(block.text, codeBackground),
                    fontSize = (18 - (block.level - 1)).coerceAtLeast(BASE_FONT_SIZE),
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                )

                is MdBlock.Bullet -> TrueText(
                    s = buildAnnotatedString {
                        append("•  ")
                        append(inlineAnnotatedString(block.text, codeBackground))
                    },
                    fontSize = BASE_FONT_SIZE,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier.padding(start = (block.indent / 4 * INDENT_UNIT_DP).dp),
                )

                is MdBlock.Numbered -> TrueText(
                    s = buildAnnotatedString {
                        append("${block.number}.  ")
                        append(inlineAnnotatedString(block.text, codeBackground))
                    },
                    fontSize = BASE_FONT_SIZE,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier.padding(start = (block.indent / 4 * INDENT_UNIT_DP).dp),
                )

                is MdBlock.Paragraph -> TrueText(
                    s = inlineAnnotatedString(block.text, codeBackground),
                    fontSize = BASE_FONT_SIZE,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                )
            }
        }
    }
}
