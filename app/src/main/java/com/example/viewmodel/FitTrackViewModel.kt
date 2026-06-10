package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class FitTrackViewModel(private val repository: FitTrackRepository) : ViewModel() {

    // Current Date String for today
    private val _selectedDate = MutableStateFlow(getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    // Auth state flow
    val currentUserEmail: StateFlow<String?> = repository.currentUserEmail
    val userProfile: StateFlow<UserProfile?> = repository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Workouts
    val workoutPlans: StateFlow<List<WorkoutPlan>> = repository.getWorkoutPlans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val exercises: StateFlow<List<Exercise>> = repository.getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.getWorkoutLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSets: StateFlow<List<ExerciseSetLog>> = repository.getAllSetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Meals on the *selected date*
    val mealsForSelectedDate: StateFlow<List<Meal>> = selectedDate
        .flatMapLatest { date -> repository.getMealsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeals: StateFlow<List<Meal>> = repository.getAllMeals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Water tracker on the *selected date*
    val waterLogsForSelectedDate: StateFlow<List<WaterLog>> = selectedDate
        .flatMapLatest { date -> repository.getWaterLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sleep records
    val sleepLogs: StateFlow<List<SleepLog>> = repository.getSleepLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Habits & Habit Completions for the selected date
    val habits: StateFlow<List<Habit>> = repository.getHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitLogsForSelectedDate: StateFlow<List<HabitLog>> = selectedDate
        .flatMapLatest { date -> repository.getHabitLogsForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Body Measurements
    val bodyMeasurements: StateFlow<List<BodyMeasurement>> = repository.getBodyMeasurements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Progress Photos
    val progressPhotos: StateFlow<List<ProgressPhoto>> = repository.getProgressPhotos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // System Messages/Banners
    private val _message = MutableSharedFlow<String>()
    val message: SharedFlow<String> = _message.asSharedFlow()

    // Interactive Theme Setting (User Preferences)
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- Authentication Actions ---
    fun login(email: String) {
        viewModelScope.launch {
            val success = repository.login(email, "")
            if (success) {
                _message.emit("Welcome back!")
            } else {
                _message.emit("Account not found. Please sign up.")
            }
        }
    }

    fun signUpAndLogin(email: String, name: String, goal: String) {
        viewModelScope.launch {
            repository.login(email, name)
            // Create a custom seed profile for this user
            val profile = UserProfile(
                email = email,
                name = name,
                goalType = goal,
                weightKg = 70f,
                heightCm = 175f,
                targetWeightKg = if (goal == "Weight Loss") 65f else 75f,
                dailyCalorieTarget = if (goal == "Weight Loss") 1800 else if (goal == "Muscle Gain") 2500 else 2100,
                dailyProteinTarget = if (goal == "Weight Loss") 130 else if (goal == "Muscle Gain") 160 else 140,
                dailyWaterTargetMl = 2500
            )
            repository.updateProfile(profile)
            _message.emit("Welcome to FitTrack Pro, $name!")
        }
    }

    fun googleSignIn() {
        signUpAndLogin("google.user@gmail.com", "Google Champion", "Maintenance")
    }

    fun logout() {
        repository.logout()
    }

    fun updateProfileDetails(
        name: String,
        goal: String,
        height: Float,
        weight: Float,
        targetWeight: Float,
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        water: Int
    ) {
        viewModelScope.launch {
            val email = currentUserEmail.value ?: return@launch
            userProfile.value?.let { currentProfile ->
                val updated = currentProfile.copy(
                    name = name,
                    goalType = goal,
                    heightCm = height,
                    weightKg = weight,
                    targetWeightKg = targetWeight,
                    dailyCalorieTarget = calories,
                    dailyProteinTarget = protein,
                    dailyCarbsTarget = carbs,
                    dailyFatTarget = fat,
                    dailyWaterTargetMl = water
                )
                repository.updateProfile(updated)
                // Log body measurement change for the progress chart
                repository.addBodyMeasurement(
                    BodyMeasurement(
                        userEmail = email,
                        dateString = getTodayDateString(),
                        weightKg = weight,
                        chestCm = 101.8f, // preserve defaults or use existing latest if any
                        waistCm = 80.5f,
                        bicepsCm = 37.5f,
                        thighCm = 56.8f
                    )
                )
                _message.emit("Profile saved successfully!")
            }
        }
    }

    // --- Workout Logging ---
    fun addCustomWorkoutPlan(title: String, description: String, type: String) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addWorkoutPlan(WorkoutPlan(userEmail = email, title = title, description = description, type = type))
            _message.emit("Workout plan '$title' added!")
        }
    }

    fun logWorkoutSession(planTitle: String, durationMin: Int, notes: String, sets: List<ExerciseSetLog>) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            val logId = repository.addWorkoutLog(
                WorkoutLog(
                    userEmail = email,
                    planId = 0,
                    planTitle = planTitle,
                    dateString = selectedDate.value,
                    durationMinutes = durationMin,
                    notes = notes
                )
            )
            // Associate sets with this log and user
            val finalSets = sets.map { it.copy(workoutLogId = logId, userEmail = email) }
            repository.insertExerciseSets(finalSets)
            
            // Add experience points for executing workout!
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(points = profile.points + 50))
            }

            _message.emit("Workout '$planTitle' logged successfully! +50 XP")
        }
    }

    // --- Nutrition Logging ---
    fun logMeal(type: String, title: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addMeal(
                Meal(
                    userEmail = email,
                    mealType = type,
                    title = title,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    dateString = selectedDate.value
                )
            )
            // award XP for tracking!
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(points = profile.points + 10))
            }
            _message.emit("Logged $type: $title! +10 XP")
        }
    }

    fun deleteMeal(mealId: Long) {
        viewModelScope.launch {
            repository.deleteMeal(mealId)
            _message.emit("Meal removed")
        }
    }

    // --- Hydration (Water) Logging ---
    fun quickAddWater(amountMl: Int) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addWaterLog(WaterLog(userEmail = email, amountMl = amountMl, dateString = selectedDate.value))
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(points = profile.points + 5))
            }
            _message.emit("Added $amountMl ml water! +5 XP")
        }
    }

    // --- Sleep Logging ---
    fun logSleep(hours: Float, rating: Int, notes: String) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addSleepLog(
                SleepLog(
                    userEmail = email,
                    dateString = selectedDate.value,
                    durationHours = hours,
                    qualityRating = rating,
                    notes = notes
                )
            )
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(points = profile.points + 20))
            }
            _message.emit("Sleep logged! +20 XP")
        }
    }

    // --- Habit Actions ---
    fun toggleHabitCompletion(habit: Habit) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            val date = selectedDate.value
            val currentCompletions = habitLogsForSelectedDate.value
            val isAlreadyCompleted = currentCompletions.any { it.habitId == habit.id }

            if (isAlreadyCompleted) {
                repository.removeHabitLog(habit.id, date)
                // decrement streak
                val newStreak = maxOf(0, habit.streak - 1)
                repository.updateHabitStreak(habit.id, newStreak)
            } else {
                repository.addHabitLog(HabitLog(userEmail = email, habitId = habit.id, dateString = date))
                // increment streak
                val newStreak = habit.streak + 1
                repository.updateHabitStreak(habit.id, newStreak)
                // award points!
                userProfile.value?.let { profile ->
                    repository.updateProfile(profile.copy(points = profile.points + 15))
                }
                _message.emit("Habit Completed! Streak: $newStreak days! +15 XP")
            }
        }
    }

    fun createCustomHabit(title: String, category: String) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addHabit(Habit(userEmail = email, title = title, category = category, isCustom = true))
            _message.emit("Custom habit '$title' created!")
        }
    }

    // --- Body Measurement Actions ---
    fun logBodyMeasurement(weight: Float, chest: Float, waist: Float, biceps: Float, thigh: Float) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addBodyMeasurement(
                BodyMeasurement(
                    userEmail = email,
                    dateString = selectedDate.value,
                    weightKg = weight,
                    chestCm = chest,
                    waistCm = waist,
                    bicepsCm = biceps,
                    thighCm = thigh
                )
            )
            // also update active weight inside profile
            userProfile.value?.let { profile ->
                repository.updateProfile(profile.copy(weightKg = weight, points = profile.points + 25))
            }
            _message.emit("Body measurements details synchronized! +25 XP")
        }
    }

    // --- Progress Photo Upload ---
    fun addProgressPhotoAsset(label: String, typeName: String) {
        val email = currentUserEmail.value ?: return
        viewModelScope.launch {
            repository.addProgressPhoto(
                ProgressPhoto(
                    userEmail = email,
                    dateString = selectedDate.value,
                    imagePath = typeName, // we use predefined descriptive categories
                    tag = typeName, // "Before", "After" or "Progress"
                    notes = label
                )
            )
            _message.emit("Progress Photo catalogued!")
        }
    }

    // --- Helpers ---
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    companion object {
        fun provideFactory(repository: FitTrackRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return FitTrackViewModel(repository) as T
                }
            }
    }
}
