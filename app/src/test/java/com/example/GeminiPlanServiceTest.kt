package com.example

import com.example.data.ai.DailyPlanResult
import com.example.data.ai.GeminiPlanService
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GeminiPlanServiceTest {

    private val service = GeminiPlanService()

    @Test
    fun testDailyPlan_GeneratesThreeProblemsForCurrentTopic() = runBlocking {
        val topic = TopicEntity(
            id = 6,
            order = 6,
            name = "Sliding Window",
            status = TopicEntity.STATUS_IN_PROGRESS
        )
        val solvedProblems = listOf(
            ProblemEntity(
                id = "p1",
                topicId = 6,
                title = "Maximum Average Subarray I",
                difficulty = "Easy",
                platform = "LeetCode",
                solved = true
            )
        )

        val result = service.generateDailyPlan(
            topic = topic,
            solvedProblems = solvedProblems,
            unsolvedProblems = emptyList(),
            userDifficultyPreference = "Medium"
        )

        assertTrue(result is DailyPlanResult.Success)
        val success = result as DailyPlanResult.Success
        assertEquals(3, success.problems.size)
        // Ensure already solved problem is not recommended
        assertFalse(success.problems.any { it.title.equals("Maximum Average Subarray I", ignoreCase = true) })
    }

    @Test
    fun testDailyPlan_DifficultyPreferenceRespected() = runBlocking {
        val topic = TopicEntity(
            id = 2,
            order = 2,
            name = "Arrays",
            status = TopicEntity.STATUS_IN_PROGRESS
        )

        val result = service.generateDailyPlan(
            topic = topic,
            solvedProblems = emptyList(),
            unsolvedProblems = emptyList(),
            userDifficultyPreference = "Easy"
        )

        assertTrue(result is DailyPlanResult.Success)
        val success = result as DailyPlanResult.Success
        assertEquals(3, success.problems.size)
        // First problem should prioritize Easy
        assertTrue(success.problems.any { it.difficulty.equals("Easy", ignoreCase = true) })
    }

    @Test
    fun testDailyPlan_AllTopicsHaveCuratedCoverage() = runBlocking {
        for (order in 1..25) {
            val topic = TopicEntity(
                id = order,
                order = order,
                name = "Topic $order",
                status = TopicEntity.STATUS_IN_PROGRESS
            )
            val result = service.generateDailyPlan(
                topic = topic,
                solvedProblems = emptyList(),
                unsolvedProblems = emptyList(),
                userDifficultyPreference = "Medium"
            )
            assertTrue(result is DailyPlanResult.Success)
            val success = result as DailyPlanResult.Success
            assertEquals(3, success.problems.size)
        }
    }
}
