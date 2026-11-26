package com.pushback.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class JobParserTest {

    @Test
    fun testGreenhouseParsing() {
        val parsed = JobParser.parse("https://boards.greenhouse.io/stripe/jobs/1234567")
        assertEquals("Stripe", parsed.company)
    }

    @Test
    fun testLeverParsing() {
        val parsed = JobParser.parse("https://jobs.lever.co/figma/senior-product-designer")
        assertEquals("Figma", parsed.company)
        assertEquals("Senior Product Designer", parsed.role)
    }

    @Test
    fun testLinkedInParsing() {
        val parsed = JobParser.parse("https://www.linkedin.com/jobs/view/senior-android-engineer-at-airbnb-3819283")
        assertEquals("Airbnb", parsed.company)
        assertEquals("Senior Android Engineer", parsed.role)
    }

    @Test
    fun testAshbyParsing() {
        val parsed = JobParser.parse("https://jobs.ashbyhq.com/openai/research-engineer")
        assertEquals("OpenAI", parsed.company)
        assertEquals("Research Engineer", parsed.role)
    }

    @Test
    fun testWellfoundParsing() {
        val parsed = JobParser.parse("https://wellfound.com/company/cursor/jobs/2819-senior-software-engineer")
        assertEquals("Cursor", parsed.company)
        assertEquals("Senior Software Engineer", parsed.role)
    }
}
