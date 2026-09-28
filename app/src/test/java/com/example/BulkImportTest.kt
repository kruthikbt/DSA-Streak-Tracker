package com.example

import com.example.data.model.ProblemEntity
import com.example.ui.components.parseProblemLines
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkImportTest {

    @Test
    fun testParseSimpleTitles_OnePerLine() {
        val input = """
            Two Sum
            Best Time to Buy and Sell Stock
            Maximum Subarray
        """.trimIndent()

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Medium",
            defaultPlatform = "LeetCode",
            existingProblems = emptyList()
        )

        assertEquals(3, parsed.size)
        assertEquals("Two Sum", parsed[0].title)
        assertEquals("Medium", parsed[0].difficulty)
        assertEquals("LeetCode", parsed[0].platform)

        assertEquals("Best Time to Buy and Sell Stock", parsed[1].title)
        assertEquals("Maximum Subarray", parsed[2].title)
    }

    @Test
    fun testParseNumberedTitles() {
        val input = """
            1. Two Sum
            2) Valid Anagram
            3 - Contains Duplicate
        """.trimIndent()

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Medium",
            defaultPlatform = "LeetCode",
            existingProblems = emptyList()
        )

        assertEquals(3, parsed.size)
        assertEquals("Two Sum", parsed[0].title)
        assertEquals("Valid Anagram", parsed[1].title)
        assertEquals("Contains Duplicate", parsed[2].title)
    }

    @Test
    fun testParseTitlesWithDifficultyAnnotations() {
        val input = """
            Two Sum (Easy)
            3Sum [Medium]
            Trapping Rain Water - Hard
        """.trimIndent()

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Medium",
            defaultPlatform = "LeetCode",
            existingProblems = emptyList()
        )

        assertEquals(3, parsed.size)
        assertEquals("Two Sum", parsed[0].title)
        assertEquals("Easy", parsed[0].difficulty)

        assertEquals("3Sum", parsed[1].title)
        assertEquals("Medium", parsed[1].difficulty)

        assertEquals("Trapping Rain Water", parsed[2].title)
        assertEquals("Hard", parsed[2].difficulty)
    }

    @Test
    fun testParseTabSeparatedFromGoogleSheets() {
        val input = "Two Sum\tEasy\tLeetCode\nSubarray Sum Equals K\tMedium\tGFG"

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Hard",
            defaultPlatform = "Other",
            existingProblems = emptyList()
        )

        assertEquals(2, parsed.size)
        assertEquals("Two Sum", parsed[0].title)
        assertEquals("Easy", parsed[0].difficulty)
        assertEquals("LeetCode", parsed[0].platform)

        assertEquals("Subarray Sum Equals K", parsed[1].title)
        assertEquals("Medium", parsed[1].difficulty)
        assertEquals("GFG", parsed[1].platform)
    }

    @Test
    fun testDetectsDuplicatesAgainstExistingProblems() {
        val existing = listOf(
            ProblemEntity(id = "1", topicId = 2, title = "Two Sum", solved = true)
        )

        val input = """
            Two Sum
            Three Sum
        """.trimIndent()

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Medium",
            defaultPlatform = "LeetCode",
            existingProblems = existing
        )

        assertEquals(2, parsed.size)
        assertTrue("Two Sum should be marked as duplicate", parsed[0].isDuplicate)
        assertFalse("Three Sum should not be duplicate", parsed[1].isDuplicate)
    }

    @Test
    fun testIgnoresBlankLines() {
        val input = """
            
            Two Sum
            
            3Sum
            
        """.trimIndent()

        val parsed = parseProblemLines(
            rawText = input,
            defaultDifficulty = "Medium",
            defaultPlatform = "LeetCode",
            existingProblems = emptyList()
        )

        assertEquals(2, parsed.size)
    }
}
