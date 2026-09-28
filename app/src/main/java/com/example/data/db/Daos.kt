package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyLogEntity
import com.example.data.model.ProblemEntity
import com.example.data.model.StreakFreezeEntity
import com.example.data.model.TopicEntity
import com.example.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY `order` ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :id LIMIT 1")
    fun getTopicById(id: Int): Flow<TopicEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopics()
}

@Dao
interface ProblemDao {
    @Query("SELECT * FROM problems ORDER BY title ASC")
    fun getAllProblems(): Flow<List<ProblemEntity>>

    @Query("SELECT * FROM problems WHERE topicId = :topicId")
    fun getProblemsForTopic(topicId: Int): Flow<List<ProblemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblem(problem: ProblemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProblems(problems: List<ProblemEntity>)

    @Update
    suspend fun updateProblem(problem: ProblemEntity)

    @Query("DELETE FROM problems WHERE id = :id")
    suspend fun deleteProblemById(id: String)

    @Query("DELETE FROM problems")
    suspend fun deleteAllProblems()
}

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<DailyLogEntity>>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): DailyLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: DailyLogEntity)

    @Query("DELETE FROM daily_logs WHERE date = :date")
    suspend fun deleteLog(date: String)

    @Query("DELETE FROM daily_logs")
    suspend fun deleteAllLogs()
}

@Dao
interface StreakFreezeDao {
    @Query("SELECT * FROM streak_freezes ORDER BY date DESC")
    fun getAllFreezes(): Flow<List<StreakFreezeEntity>>

    @Query("SELECT * FROM streak_freezes WHERE monthKey = :monthKey")
    suspend fun getFreezesForMonth(monthKey: String): List<StreakFreezeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFreeze(freeze: StreakFreezeEntity)

    @Query("DELETE FROM streak_freezes WHERE date = :date")
    suspend fun deleteFreeze(date: String)

    @Query("DELETE FROM streak_freezes")
    suspend fun deleteAllFreezes()
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSync(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)
}
