package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FitTrackDao {

    // User Profile
    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    fun getUserProfileFlow(email: String): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserProfileDirect(email: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    // Workout Plans
    @Query("SELECT * FROM workout_plans WHERE userEmail = :userEmail")
    fun getWorkoutPlansFlow(userEmail: String): Flow<List<WorkoutPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(plan: WorkoutPlan): Long

    @Query("DELETE FROM workout_plans WHERE id = :planId")
    suspend fun deleteWorkoutPlan(planId: Long)

    // Exercises
    @Query("SELECT * FROM exercises WHERE userEmail = :userEmail")
    fun getExercisesFlow(userEmail: String): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    // Workout Logs
    @Query("SELECT * FROM workout_logs WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getWorkoutLogsFlow(userEmail: String): Flow<List<WorkoutLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog): Long

    // Exercise Set Logs
    @Query("SELECT * FROM exercise_set_logs WHERE workoutLogId = :workoutLogId AND userEmail = :userEmail")
    suspend fun getSetsForWorkout(workoutLogId: Long, userEmail: String): List<ExerciseSetLog>

    @Query("SELECT * FROM exercise_set_logs WHERE userEmail = :userEmail")
    fun getAllSetsFlow(userEmail: String): Flow<List<ExerciseSetLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSetLogs(sets: List<ExerciseSetLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExerciseSetLog(set: ExerciseSetLog)

    // Meals
    @Query("SELECT * FROM meals WHERE dateString = :dateString AND userEmail = :userEmail ORDER BY timestamp ASC")
    fun getMealsForDateFlow(dateString: String, userEmail: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllMealsFlow(userEmail: String): Flow<List<Meal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMeal(mealId: Long)

    // Water Logs
    @Query("SELECT * FROM water_logs WHERE dateString = :dateString AND userEmail = :userEmail ORDER BY timestamp ASC")
    fun getWaterLogsForDateFlow(dateString: String, userEmail: String): Flow<List<WaterLog>>

    @Query("SELECT * FROM water_logs WHERE userEmail = :userEmail ORDER BY timestamp DESC")
    fun getAllWaterLogsFlow(userEmail: String): Flow<List<WaterLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLog)

    // Sleep Logs
    @Query("SELECT * FROM sleep_logs WHERE userEmail = :userEmail ORDER BY dateString DESC")
    fun getSleepLogsFlow(userEmail: String): Flow<List<SleepLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepLog(sleepLog: SleepLog)

    // Habits
    @Query("SELECT * FROM habits WHERE userEmail = :userEmail")
    fun getHabitsFlow(userEmail: String): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Query("UPDATE habits SET streak = :newStreak WHERE id = :habitId")
    suspend fun updateHabitStreak(habitId: Long, newStreak: Int)

    // Habit Logs (Completions)
    @Query("SELECT * FROM habit_logs WHERE dateString = :dateString AND userEmail = :userEmail")
    fun getHabitLogsForDateFlow(dateString: String, userEmail: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE userEmail = :userEmail")
    fun getAllHabitLogsFlow(userEmail: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND dateString = :dateString AND userEmail = :userEmail")
    suspend fun deleteHabitLog(habitId: Long, dateString: String, userEmail: String)

    // Body Measurements
    @Query("SELECT * FROM body_measurements WHERE userEmail = :userEmail ORDER BY dateString ASC")
    fun getBodyMeasurementsFlow(userEmail: String): Flow<List<BodyMeasurement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyMeasurement(measurement: BodyMeasurement)

    // Progress Photos
    @Query("SELECT * FROM progress_photos WHERE userEmail = :userEmail ORDER BY dateString DESC")
    fun getProgressPhotosFlow(userEmail: String): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhoto)
}
