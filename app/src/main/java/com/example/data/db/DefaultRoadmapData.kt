package com.example.data.db

import com.example.data.model.ProblemEntity
import com.example.data.model.SubtopicItem
import com.example.data.model.TopicEntity
import java.util.UUID

object DefaultRoadmapData {

    data class TopicDefinition(
        val order: Int,
        val name: String,
        val subtopics: List<String>,
        val initialProblems: List<ProblemSeed>
    )

    data class ProblemSeed(
        val title: String,
        val difficulty: String,
        val platform: String,
        val link: String = "",
        val solved: Boolean = false
    )

    val TOPIC_DEFINITIONS = listOf(
        TopicDefinition(
            order = 1,
            name = "Time & Space Complexity (Big-O basics)",
            subtopics = listOf(
                "Asymptotic Notations (O, Ω, Θ)",
                "Worst, Average, and Best Case",
                "Time Complexity Analysis of Loops",
                "Space Complexity & Auxiliary Space",
                "Complexity Classes: O(1) to O(2^n)"
            ),
            initialProblems = listOf(
                ProblemSeed("Count Operations & Amortized Analysis", "Easy", "Other", "https://leetcode.com"),
                ProblemSeed("Analyze Recursive Space Complexity", "Medium", "Other", "")
            )
        ),
        TopicDefinition(
            order = 2,
            name = "Arrays",
            subtopics = listOf(
                "Array Memory & Cache Locality",
                "Basic Operations (Insert, Delete, Search)",
                "Kadane's Algorithm for Maximum Subarray",
                "Dutch National Flag (0s, 1s, 2s)",
                "Array Rotations and Inversions",
                "Matrix / 2D Arrays Traversal"
            ),
            initialProblems = listOf(
                ProblemSeed("Two Sum", "Easy", "LeetCode", "https://leetcode.com/problems/two-sum/"),
                ProblemSeed("Best Time to Buy and Sell Stock", "Easy", "LeetCode", "https://leetcode.com/problems/best-time-to-buy-and-sell-stock/"),
                ProblemSeed("Maximum Subarray", "Medium", "LeetCode", "https://leetcode.com/problems/maximum-subarray/")
            )
        ),
        TopicDefinition(
            order = 3,
            name = "Strings",
            subtopics = listOf(
                "String Immutability & Memory Internals",
                "Valid Palindromes & Transformations",
                "Valid Anagrams & Frequency Hash",
                "Longest Common Prefix",
                "Pattern Matching Basics (KMP & Rabin-Karp)"
            ),
            initialProblems = listOf(
                ProblemSeed("Valid Anagram", "Easy", "LeetCode", "https://leetcode.com/problems/valid-anagram/"),
                ProblemSeed("Valid Palindrome", "Easy", "LeetCode", "https://leetcode.com/problems/valid-palindrome/"),
                ProblemSeed("Longest Common Prefix", "Easy", "LeetCode", "https://leetcode.com/problems/longest-common-prefix/")
            )
        ),
        TopicDefinition(
            order = 4,
            name = "Hashing (HashMap / HashSet)",
            subtopics = listOf(
                "Hash Tables, Collisions, & Load Factor",
                "Frequency Counting & Mapping",
                "Subarray Sum Equals K",
                "Group Anagrams by Signature",
                "Longest Consecutive Sequence"
            ),
            initialProblems = listOf(
                ProblemSeed("Contains Duplicate", "Easy", "LeetCode", "https://leetcode.com/problems/contains-duplicate/"),
                ProblemSeed("Group Anagrams", "Medium", "LeetCode", "https://leetcode.com/problems/group-anagrams/"),
                ProblemSeed("Longest Consecutive Sequence", "Medium", "LeetCode", "https://leetcode.com/problems/longest-consecutive-sequence/")
            )
        ),
        TopicDefinition(
            order = 5,
            name = "Two Pointers",
            subtopics = listOf(
                "Opposite Direction Pointers",
                "Fast & Slow Pointers (Cycle Detection)",
                "Merging Two Sorted Lists/Arrays",
                "Container With Most Water",
                "Trapping Rain Water"
            ),
            initialProblems = listOf(
                ProblemSeed("Two Sum II - Input Array Is Sorted", "Medium", "LeetCode", "https://leetcode.com/problems/two-sum-ii-input-array-is-sorted/"),
                ProblemSeed("3Sum", "Medium", "LeetCode", "https://leetcode.com/problems/3sum/"),
                ProblemSeed("Container With Most Water", "Medium", "LeetCode", "https://leetcode.com/problems/container-with-most-water/")
            )
        ),
        TopicDefinition(
            order = 6,
            name = "Sliding Window",
            subtopics = listOf(
                "Fixed-size Window Maximum/Minimum",
                "Variable-size Dynamic Window",
                "Longest Substring Without Repeating",
                "Minimum Window Substring",
                "Sliding Window Maximum (Deque/Monoqueue)"
            ),
            initialProblems = listOf(
                ProblemSeed("Longest Substring Without Repeating Characters", "Medium", "LeetCode", "https://leetcode.com/problems/longest-substring-without-repeating-characters/"),
                ProblemSeed("Longest Repeating Character Replacement", "Medium", "LeetCode", "https://leetcode.com/problems/longest-repeating-character-replacement/"),
                ProblemSeed("Minimum Window Substring", "Hard", "LeetCode", "https://leetcode.com/problems/minimum-window-substring/")
            )
        ),
        TopicDefinition(
            order = 7,
            name = "Prefix Sum",
            subtopics = listOf(
                "1D Cumulative Sum Array",
                "2D Range Sum Query Matrix",
                "Subarray Sum Divisible by K",
                "Difference Array / Range Update Technique",
                "Equilibrium Index & Pivot Finding"
            ),
            initialProblems = listOf(
                ProblemSeed("Range Sum Query - Immutable", "Easy", "LeetCode", "https://leetcode.com/problems/range-sum-query-immutable/"),
                ProblemSeed("Subarray Sum Equals K", "Medium", "LeetCode", "https://leetcode.com/problems/subarray-sum-equals-k/"),
                ProblemSeed("Product of Array Except Self", "Medium", "LeetCode", "https://leetcode.com/problems/product-of-array-except-self/")
            )
        ),
        TopicDefinition(
            order = 8,
            name = "Binary Search",
            subtopics = listOf(
                "Standard Binary Search & Invariants",
                "Lower Bound and Upper Bound",
                "Search in Rotated Sorted Array",
                "Binary Search on Answer Space (Aggressive Cows)",
                "Median of Two Sorted Arrays"
            ),
            initialProblems = listOf(
                ProblemSeed("Binary Search", "Easy", "LeetCode", "https://leetcode.com/problems/binary-search/"),
                ProblemSeed("Search in Rotated Sorted Array", "Medium", "LeetCode", "https://leetcode.com/problems/search-in-rotated-sorted-array/"),
                ProblemSeed("Koko Eating Bananas", "Medium", "LeetCode", "https://leetcode.com/problems/koko-eating-bananas/")
            )
        ),
        TopicDefinition(
            order = 9,
            name = "Sorting Algorithms",
            subtopics = listOf(
                "Merge Sort & Divide-and-Conquer",
                "Quick Sort & Quick Select (Kth Element)",
                "Heap Sort Algorithm",
                "Non-comparison Sorts (Counting & Radix)",
                "Custom Comparators & Inversion Counting"
            ),
            initialProblems = listOf(
                ProblemSeed("Sort an Array", "Medium", "LeetCode", "https://leetcode.com/problems/sort-an-array/"),
                ProblemSeed("Kth Largest Element in an Array", "Medium", "LeetCode", "https://leetcode.com/problems/kth-largest-element-in-an-array/")
            )
        ),
        TopicDefinition(
            order = 10,
            name = "Recursion",
            subtopics = listOf(
                "Base Cases & Call Stack Recursion Tree",
                "Tail Call Optimization Concepts",
                "Tower of Hanoi",
                "Subset Generation",
                "Divide and Conquer Master Theorem"
            ),
            initialProblems = listOf(
                ProblemSeed("Climbing Stairs (Recursive)", "Easy", "LeetCode", "https://leetcode.com/problems/climbing-stairs/"),
                ProblemSeed("Pow(x, n)", "Medium", "LeetCode", "https://leetcode.com/problems/powx-n/")
            )
        ),
        TopicDefinition(
            order = 11,
            name = "Backtracking",
            subtopics = listOf(
                "State-space Tree Exploration & Pruning",
                "N-Queens Problem",
                "Sudoku Solver",
                "Permutations & Combinations",
                "Word Search on 2D Board"
            ),
            initialProblems = listOf(
                ProblemSeed("Subsets", "Medium", "LeetCode", "https://leetcode.com/problems/subsets/"),
                ProblemSeed("Combination Sum", "Medium", "LeetCode", "https://leetcode.com/problems/combination-sum/"),
                ProblemSeed("N-Queens", "Hard", "LeetCode", "https://leetcode.com/problems/n-queens/")
            )
        ),
        TopicDefinition(
            order = 12,
            name = "Linked List",
            subtopics = listOf(
                "Singly & Doubly Linked List Pointers",
                "Reverse Linked List (Iterative & Recursive)",
                "Middle of Linked List (Floyd)",
                "Merge Two Sorted Lists",
                "Remove Nth Node from End of List",
                "LRU Cache Architecture"
            ),
            initialProblems = listOf(
                ProblemSeed("Reverse Linked List", "Easy", "LeetCode", "https://leetcode.com/problems/reverse-linked-list/"),
                ProblemSeed("Merge Two Sorted Lists", "Easy", "LeetCode", "https://leetcode.com/problems/merge-two-sorted-lists/"),
                ProblemSeed("Linked List Cycle", "Easy", "LeetCode", "https://leetcode.com/problems/linked-list-cycle/"),
                ProblemSeed("LRU Cache", "Medium", "LeetCode", "https://leetcode.com/problems/lru-cache/")
            )
        ),
        TopicDefinition(
            order = 13,
            name = "Stack",
            subtopics = listOf(
                "LIFO Principle & Array/Node Implementation",
                "Valid Parentheses Matching",
                "Min Stack Design with Auxiliary Stack",
                "Evaluate Reverse Polish Notation",
                "Infix, Prefix, and Postfix Transformations"
            ),
            initialProblems = listOf(
                ProblemSeed("Valid Parentheses", "Easy", "LeetCode", "https://leetcode.com/problems/valid-parentheses/"),
                ProblemSeed("Min Stack", "Medium", "LeetCode", "https://leetcode.com/problems/min-stack/"),
                ProblemSeed("Evaluate Reverse Polish Notation", "Medium", "LeetCode", "https://leetcode.com/problems/evaluate-reverse-polish-notation/")
            )
        ),
        TopicDefinition(
            order = 14,
            name = "Queue & Deque",
            subtopics = listOf(
                "FIFO Principle & Circular Buffer Implementation",
                "Implement Queue using Stacks",
                "Implement Stack using Queues",
                "Double-Ended Queue (Deque) Operations",
                "Sliding Window using Deque"
            ),
            initialProblems = listOf(
                ProblemSeed("Implement Queue using Stacks", "Easy", "LeetCode", "https://leetcode.com/problems/implement-queue-using-stacks/"),
                ProblemSeed("Design Circular Queue", "Medium", "LeetCode", "https://leetcode.com/problems/design-circular-queue/")
            )
        ),
        TopicDefinition(
            order = 15,
            name = "Monotonic Stack / Queue",
            subtopics = listOf(
                "Monotonic Increasing / Decreasing Concept",
                "Next Greater Element I & II",
                "Daily Temperatures",
                "Largest Rectangle in Histogram",
                "Maximal Rectangle in Binary Matrix"
            ),
            initialProblems = listOf(
                ProblemSeed("Next Greater Element I", "Easy", "LeetCode", "https://leetcode.com/problems/next-greater-element-i/"),
                ProblemSeed("Daily Temperatures", "Medium", "LeetCode", "https://leetcode.com/problems/daily-temperatures/"),
                ProblemSeed("Largest Rectangle in Histogram", "Hard", "LeetCode", "https://leetcode.com/problems/largest-rectangle-in-histogram/")
            )
        ),
        TopicDefinition(
            order = 16,
            name = "Heap / Priority Queue",
            subtopics = listOf(
                "Complete Binary Trees & Array Indexing",
                "Heapify Up & Down (O(n) Build Heap)",
                "Top K Frequent Elements",
                "Kth Largest Element in a Stream",
                "Find Median from Data Stream (Two Heaps)"
            ),
            initialProblems = listOf(
                ProblemSeed("Kth Largest Element in a Stream", "Easy", "LeetCode", "https://leetcode.com/problems/kth-largest-element-in-a-stream/"),
                ProblemSeed("Top K Frequent Elements", "Medium", "LeetCode", "https://leetcode.com/problems/top-k-frequent-elements/"),
                ProblemSeed("Find Median from Data Stream", "Hard", "LeetCode", "https://leetcode.com/problems/find-median-from-data-stream/")
            )
        ),
        TopicDefinition(
            order = 17,
            name = "Trees (Binary Tree, Traversals)",
            subtopics = listOf(
                "Node Structure & Tree Terminology",
                "DFS Traversals (Pre, In, Post-order)",
                "BFS / Level Order Traversal with Queue",
                "Maximum Depth & Diameter of Binary Tree",
                "Lowest Common Ancestor (LCA)",
                "Subtree of Another Tree & Invert Tree"
            ),
            initialProblems = listOf(
                ProblemSeed("Invert Binary Tree", "Easy", "LeetCode", "https://leetcode.com/problems/invert-binary-tree/"),
                ProblemSeed("Maximum Depth of Binary Tree", "Easy", "LeetCode", "https://leetcode.com/problems/maximum-depth-of-binary-tree/"),
                ProblemSeed("Binary Tree Level Order Traversal", "Medium", "LeetCode", "https://leetcode.com/problems/binary-tree-level-order-traversal/"),
                ProblemSeed("Lowest Common Ancestor of a Binary Tree", "Medium", "LeetCode", "https://leetcode.com/problems/lowest-common-ancestor-of-a-binary-tree/")
            )
        ),
        TopicDefinition(
            order = 18,
            name = "Binary Search Tree",
            subtopics = listOf(
                "BST Invariant & Inorder Traversal Sorting",
                "Search, Insert, and Delete in BST",
                "Validate Binary Search Tree",
                "Kth Smallest Element in BST",
                "Convert Sorted Array to Balanced BST"
            ),
            initialProblems = listOf(
                ProblemSeed("Validate Binary Search Tree", "Medium", "LeetCode", "https://leetcode.com/problems/validate-binary-search-tree/"),
                ProblemSeed("Kth Smallest Element in a BST", "Medium", "LeetCode", "https://leetcode.com/problems/kth-smallest-element-in-a-bst/"),
                ProblemSeed("Delete Node in a BST", "Medium", "LeetCode", "https://leetcode.com/problems/delete-node-in-a-bst/")
            )
        ),
        TopicDefinition(
            order = 19,
            name = "Graphs (BFS, DFS)",
            subtopics = listOf(
                "Adjacency Matrix vs Adjacency List",
                "Breadth-First Search (Shortest Path Unweighted)",
                "Depth-First Search (Components & Backtracking)",
                "Number of Islands & Flood Fill",
                "Cycle Detection (Directed vs Undirected)",
                "Bipartite Graph Verification"
            ),
            initialProblems = listOf(
                ProblemSeed("Number of Islands", "Medium", "LeetCode", "https://leetcode.com/problems/number-of-islands/"),
                ProblemSeed("Clone Graph", "Medium", "LeetCode", "https://leetcode.com/problems/clone-graph/"),
                ProblemSeed("Rotting Oranges", "Medium", "LeetCode", "https://leetcode.com/problems/rotting-oranges/")
            )
        ),
        TopicDefinition(
            order = 20,
            name = "Advanced Graphs (Dijkstra, Topological Sort, Union-Find)",
            subtopics = listOf(
                "Disjoint Set Union (DSU / Union-Find) with Rank & Path Compression",
                "Kruskal's & Prim's Minimum Spanning Tree",
                "Dijkstra's Algorithm (Priority Queue)",
                "Bellman-Ford & Negative Weight Cycles",
                "Topological Sort (Kahn's & DFS Post-order)"
            ),
            initialProblems = listOf(
                ProblemSeed("Course Schedule", "Medium", "LeetCode", "https://leetcode.com/problems/course-schedule/"),
                ProblemSeed("Network Delay Time", "Medium", "LeetCode", "https://leetcode.com/problems/network-delay-time/"),
                ProblemSeed("Redundant Connection", "Medium", "LeetCode", "https://leetcode.com/problems/redundant-connection/")
            )
        ),
        TopicDefinition(
            order = 21,
            name = "Trie",
            subtopics = listOf(
                "Trie Node Array vs HashMap Children",
                "Insert, Search, and StartsWith Prefix",
                "Word Break & Auto-complete Design",
                "Word Search II (Trie + Backtracking)",
                "Maximum XOR of Two Numbers in Array"
            ),
            initialProblems = listOf(
                ProblemSeed("Implement Trie (Prefix Tree)", "Medium", "LeetCode", "https://leetcode.com/problems/implement-trie-prefix-tree/"),
                ProblemSeed("Design Add and Search Words Data Structure", "Medium", "LeetCode", "https://leetcode.com/problems/design-add-and-search-words-data-structure/"),
                ProblemSeed("Word Search II", "Hard", "LeetCode", "https://leetcode.com/problems/word-search-ii/")
            )
        ),
        TopicDefinition(
            order = 22,
            name = "Greedy",
            subtopics = listOf(
                "Greedy Choice Property & Optimal Substructure",
                "Activity Selection & Interval Scheduling",
                "Jump Game I & II Reachability",
                "Gas Station Circular Route",
                "Task Scheduler with Cooldown"
            ),
            initialProblems = listOf(
                ProblemSeed("Jump Game", "Medium", "LeetCode", "https://leetcode.com/problems/jump-game/"),
                ProblemSeed("Gas Station", "Medium", "LeetCode", "https://leetcode.com/problems/gas-station/"),
                ProblemSeed("Hand of Straights", "Medium", "LeetCode", "https://leetcode.com/problems/hand-of-straights/")
            )
        ),
        TopicDefinition(
            order = 23,
            name = "Dynamic Programming (1D, 2D, Knapsack, LIS, LCS)",
            subtopics = listOf(
                "Memoization (Top-down) vs Tabulation (Bottom-up)",
                "1D DP: Climbing Stairs & House Robber",
                "0/1 Knapsack & Unbounded Knapsack Patterns",
                "Longest Increasing Subsequence (LIS)",
                "Longest Common Subsequence (LCS)",
                "Coin Change I & II Variations",
                "Edit Distance & Matrix Grid Paths"
            ),
            initialProblems = listOf(
                ProblemSeed("Climbing Stairs", "Easy", "LeetCode", "https://leetcode.com/problems/climbing-stairs/"),
                ProblemSeed("House Robber", "Medium", "LeetCode", "https://leetcode.com/problems/house-robber/"),
                ProblemSeed("Coin Change", "Medium", "LeetCode", "https://leetcode.com/problems/coin-change/"),
                ProblemSeed("Longest Increasing Subsequence", "Medium", "LeetCode", "https://leetcode.com/problems/longest-increasing-subsequence/")
            )
        ),
        TopicDefinition(
            order = 24,
            name = "Bit Manipulation",
            subtopics = listOf(
                "Bitwise Operators (&, |, ^, ~, <<, >>)",
                "Checking Powers of Two & Bitmasks",
                "Brian Kernighan's Algorithm for Set Bits",
                "Single Number I, II, and III",
                "Bitwise Subset Generation"
            ),
            initialProblems = listOf(
                ProblemSeed("Single Number", "Easy", "LeetCode", "https://leetcode.com/problems/single-number/"),
                ProblemSeed("Number of 1 Bits", "Easy", "LeetCode", "https://leetcode.com/problems/number-of-1-bits/"),
                ProblemSeed("Counting Bits", "Easy", "LeetCode", "https://leetcode.com/problems/counting-bits/")
            )
        ),
        TopicDefinition(
            order = 25,
            name = "Intervals",
            subtopics = listOf(
                "Sorting Intervals by Start/End Times",
                "Merge Overlapping Intervals",
                "Insert Interval into Sorted List",
                "Non-overlapping Intervals Count",
                "Meeting Rooms I & II (Min Rooms with Min-Heap)"
            ),
            initialProblems = listOf(
                ProblemSeed("Merge Intervals", "Medium", "LeetCode", "https://leetcode.com/problems/merge-intervals/"),
                ProblemSeed("Insert Interval", "Medium", "LeetCode", "https://leetcode.com/problems/insert-interval/"),
                ProblemSeed("Non-overlapping Intervals", "Medium", "LeetCode", "https://leetcode.com/problems/non-overlapping-intervals/")
            )
        )
    )

    fun createInitialTopics(): List<TopicEntity> {
        return TOPIC_DEFINITIONS.mapIndexed { index, def ->
            val subtopicItems = def.subtopics.map { SubtopicItem(title = it, completed = false) }
            val initialStatus = if (index == 0) TopicEntity.STATUS_IN_PROGRESS else TopicEntity.STATUS_NOT_STARTED
            TopicEntity(
                id = def.order,
                order = def.order,
                name = def.name,
                status = initialStatus
            ).withSubtopics(subtopicItems)
        }
    }

    fun createInitialProblems(): List<ProblemEntity> {
        val problems = mutableListOf<ProblemEntity>()
        val today = java.time.LocalDate.now()
        val date3DaysAgo = today.minusDays(3).toString()
        val date7DaysAgo = today.minusDays(7).toString()
        val date30DaysAgo = today.minusDays(30).toString()

        TOPIC_DEFINITIONS.forEach { def ->
            def.initialProblems.forEach { seed ->
                val isSolvedSeed = when (seed.title) {
                    "Contains Duplicate" -> true
                    "Two Sum II - Input Array Is Sorted" -> true
                    "Valid Palindrome" -> true
                    "Two Sum" -> true
                    else -> seed.solved
                }

                val solvedDateSeed = when (seed.title) {
                    "Contains Duplicate" -> date3DaysAgo
                    "Two Sum II - Input Array Is Sorted" -> date3DaysAgo
                    "Valid Palindrome" -> date7DaysAgo
                    "Two Sum" -> date30DaysAgo
                    else -> if (seed.solved) date7DaysAgo else null
                }

                val notesSeed = when (seed.title) {
                    "Contains Duplicate" -> "Hash set for O(1) membership lookup. Invariant: seen elements are unique."
                    "Two Sum II - Input Array Is Sorted" -> "Opposite pointers converging. Sorted monotonic property."
                    "Valid Palindrome" -> "Inward dual pointers skipping non-alphanumerics. Symmetrical equality check."
                    "Two Sum" -> "One-pass hash map caching target - num complement. O(n) time and O(n) space."
                    else -> ""
                }

                problems.add(
                    ProblemEntity(
                        id = UUID.randomUUID().toString(),
                        topicId = def.order,
                        title = seed.title,
                        difficulty = seed.difficulty,
                        platform = seed.platform,
                        link = seed.link,
                        solved = isSolvedSeed,
                        solvedDate = solvedDateSeed,
                        revisionNotes = notesSeed
                    )
                )
            }
        }
        return problems
    }
}
