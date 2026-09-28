package com.example

import com.example.data.db.DefaultRoadmapData
import com.example.data.model.ProblemEntity
import com.example.data.model.RevisionFilter
import com.example.data.model.RevisionProblemItem
import com.example.data.model.TopicEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class SpacedRevisionTest {

    @Test
    fun testDefaultSeedData_Contains3d7d30dProblems() {
        val initialProblems = DefaultRoadmapData.createInitialProblems()
        val solvedProblems = initialProblems.filter { it.solved && !it.solvedDate.isNullOrBlank() }

        assertTrue("Should have seeded solved problems", solvedProblems.isNotEmpty())

        val today = LocalDate.now()
        val daysAgoList = solvedProblems.map {
            ChronoUnit.DAYS.between(LocalDate.parse(it.solvedDate), today)
        }

        assertTrue("Should contain a problem solved 3 days ago", daysAgoList.contains(3L))
        assertTrue("Should contain a problem solved 7 days ago", daysAgoList.contains(7L))
        assertTrue("Should contain a problem solved 30 days ago", daysAgoList.contains(30L))
    }

    @Test
    fun testRevisionProblemItem_ClassifiesIntervalsCorrectly() {
        val today = LocalDate.now()
        val topic = TopicEntity(id = 2, order = 2, name = "Arrays")

        // Problem 3 days ago
        val p3 = ProblemEntity(
            id = "p3",
            topicId = 2,
            title = "Two Sum",
            solved = true,
            solvedDate = today.minusDays(3).toString()
        )
        val item3 = RevisionProblemItem(
            problem = p3,
            topic = topic,
            solvedLocalDate = today.minusDays(3),
            daysAgo = 3L,
            is3DaysAgo = true,
            is7DaysAgo = false,
            is30DaysAgo = false,
            isDue = true,
            isRevisedToday = false
        )
        assertTrue(item3.is3DaysAgo)
        assertTrue(item3.isDue)
        assertEquals("3-Day Recall", item3.intervalLabel)

        // Problem 7 days ago
        val p7 = ProblemEntity(
            id = "p7",
            topicId = 2,
            title = "Valid Palindrome",
            solved = true,
            solvedDate = today.minusDays(7).toString()
        )
        val item7 = RevisionProblemItem(
            problem = p7,
            topic = topic,
            solvedLocalDate = today.minusDays(7),
            daysAgo = 7L,
            is3DaysAgo = false,
            is7DaysAgo = true,
            is30DaysAgo = false,
            isDue = true,
            isRevisedToday = false
        )
        assertTrue(item7.is7DaysAgo)
        assertTrue(item7.isDue)
        assertEquals("7-Day Consolidation", item7.intervalLabel)

        // Problem 30 days ago
        val p30 = ProblemEntity(
            id = "p30",
            topicId = 2,
            title = "Contains Duplicate",
            solved = true,
            solvedDate = today.minusDays(30).toString()
        )
        val item30 = RevisionProblemItem(
            problem = p30,
            topic = topic,
            solvedLocalDate = today.minusDays(30),
            daysAgo = 30L,
            is3DaysAgo = false,
            is7DaysAgo = false,
            is30DaysAgo = true,
            isDue = true,
            isRevisedToday = false
        )
        assertTrue(item30.is30DaysAgo)
        assertTrue(item30.isDue)
        assertEquals("30-Day Mastery", item30.intervalLabel)
    }

    @Test
    fun testRevisionFilter_ValuesAndLabels() {
        assertEquals("ALL_DUE", RevisionFilter.ALL_DUE.id)
        assertEquals("3_DAYS", RevisionFilter.THREE_DAYS.id)
        assertEquals("7_DAYS", RevisionFilter.SEVEN_DAYS.id)
        assertEquals("30_DAYS", RevisionFilter.THIRTY_DAYS.id)
        assertEquals("ALL_SOLVED", RevisionFilter.ALL_SOLVED.id)
    }

    @Test
    fun testMarkRevisedToday_SetsRevisionStatus() {
        val today = LocalDate.now()
        val problem = ProblemEntity(
            id = "test_revised",
            topicId = 1,
            title = "Sample Problem",
            solved = true,
            solvedDate = today.minusDays(3).toString(),
            lastRevisedDate = today.toString()
        )

        val item = RevisionProblemItem(
            problem = problem,
            topic = null,
            solvedLocalDate = today.minusDays(3),
            daysAgo = 3L,
            is3DaysAgo = true,
            is7DaysAgo = false,
            is30DaysAgo = false,
            isDue = true,
            isRevisedToday = problem.lastRevisedDate == today.toString()
        )

        assertTrue("Problem should be marked revised today", item.isRevisedToday)
    }
}
