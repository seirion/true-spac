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
}
