package com.pushback.app.domain

import java.net.URI

data class ParsedJobInfo(
    val company: String,
    val role: String,
    val cleanUrl: String,
)

object JobParser {
    fun parse(rawUrl: String): ParsedJobInfo {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return ParsedJobInfo("", "", "")

        val url = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
        val uri = runCatching { URI(url) }.getOrNull() ?: return ParsedJobInfo("", "", url)

        val host = uri.host?.lowercase().orEmpty().removePrefix("www.")
        val pathSegments = uri.path?.split("/")?.filter { it.isNotBlank() } ?: emptyList()

        var inferredCompany = ""
        var inferredRole = ""

        when {
            host.contains("greenhouse.io") -> {
                // e.g. boards.greenhouse.io/stripe/jobs/12345
                if (pathSegments.isNotEmpty()) {
                    inferredCompany = cleanSlug(pathSegments.first())
                }
            }
            host.contains("lever.co") -> {
                // e.g. jobs.lever.co/figma/uuid-or-role
                if (pathSegments.isNotEmpty()) {
                    inferredCompany = cleanSlug(pathSegments.first())
                }
                if (pathSegments.size > 1) {
                    val segment = pathSegments[1]
                    if (!isRandomUuidOrHex(segment)) {
                        inferredRole = cleanSlug(segment)
                    }
                }
            }
            host.contains("ashbyhq.com") -> {
                // e.g. jobs.ashbyhq.com/openai/uuid-or-role
                if (pathSegments.isNotEmpty()) {
                    inferredCompany = cleanSlug(pathSegments.first())
                }
                if (pathSegments.size > 1) {
                    val segment = pathSegments[1]
                    if (!isRandomUuidOrHex(segment)) {
                        inferredRole = cleanSlug(segment)
                    }
                }
            }
            host.contains("linkedin.com") -> {
                // e.g. linkedin.com/jobs/view/senior-android-engineer-at-airbnb-3819283
                val jobSlug = pathSegments.lastOrNull().orEmpty()
                if (jobSlug.contains("-at-")) {
                    val parts = jobSlug.split("-at-")
                    inferredRole = cleanSlug(parts.first())
                    inferredCompany = cleanSlug(parts.getOrNull(1)?.replace(Regex("-\\d+$"), "").orEmpty())
                } else if (jobSlug.isNotBlank() && !jobSlug.all { it.isDigit() }) {
                    inferredRole = cleanSlug(jobSlug.replace(Regex("-\\d+$"), ""))
                }
            }
            host.contains("wellfound.com") || host.contains("angel.co") -> {
                // e.g. wellfound.com/company/cursor/jobs/123-senior-engineer
                val compIdx = pathSegments.indexOf("company")
                if (compIdx >= 0 && compIdx + 1 < pathSegments.size) {
                    inferredCompany = cleanSlug(pathSegments[compIdx + 1])
                }
                val jobIdx = pathSegments.indexOf("jobs")
                if (jobIdx >= 0 && jobIdx + 1 < pathSegments.size) {
                    inferredRole = cleanSlug(pathSegments[jobIdx + 1])
                }
            }
            else -> {
                // General domain fallback: stripe.com/jobs/...
                val domainParts = host.split(".")
                if (domainParts.size >= 2) {
                    val mainDomain = if (domainParts.size == 2) domainParts[0] else domainParts[domainParts.size - 2]
                    if (mainDomain !in listOf("jobs", "careers", "work", "apply", "hire")) {
                        inferredCompany = cleanSlug(mainDomain)
                    }
                }
                val lastSegment = pathSegments.lastOrNull().orEmpty()
                if (lastSegment.isNotBlank() && !lastSegment.all { it.isDigit() } && !isRandomUuidOrHex(lastSegment)) {
                    inferredRole = cleanSlug(lastSegment)
                }
            }
        }

        val cleanUrl = uri.scheme + "://" + uri.host + uri.path
        return ParsedJobInfo(
            company = inferredCompany,
            role = inferredRole,
            cleanUrl = cleanUrl,
        )
    }

    private fun isRandomUuidOrHex(str: String): Boolean {
        val s = str.trim()
        if (s.length in 20..40 && s.matches(Regex("^[a-fA-F0-9-]+$"))) return true
        return false
    }

    private fun cleanSlug(slug: String): String {
        val cleaned = slug
            .replace(Regex("[?#].*$"), "")
            .replace(Regex("^[0-9a-fA-F]{6,}-"), "") // leading hex hash
            .replace(Regex("-\\d{4,}$"), "") // trailing job id
            .replace(Regex("[_-]+"), " ")
            .trim()

        val commonSpecialCasing = mapOf(
            "openai" to "OpenAI",
            "github" to "GitHub",
            "linkedin" to "LinkedIn",
            "ios" to "iOS",
            "ai" to "AI",
            "ml" to "ML",
            "dx" to "DX",
            "ui" to "UI",
            "ux" to "UX",
        )

        return cleaned.split(" ")
            .filter { it.isNotBlank() && !it.all { char -> char.isDigit() } }
            .joinToString(" ") { word ->
                val lower = word.lowercase()
                commonSpecialCasing[lower] ?: lower.replaceFirstChar { it.uppercase() }
            }
    }
}
