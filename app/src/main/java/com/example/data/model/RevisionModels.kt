package com.example.data.model

import java.time.LocalDate

data class RevisionProblemItem(
    val problem: ProblemEntity,
    val topic: TopicEntity?,
    val solvedLocalDate: LocalDate,
    val daysAgo: Long,
    val is3DaysAgo: Boolean,
    val is7DaysAgo: Boolean,
    val is30DaysAgo: Boolean,
    val isDue: Boolean,
    val isRevisedToday: Boolean
) {
    val intervalLabel: String
        get() = when {
            is3DaysAgo -> "3-Day Recall"
            is7DaysAgo -> "7-Day Consolidation"
            is30DaysAgo -> "30-Day Mastery"
            daysAgo in 3L..6L -> "3d Interval"
            daysAgo in 7L..29L -> "7d Interval"
            daysAgo >= 30L -> "30d+ Interval"
            else -> "$daysAgo days ago"
        }
}

enum class RevisionFilter(val id: String, val label: String) {
    ALL_DUE("ALL_DUE", "All Due"),
    THREE_DAYS("3_DAYS", "3 Days Ago"),
    SEVEN_DAYS("7_DAYS", "7 Days Ago"),
    THIRTY_DAYS("30_DAYS", "30 Days Ago"),
    ALL_SOLVED("ALL_SOLVED", "All Solved")
}
