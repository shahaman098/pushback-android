package com.pushback.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.pushback.app.domain.EventType
import com.pushback.app.domain.FailureMode
import com.pushback.app.domain.JobStatus
import com.pushback.app.domain.TaskTrack

class Converters {
    @TypeConverter fun jobStatusToString(v: JobStatus) = v.name
    @TypeConverter fun stringToJobStatus(v: String) = JobStatus.valueOf(v)
    @TypeConverter fun trackToString(v: TaskTrack) = v.name
    @TypeConverter fun stringToTrack(v: String) = TaskTrack.valueOf(v)
    @TypeConverter fun eventToString(v: EventType) = v.name
    @TypeConverter fun stringToEvent(v: String) = EventType.valueOf(v)
    @TypeConverter fun modeToString(v: FailureMode) = v.name
    @TypeConverter fun stringToMode(v: String) = FailureMode.valueOf(v)
}

@Database(
    entities = [
        JobEntity::class,
        TaskEntity::class,
        DayPlanEntity::class,
        DayReviewEntity::class,
        FocusSessionEntity::class,
        UsageSliceEntity::class,
        BehaviorEventEntity::class,
        PatternInsightEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class PushBackDatabase : RoomDatabase() {
    abstract fun jobs(): JobDao
    abstract fun tasks(): TaskDao
    abstract fun plans(): DayPlanDao
    abstract fun reviews(): DayReviewDao
    abstract fun focus(): FocusDao
    abstract fun usage(): UsageDao
    abstract fun events(): EventDao
    abstract fun insights(): InsightDao

    companion object {
        fun create(context: Context): PushBackDatabase =
            Room.databaseBuilder(context, PushBackDatabase::class.java, "pushback.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
