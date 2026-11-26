package com.pushback.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pushback.app.domain.EventType
import com.pushback.app.domain.TaskTrack
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    suspend fun all(): List<JobEntity>

    @Insert
    suspend fun insert(job: JobEntity): Long

    @Update
    suspend fun update(job: JobEntity)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM jobs WHERE appliedAt >= :from AND appliedAt <= :to")
    suspend fun appliedBetween(from: Long, to: Long): List<JobEntity>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isFrog DESC, createdAt DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY isFrog DESC, createdAt DESC")
    fun observeForDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDate = :date")
    suspend fun forDate(date: String): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE track = :track AND dueDate < :today AND doneAt IS NULL")
    suspend fun debt(today: String, track: TaskTrack = TaskTrack.REQUIRED): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("UPDATE tasks SET isFrog = 0 WHERE dueDate = :date")
    suspend fun clearFrog(date: String)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface DayPlanDao {
    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    fun observe(date: String): Flow<DayPlanEntity?>

    @Query("SELECT * FROM day_plans WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DayPlanEntity?

    @Query("SELECT * FROM day_plans WHERE date >= :from ORDER BY date DESC")
    suspend fun since(from: String): List<DayPlanEntity>

    @Query("SELECT * FROM day_plans ORDER BY date DESC")
    fun observeAll(): Flow<List<DayPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(plan: DayPlanEntity): Long

    @Update
    suspend fun update(plan: DayPlanEntity)
}

@Dao
interface DayReviewDao {
    @Query("SELECT * FROM day_reviews WHERE date = :date LIMIT 1")
    fun observe(date: String): Flow<DayReviewEntity?>

    @Query("SELECT * FROM day_reviews ORDER BY date DESC")
    fun observeAll(): Flow<List<DayReviewEntity>>

    @Query("SELECT * FROM day_reviews WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DayReviewEntity?

    @Query("SELECT * FROM day_reviews WHERE date >= :from")
    suspend fun since(from: String): List<DayReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(review: DayReviewEntity): Long
}

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions WHERE endedAt IS NULL ORDER BY startedAt DESC LIMIT 1")
    fun observeActive(): Flow<FocusSessionEntity?>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC LIMIT 40")
    suspend fun recent(): List<FocusSessionEntity>

    @Insert
    suspend fun insert(session: FocusSessionEntity): Long

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun get(id: Long): FocusSessionEntity?
}

@Dao
interface UsageDao {
    @Insert
    suspend fun insert(slice: UsageSliceEntity)

    @Query("SELECT * FROM usage_slices WHERE timestamp >= :from")
    suspend fun since(from: Long): List<UsageSliceEntity>
}

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: BehaviorEventEntity)

    @Query("SELECT * FROM behavior_events WHERE timestamp >= :from ORDER BY timestamp DESC")
    suspend fun since(from: Long): List<BehaviorEventEntity>

    @Query("SELECT * FROM behavior_events WHERE type = :type AND timestamp >= :from")
    suspend fun ofTypeSince(type: EventType, from: Long): List<BehaviorEventEntity>
}

@Dao
interface InsightDao {
    @Query("SELECT * FROM pattern_insights ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PatternInsightEntity>>

    @Query("SELECT * FROM pattern_insights WHERE acceptedAt IS NULL AND rejectedAt IS NULL ORDER BY createdAt DESC LIMIT 1")
    fun observePending(): Flow<PatternInsightEntity?>

    @Query("SELECT * FROM pattern_insights ORDER BY createdAt DESC LIMIT 1")
    suspend fun latest(): PatternInsightEntity?

    @Insert
    suspend fun insert(insight: PatternInsightEntity): Long

    @Update
    suspend fun update(insight: PatternInsightEntity)

    @Query("SELECT COUNT(*) FROM pattern_insights WHERE createdAt >= :from")
    suspend fun countSince(from: Long): Int
}
