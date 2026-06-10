package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class FitTrackRepository(private val dao: FitTrackDao) {

    // Simulating user session state reactively
    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    suspend fun login(email: String, name: String): Boolean {
        val profile = dao.getUserProfileDirect(email)
        return if (profile != null) {
            _currentUserEmail.value = email
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentUserEmail.value = null
    }

    // User Profile
    fun getUserProfile(): Flow<UserProfile?> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(null) else dao.getUserProfileFlow(email)
    }

    suspend fun getUserProfileDirect(): UserProfile? = currentUserEmail.value?.let { dao.getUserProfileDirect(it) }

    suspend fun updateProfile(profile: UserProfile) = dao.insertProfile(profile)

    // Workout Plans
    fun getWorkoutPlans(): Flow<List<WorkoutPlan>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getWorkoutPlansFlow(email)
    }

    suspend fun addWorkoutPlan(plan: WorkoutPlan): Long = dao.insertWorkoutPlan(plan)
    suspend fun deleteWorkoutPlan(planId: Long) = dao.deleteWorkoutPlan(planId)

    // Exercises
    fun getExercises(): Flow<List<Exercise>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getExercisesFlow(email)
    }

    suspend fun addExercise(exercise: Exercise) = dao.insertExercise(exercise)

    // Workout Logs
    fun getWorkoutLogs(): Flow<List<WorkoutLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getWorkoutLogsFlow(email)
    }

    fun getAllSetsFlow(): Flow<List<ExerciseSetLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getAllSetsFlow(email)
    }

    suspend fun addWorkoutLog(log: WorkoutLog): Long = dao.insertWorkoutLog(log)
    suspend fun insertExerciseSets(sets: List<ExerciseSetLog>) = dao.insertExerciseSetLogs(sets)
    suspend fun getSetsForWorkout(workoutLogId: Long) = currentUserEmail.value?.let { dao.getSetsForWorkout(workoutLogId, it) } ?: emptyList()

    // Meals
    fun getMealsForDate(dateString: String): Flow<List<Meal>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getMealsForDateFlow(dateString, email)
    }

    fun getAllMeals(): Flow<List<Meal>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getAllMealsFlow(email)
    }

    suspend fun addMeal(meal: Meal) = dao.insertMeal(meal)
    suspend fun deleteMeal(mealId: Long) = dao.deleteMeal(mealId)

    // Water Logs
    fun getWaterLogsForDate(dateString: String): Flow<List<WaterLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getWaterLogsForDateFlow(dateString, email)
    }

    fun getAllWaterLogs(): Flow<List<WaterLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getAllWaterLogsFlow(email)
    }

    suspend fun addWaterLog(waterLog: WaterLog) = dao.insertWaterLog(waterLog)

    // Sleep Logs
    fun getSleepLogs(): Flow<List<SleepLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getSleepLogsFlow(email)
    }

    suspend fun addSleepLog(sleepLog: SleepLog) = dao.insertSleepLog(sleepLog)

    // Habits & Habit completion
    fun getHabits(): Flow<List<Habit>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getHabitsFlow(email)
    }

    suspend fun addHabit(habit: Habit): Long = dao.insertHabit(habit)
    suspend fun updateHabitStreak(habitId: Long, streak: Int) = dao.updateHabitStreak(habitId, streak)

    fun getHabitLogsForDate(dateString: String): Flow<List<HabitLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getHabitLogsForDateFlow(dateString, email)
    }

    fun getAllHabitLogs(): Flow<List<HabitLog>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getAllHabitLogsFlow(email)
    }

    suspend fun addHabitLog(log: HabitLog) = dao.insertHabitLog(log)
    suspend fun removeHabitLog(habitId: Long, dateString: String) = currentUserEmail.value?.let { dao.deleteHabitLog(habitId, dateString, it) }

    // Body Measurements
    fun getBodyMeasurements(): Flow<List<BodyMeasurement>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getBodyMeasurementsFlow(email)
    }

    suspend fun addBodyMeasurement(measurement: BodyMeasurement) = dao.insertBodyMeasurement(measurement)

    // Progress Photos
    fun getProgressPhotos(): Flow<List<ProgressPhoto>> = currentUserEmail.flatMapLatest { email ->
        if (email == null) flowOf(emptyList()) else dao.getProgressPhotosFlow(email)
    }

    suspend fun addProgressPhoto(photo: ProgressPhoto) = dao.insertProgressPhoto(photo)
}
