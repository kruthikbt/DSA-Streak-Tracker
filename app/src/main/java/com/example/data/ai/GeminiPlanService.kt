package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ProblemEntity
import com.example.data.model.TopicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SuggestedProblem(
    val title: String,
    val difficulty: String,
    val platform: String,
    val link: String = "",
    val reason: String = ""
)

sealed class DailyPlanResult {
    data class Success(val problems: List<SuggestedProblem>, val isFromAI: Boolean, val message: String? = null) : DailyPlanResult()
    data class Error(val message: String, val fallbackProblems: List<SuggestedProblem>) : DailyPlanResult()
}

class GeminiPlanService(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()
) {

    suspend fun generateDailyPlan(
        topic: TopicEntity,
        solvedProblems: List<ProblemEntity>,
        unsolvedProblems: List<ProblemEntity>,
        userDifficultyPreference: String = "Medium"
    ): DailyPlanResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val fallback = getCuratedFallbackProblems(topic, solvedProblems, userDifficultyPreference)
            return@withContext DailyPlanResult.Success(
                problems = fallback,
                isFromAI = false,
                message = "Using curated recommendations for $userDifficultyPreference difficulty. Configure your GEMINI_API_KEY in Secrets for adaptive AI suggestions."
            )
        }

        try {
            val solvedTitles = solvedProblems.map { "${it.title} (${it.difficulty})" }.joinToString(", ")
            val unsolvedTitles = unsolvedProblems.map { "${it.title} (${it.difficulty})" }.joinToString(", ")

            val prompt = """
                You are an expert Data Structures & Algorithms interview coach.
                The student is actively working on DSA Topic #${topic.order}: "${topic.name}".
                
                Student context:
                - Already solved problems in this topic: ${if (solvedTitles.isNotBlank()) solvedTitles else "None yet"}
                - Target practice difficulty level: $userDifficultyPreference
                - Existing unsolved problems queued: ${if (unsolvedTitles.isNotBlank()) unsolvedTitles else "None"}
                
                Suggest exactly 3 high-yield problems to practice today for "${topic.name}" matching the student's difficulty level ($userDifficultyPreference) that will strengthen their algorithmic pattern recognition.
                Do NOT suggest problems they have already solved.
                
                Return ONLY a JSON array matching this exact schema:
                [
                  {
                    "title": "Exact Problem Name",
                    "difficulty": "Easy|Medium|Hard",
                    "platform": "LeetCode|GFG|Codeforces|Other",
                    "link": "https://leetcode.com/problems/... (optional URL or empty)",
                    "reason": "1 concise sentence explaining the specific technique or invariant practiced"
                  }
                ]
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArray = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", partsArray)
                    }
                    put(contentObj)
                }
                put("contents", contentsArray)

                val generationConfig = JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.4)
                }
                put("generationConfig", generationConfig)
            }

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiPlanService", "API error: ${response.code} $responseBody")
                val fallback = getCuratedFallbackProblems(topic, solvedProblems, userDifficultyPreference)
                return@withContext DailyPlanResult.Success(
                    problems = fallback,
                    isFromAI = false,
                    message = "Gemini API returned HTTP ${response.code}. Provided curated $userDifficultyPreference problems."
                )
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            val cleanedText = rawText.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedArray = JSONArray(cleanedText)
            val suggestions = mutableListOf<SuggestedProblem>()

            for (i in 0 until minOf(3, parsedArray.length())) {
                val obj = parsedArray.getJSONObject(i)
                suggestions.add(
                    SuggestedProblem(
                        title = obj.optString("title", "DSA Problem ${i + 1}"),
                        difficulty = obj.optString("difficulty", userDifficultyPreference),
                        platform = obj.optString("platform", "LeetCode"),
                        link = obj.optString("link", ""),
                        reason = obj.optString("reason", "Targeted practice for ${topic.name}")
                    )
                )
            }

            if (suggestions.isNotEmpty()) {
                DailyPlanResult.Success(problems = suggestions, isFromAI = true)
            } else {
                val fallback = getCuratedFallbackProblems(topic, solvedProblems, userDifficultyPreference)
                DailyPlanResult.Success(problems = fallback, isFromAI = false)
            }
        } catch (e: Exception) {
            Log.e("GeminiPlanService", "Error calling Gemini", e)
            val fallback = getCuratedFallbackProblems(topic, solvedProblems, userDifficultyPreference)
            DailyPlanResult.Success(
                problems = fallback,
                isFromAI = false,
                message = "Could not reach Gemini (${e.localizedMessage ?: "Network error"}). Provided curated $userDifficultyPreference problems."
            )
        }
    }

    private fun getCuratedFallbackProblems(
        topic: TopicEntity,
        solvedProblems: List<ProblemEntity>,
        difficultyPreference: String
    ): List<SuggestedProblem> {
        val solvedNames = solvedProblems.map { it.title.lowercase() }.toSet()

        val allCurated = when (topic.order) {
            1 -> listOf(
                SuggestedProblem("Counting Bits", "Easy", "LeetCode", "https://leetcode.com/problems/counting-bits/", "Analyze linear bit population count vs logarithmic bounds."),
                SuggestedProblem("Pow(x, n)", "Medium", "LeetCode", "https://leetcode.com/problems/powx-n/", "Logarithmic O(log n) time complexity binary exponentiation."),
                SuggestedProblem("Find Peak Element", "Medium", "LeetCode", "https://leetcode.com/problems/find-peak-element/", "Proving asymptotic bounds on non-monotonic arrays."),
                SuggestedProblem("Median of Two Sorted Arrays", "Hard", "LeetCode", "https://leetcode.com/problems/median-of-two-sorted-arrays/", "Logarithmic partitioning across dual sorted streams.")
            )
            2 -> listOf(
                SuggestedProblem("Two Sum", "Easy", "LeetCode", "https://leetcode.com/problems/two-sum/", "Linear lookup using hash complement caching."),
                SuggestedProblem("Product of Array Except Self", "Medium", "LeetCode", "https://leetcode.com/problems/product-of-array-except-self/", "Prefix and suffix cumulative accumulation."),
                SuggestedProblem("Subarray Sums Divisible by K", "Medium", "LeetCode", "https://leetcode.com/problems/subarray-sums-divisible-by-k/", "Array prefix modulo hash arithmetic."),
                SuggestedProblem("Trapping Rain Water", "Hard", "LeetCode", "https://leetcode.com/problems/trapping-rain-water/", "Array precomputation and inward boundary heights.")
            )
            3 -> listOf(
                SuggestedProblem("Valid Anagram", "Easy", "LeetCode", "https://leetcode.com/problems/valid-anagram/", "Character frequency hashing and invariant comparison."),
                SuggestedProblem("Longest Palindromic Substring", "Medium", "LeetCode", "https://leetcode.com/problems/longest-palindromic-substring/", "Expand around center technique for palindromic symmetry."),
                SuggestedProblem("Group Shifted Strings", "Medium", "LeetCode", "https://leetcode.com/problems/group-shifted-strings/", "Canonical character distance hash representation."),
                SuggestedProblem("Minimum Window Substring", "Hard", "LeetCode", "https://leetcode.com/problems/minimum-window-substring/", "String sliding window with character frequency count.")
            )
            4 -> listOf(
                SuggestedProblem("Contains Duplicate", "Easy", "LeetCode", "https://leetcode.com/problems/contains-duplicate/", "Hash set existence lookup for immediate collisions."),
                SuggestedProblem("Subarray Sum Equals K", "Medium", "LeetCode", "https://leetcode.com/problems/subarray-sum-equals-k/", "Hash map cumulative sum occurrence lookup."),
                SuggestedProblem("Longest Consecutive Sequence", "Medium", "LeetCode", "https://leetcode.com/problems/longest-consecutive-sequence/", "O(n) set lookahead for sequence start points."),
                SuggestedProblem("Insert Delete GetRandom O(1)", "Medium", "LeetCode", "https://leetcode.com/problems/insert-delete-getrandom-o1/", "Hash map + array swap for true O(1) removals.")
            )
            5 -> listOf(
                SuggestedProblem("Valid Palindrome", "Easy", "LeetCode", "https://leetcode.com/problems/valid-palindrome/", "Dual pointers converging from ends towards center."),
                SuggestedProblem("Container With Most Water", "Medium", "LeetCode", "https://leetcode.com/problems/container-with-most-water/", "Shrinking boundary two-pointer invariant."),
                SuggestedProblem("3Sum", "Medium", "LeetCode", "https://leetcode.com/problems/3sum/", "Sorting + dual pointer collision for triplet sum."),
                SuggestedProblem("Trapping Rain Water", "Hard", "LeetCode", "https://leetcode.com/problems/trapping-rain-water/", "Inward dual pointers tracking max left/right walls.")
            )
            6 -> listOf(
                SuggestedProblem("Maximum Average Subarray I", "Easy", "LeetCode", "https://leetcode.com/problems/maximum-average-subarray-i/", "Fixed-size sliding window sum maintenance."),
                SuggestedProblem("Longest Substring Without Repeating Characters", "Medium", "LeetCode", "https://leetcode.com/problems/longest-substring-without-repeating-characters/", "Dynamic window expansion and fast jump left pointer."),
                SuggestedProblem("Longest Repeating Character Replacement", "Medium", "LeetCode", "https://leetcode.com/problems/longest-repeating-character-replacement/", "Sliding window frequency constraint validation."),
                SuggestedProblem("Sliding Window Maximum", "Hard", "LeetCode", "https://leetcode.com/problems/sliding-window-maximum/", "Monotonic deque sliding window optimal O(n).")
            )
            else -> listOf(
                SuggestedProblem("${topic.name} Starter", "Easy", "LeetCode", "", "Strengthen primary base patterns for ${topic.name}."),
                SuggestedProblem("${topic.name} Core Pattern", "Medium", "LeetCode", "", "Apply core algorithmic invariants and trade-offs."),
                SuggestedProblem("${topic.name} Advanced", "Hard", "LeetCode", "", "Deep application combining ${topic.name} with optimization.")
            )
        }

        // Prioritize requested difficulty and unsolved
        val unsolvedCurated = allCurated.filter { !solvedNames.contains(it.title.lowercase()) }
        val matchingDifficulty = unsolvedCurated.filter { it.difficulty.equals(difficultyPreference, ignoreCase = true) }
        val others = unsolvedCurated.filter { !it.difficulty.equals(difficultyPreference, ignoreCase = true) }

        val combined = (matchingDifficulty + others).take(3)
        return if (combined.size == 3) {
            combined
        } else {
            (unsolvedCurated + allCurated).distinctBy { it.title }.take(3)
        }
    }
}
