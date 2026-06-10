package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val goalType: String = "Weight Loss", // "Weight Loss", "Muscle Gain", "Maintenance"
    val heightCm: Float = 175f,
    val weightKg: Float = 70f,
    val targetWeightKg: Float = 68f,
    val dailyCalorieTarget: Int = 2000,
    val dailyProteinTarget: Int = 140,
    val dailyCarbsTarget: Int = 220,
    val dailyFatTarget: Int = 65,
    val dailyWaterTargetMl: Int = 2500,
    val points: Int = 100 // achievement system score
)

@Entity(tableName = "workout_plans")
data class WorkoutPlan(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val title: String,
    val description: String,
    val type: String // "Push", "Pull", "Legs", "Custom"
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val name: String,
    val category: String, // "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"
    val isCustom: Boolean = false
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val planId: Long,
    val planTitle: String,
    val dateString: String, // "yyyy-MM-dd"
    val durationMinutes: Int,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_set_logs")
data class ExerciseSetLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val workoutLogId: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int,
    val weightKg: Float,
    val isCompleted: Boolean = true
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val title: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val dateString: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val amountMl: Int,
    val dateString: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sleep_logs")
data class SleepLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val dateString: String, // "yyyy-MM-dd"
    val durationHours: Float,
    val qualityRating: Int, // 1 to 5
    val notes: String = ""
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val title: String,
    val category: String, // "Workout", "Water", "Sleep", "Custom"
    val streak: Int = 0,
    val isCustom: Boolean = false
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val habitId: Long,
    val dateString: String, // "yyyy-MM-dd"
    val isCompleted: Boolean = true
)

@Entity(tableName = "body_measurements")
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val dateString: String, // "yyyy-MM-dd"
    val weightKg: Float,
    val chestCm: Float,
    val waistCm: Float,
    val bicepsCm: Float,
    val thighCm: Float,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userEmail: String,
    val dateString: String, // "yyyy-MM-dd"
    val imagePath: String, // filepath or descriptive name
    val tag: String, // "Before", "After", "Progress"
    val notes: String = ""
)
