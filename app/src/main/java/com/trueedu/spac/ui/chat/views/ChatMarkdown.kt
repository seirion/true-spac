package com.trueedu.spac.ui.chat.views

import android.widget.TextView
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.trueedu.spac.ui.components.TrueText
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin

/**
 * 모델 답변에 섞여 나오는 마크다운 표기를 화면 스타일로 바꾸기 위한 파서.
 *
 * 굵게(**), 목록(*, 1.), 헤더(#), 인라인 코드(`), 구분선(---), 기울임(*),
 * 수식($$), 표(|)를 다룬다. 범용 마크다운 라이브러리 대신 이 앱이 실제로
 * 받는 표기만 다루는 가벼운 파서로 충분하다.
 *
 * 표는 스팩 목록처럼 여러 건을 나열할 때 모델이 자주 쓴다. 지원하기 전에는
 * "| :--- |" 가 원문 그대로 노출됐다.
 */
sealed class MdBlock {
    data class Header(val level: Int, val text: String) : MdBlock()
    data class Bullet(val indent: Int, val text: String) : MdBlock()
    data class Numbered(val indent: Int, val number: String, val text: String) : MdBlock()
    data class Paragraph(val text: String) : MdBlock()
    data class Math(val latex: String) : MdBlock()
    data class Table(val header: List<String>, val rows: List<List<String>>) : MdBlock()
    data object Rule : MdBlock()
}

private val RULE_RE = Regex("^-{3,}$")
private val HEADER_RE = Regex("^(#{1,6})\\s+(.*)$")
private val BULLET_RE = Regex("^(\\s*)\\*\\s+(.*)$")
private val NUMBERED_RE = Regex("^(\\s*)(\\d+)\\.\\s+(.*)$")

// GFM 파이프 표. 두 번째 줄이 구분선(| :--- | ---: |)인지로 표를 판별한다.
// 파이프가 들어간 평범한 문장을 표로 오인하지 않기 위해서다.
private val TABLE_DIVIDER_RE = Regex("^\\|?\\s*:?-{2,}:?\\s*(\\|\\s*:?-{2,}:?\\s*)*\\|?$")

private fun splitTableRow(line: String): List<String> =
    line.trim().removePrefix("|").removeSuffix("|").split("|").map { it.trim() }

// $$...$$ 는 여러 줄에 걸칠 수 있어(청산금액 계산 수식처럼) 줄 단위 파서보다
// 먼저, 문자열 전체에서 한 번에 뽑아낸다.
private val BLOCK_MATH_RE = Regex("\\$\\$(.+?)\\$\\$", RegexOption.DOT_MATCHES_ALL)

fun parseMarkdownBlocks(raw: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    var lastEnd = 0
    for (m in BLOCK_MATH_RE.findAll(raw)) {
        if (m.range.first > lastEnd) {
            blocks.addAll(parseTextBlocks(raw.substring(lastEnd, m.range.first)))
        }
        blocks.add(MdBlock.Math(m.groupValues[1].trim()))
        lastEnd = m.range.last + 1
    }
    if (lastEnd < raw.length) {
        blocks.addAll(parseTextBlocks(raw.substring(lastEnd)))
    }
    return blocks
}

private fun parseTextBlocks(raw: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val paragraph = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks.add(MdBlock.Paragraph(paragraph.joinToString("\n")))
            paragraph.clear()
        }
    }

    val lines = raw.split("\n")
    var i = 0
    while (i < lines.size) {
        val line = lines[i]
        val trimmed = line.trim()

        // 표: 헤더 줄 + 구분선 + 본문 줄들
        if (trimmed.contains("|") && i + 1 < lines.size &&
            TABLE_DIVIDER_RE.matches(lines[i + 1].trim())
        ) {
            flushParagraph()
            val header = splitTableRow(trimmed)
            val rows = mutableListOf<List<String>>()
            var j = i + 2
            while (j < lines.size && lines[j].trim().contains("|")) {
                val cells = splitTableRow(lines[j])
                // 열 수가 헤더와 다르면 잘라내거나 빈 칸으로 채운다. 모델이
                // 셀 안에 파이프를 넣거나 열을 빠뜨리는 경우가 있다.
                rows.add(List(header.size) { cells.getOrElse(it) { "" } })
                j++
            }
            blocks.add(MdBlock.Table(header, rows))
            i = j
            continue
        }

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
        i++
    }
    flushParagraph()
    return blocks
}

// 인라인 수식($...$)은 실사용 답변에서 화살표(\rightarrow) 같은 기호 하나 정도로만
// 쓰였다. 짧은 기호까지 JLaTeXMath 로 이미지를 그리는 건 과하므로, 실제 조판이
// 필요한 $$...$$ 블록 수식(MdBlock.Math)만 진짜로 렌더링하고 인라인은 흔한
// LaTeX 명령을 유니코드 기호로 바꿔치기하는 것으로 충분하다.
private val LATEX_TEXT_RE = Regex("\\\\text\\{([^}]*)\\}")
private val LATEX_INLINE_SYMBOLS = listOf(
    "\\rightarrow" to "→", "\\Rightarrow" to "⇒", "\\leftarrow" to "←",
    "\\times" to "×", "\\div" to "÷", "\\pm" to "±", "\\approx" to "≈",
    "\\leq" to "≤", "\\geq" to "≥", "\\neq" to "≠", "\\cdot" to "·",
)

private fun normalizeInlineLatex(raw: String): String {
    var s = LATEX_TEXT_RE.replace(raw) { it.groupValues[1] }
    for ((cmd, symbol) in LATEX_INLINE_SYMBOLS) {
        s = s.replace(cmd, symbol)
    }
    return s.trim()
}

/**
 * 한 줄(또는 한 문단) 안의 굵게(**)·기울임(*)·인라인 코드(`)·인라인 수식($)을 스팬으로 바꾼다.
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
                raw[i] == '$' -> {
                    val end = raw.indexOf('$', i + 1)
                    if (end == -1) {
                        append(raw[i])
                        i += 1
                    } else {
                        append(normalizeInlineLatex(raw.substring(i + 1, end)))
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
    background: Color,
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

                is MdBlock.Table -> MarkdownTable(
                    block = block,
                    color = color,
                    codeBackground = codeBackground,
                    background = background,
                )

                is MdBlock.Math -> LatexBlockView(
                    latex = block.latex,
                    color = color,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

private const val TABLE_CELL_PADDING_DP = 8
private const val TABLE_MAX_COL_WIDTH_DP = 160

/**
 * 파이프 표를 격자로 그린다.
 *
 * 말풍선 폭이 300dp 로 제한돼 있어 4열짜리 표는 들어가지 않는다. 열 폭을
 * 내용에 맞춰 실제로 재고, 총합이 넘치면 가로로 스크롤한다. 열마다 폭을
 * 고정해야 행 사이 정렬이 맞는다 — weight 는 스크롤 영역에서 쓸 수 없다.
 */
@Composable
private fun MarkdownTable(
    block: MdBlock.Table,
    color: Color,
    codeBackground: Color,
    background: Color,
) {
    val measurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = BASE_FONT_SIZE.sp)
    val density = LocalDensity.current

    val widths = remember(block, style) {
        List(block.header.size) { col ->
            val texts = buildList {
                add(block.header.getOrElse(col) { "" })
                block.rows.forEach { add(it.getOrElse(col) { "" }) }
            }
            // 굵게 표기(**)는 폭 계산에서 빼야 실제보다 넓게 잡히지 않는다.
            val widest = texts.maxOf { text ->
                val plain = text.replace("**", "").replace("`", "")
                with(density) { measurer.measure(plain, style).size.width.toDp().value }
            }
            (widest + TABLE_CELL_PADDING_DP * 2)
                .coerceAtMost(TABLE_MAX_COL_WIDTH_DP.toFloat())
        }
    }

    val scrollState = rememberScrollState()

    Box {
    Column(modifier = Modifier.horizontalScroll(scrollState)) {
        Row {
            block.header.forEachIndexed { col, cell ->
                TrueText(
                    s = inlineAnnotatedString(cell, codeBackground),
                    fontSize = BASE_FONT_SIZE,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                    modifier = Modifier
                        .width(widths[col].dp)
                        .padding(horizontal = TABLE_CELL_PADDING_DP.dp, vertical = 4.dp),
                )
            }
        }
        HorizontalDivider(color = color.copy(alpha = 0.3f))

        block.rows.forEachIndexed { rowIndex, row ->
            if (rowIndex > 0) {
                HorizontalDivider(color = color.copy(alpha = 0.12f))
            }
            Row {
                row.forEachIndexed { col, cell ->
                    TrueText(
                        s = inlineAnnotatedString(cell, codeBackground),
                        fontSize = BASE_FONT_SIZE,
                        color = color,
                        maxLines = Int.MAX_VALUE,
                        modifier = Modifier
                            .width(widths[col].dp)
                            .padding(horizontal = TABLE_CELL_PADDING_DP.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }

        // 가로로 더 볼 게 있다는 힌트. 열이 잘려 보이기만 하면 스크롤할 수
        // 있다는 걸 알기 어렵다. 말풍선 배경색으로 페이드해 자연스럽게 만든다.
        if (scrollState.canScrollForward) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            0.88f to Color.Transparent,
                            1f to background,
                        )
                    )
            )
        }
        if (scrollState.canScrollBackward) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            0f to background,
                            0.12f to Color.Transparent,
                        )
                    )
            )
        }
    }
}

/**
 * $$...$$ 수식을 JLaTeXMath(Markwon ext-latex)로 실제 조판해서 그린다.
 * Compose 에 내장된 수식 렌더러가 없어 TextView 를 감싸 쓴다.
 */
@Composable
private fun LatexBlockView(
    latex: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val textColor = color.toArgb()
    val textSizePx = with(density) { BASE_FONT_SIZE.dp.toPx() }

    val markwon = remember(context, textSizePx) {
        Markwon.builder(context)
            .usePlugin(JLatexMathPlugin.create(textSizePx))
            .build()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx -> TextView(ctx) },
        update = { textView ->
            textView.setTextColor(textColor)
            markwon.setMarkdown(textView, "$$" + latex + "$$")
        },
    )
}
