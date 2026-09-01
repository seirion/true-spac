package com.trueedu.spac.ui.chat.views

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatMarkdownTest {

    @Test
    fun `볼드 굵게 표기 파싱`() {
        val blocks = parseMarkdownBlocks("**KB제27호스팩(464680)**의 정보입니다.")
        assertEquals(1, blocks.size)
        val text = (blocks[0] as MdBlock.Paragraph).text
        assertEquals("**KB제27호스팩(464680)**의 정보입니다.", text)

        val annotated = inlineAnnotatedString(text, codeBackground = androidx.compose.ui.graphics.Color.Gray)
        assertEquals("KB제27호스팩(464680)의 정보입니다.", annotated.text)
        assertTrue(annotated.spanStyles.any { it.item.fontWeight == androidx.compose.ui.text.font.FontWeight.Bold })
    }

    @Test
    fun `불릿 리스트와 중첩 불릿 파싱 - 실제 응답 예시`() {
        val raw = """
            *   **일시:** 2026년 7월 29일 (수요일) 오전 10시 00분
            *   **회의 목적 사항 (의안):**
                *   **제1호 의안:** 재산목록 및 대차대조표 승인의 건
        """.trimIndent()

        val blocks = parseMarkdownBlocks(raw)
        assertEquals(3, blocks.size)

        val first = blocks[0] as MdBlock.Bullet
        assertEquals(0, first.indent)
        assertTrue(first.text.startsWith("**일시:**"))

        val nested = blocks[2] as MdBlock.Bullet
        assertTrue(nested.indent > 0)
        assertTrue(nested.text.startsWith("**제1호 의안:**"))
    }

    @Test
    fun `번호 목록 파싱 - 실제 응답 예시`() {
        val raw = "1. 교보15호스팩 (465320): 8.44%\n2. 유진스팩10호 (468760): 7.8%"
        val blocks = parseMarkdownBlocks(raw)
        assertEquals(2, blocks.size)
        assertEquals("1", (blocks[0] as MdBlock.Numbered).number)
        assertEquals("2", (blocks[1] as MdBlock.Numbered).number)
    }

    @Test
    fun `헤더 파싱 - 실제 응답 예시`() {
        val blocks = parseMarkdownBlocks("### 1. 주주총회 개요")
        assertEquals(1, blocks.size)
        val header = blocks[0] as MdBlock.Header
        assertEquals(3, header.level)
        assertEquals("1. 주주총회 개요", header.text)
    }

    @Test
    fun `구분선 파싱`() {
        val blocks = parseMarkdownBlocks("위 내용입니다.\n\n---\n\n아래 내용입니다.")
        assertEquals(3, blocks.size)
        assertEquals(MdBlock.Rule, blocks[1])
    }

    @Test
    fun `인라인 코드 파싱`() {
        val annotated = inlineAnnotatedString(
            "종목 코드 예) `48xxxx`",
            codeBackground = androidx.compose.ui.graphics.Color.Gray,
        )
        assertEquals("종목 코드 예) 48xxxx", annotated.text)
        assertTrue(
            annotated.spanStyles.any {
                it.item.fontFamily == androidx.compose.ui.text.font.FontFamily.Monospace
            }
        )
    }

    @Test
    fun `일반 문단은 볼드 없이 그대로 유지`() {
        val blocks = parseMarkdownBlocks("삼성전자의 최근 30일간 공시 목록입니다.")
        assertEquals(1, blocks.size)
        assertTrue(blocks[0] is MdBlock.Paragraph)
    }

    @Test
    fun `블록 수식 파싱 - 실제 응답 예시`() {
        val raw = """
            ### 1. 기본 계산 수식
            $$\text{예상 청산금액} = \text{공모가(2,000원)} + \text{예치금 이자}$$

            ### 2. 상세 항목 설명
        """.trimIndent()

        val blocks = parseMarkdownBlocks(raw)
        assertEquals(3, blocks.size)
        assertTrue(blocks[0] is MdBlock.Header)

        val math = blocks[1] as MdBlock.Math
        assertEquals(
            "\\text{예상 청산금액} = \\text{공모가(2,000원)} + \\text{예치금 이자}",
            math.latex,
        )

        assertTrue(blocks[2] is MdBlock.Header)
    }

    @Test
    fun `인라인 수식은 유니코드 기호로 정규화`() {
        val annotated = inlineAnnotatedString(
            "[합병 불성립] \$\\rightarrow\$ [주주총회 결의]",
            codeBackground = androidx.compose.ui.graphics.Color.Gray,
        )
        assertEquals("[합병 불성립] → [주주총회 결의]", annotated.text)
    }

    @Test
    fun `파이프 표 파싱 - 실제 응답 예시`() {
        val raw = """
            현재 **합병 심사 중**인 스팩 목록입니다. (총 5개)

            | 스팩 이름 | 종목코드 | 예상 청산일 | 예상 수익률 |
            | :--- | :--- | :--- | :--- |
            | **IBKS제24호스팩** | 469480 | 2027-01-16 | 7.79% |
            | **유진스팩10호** | 468760 | 2027-02-13 | 7.80% |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(raw)
        val table = blocks.filterIsInstance<MdBlock.Table>().single()

        assertEquals(listOf("스팩 이름", "종목코드", "예상 청산일", "예상 수익률"), table.header)
        assertEquals(2, table.rows.size)
        assertEquals("469480", table.rows[0][1])
        assertEquals("7.80%", table.rows[1][3])

        // 표 앞 문단은 그대로 남는다
        assertTrue(blocks.filterIsInstance<MdBlock.Paragraph>().any { it.text.contains("합병 심사") })
    }

    @Test
    fun `구분선 없는 파이프 문장은 표로 오인하지 않는다`() {
        val blocks = parseMarkdownBlocks("검색은 A | B 형태로 입력하세요")
        assertTrue(blocks.filterIsInstance<MdBlock.Table>().isEmpty())
        assertEquals(1, blocks.filterIsInstance<MdBlock.Paragraph>().size)
    }

    @Test
    fun `열 수가 모자란 행은 빈 칸으로 채운다`() {
        val raw = "| A | B | C |\n| --- | --- | --- |\n| 1 | 2 |"
        val table = parseMarkdownBlocks(raw).filterIsInstance<MdBlock.Table>().single()
        assertEquals(3, table.rows[0].size)
        assertEquals("", table.rows[0][2])
    }
}
