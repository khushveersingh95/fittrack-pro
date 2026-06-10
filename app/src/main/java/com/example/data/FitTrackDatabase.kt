package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfile::class,
        WorkoutPlan::class,
        Exercise::class,
        WorkoutLog::class,
        ExerciseSetLog::class,
        Meal::class,
        WaterLog::class,
        SleepLog::class,
        Habit::class,
        HabitLog::class,
        BodyMeasurement::class,
        ProgressPhoto::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FitTrackDatabase : RoomDatabase() {

    abstract fun fitTrackDao(): FitTrackDao

    companion object {
        @Volatile
        private var INSTANCE: FitTrackDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): FitTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitTrackDatabase::class.java,
                    "fittrack_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    seedDatabase(database.fitTrackDao())
                }
            }
        }

        private suspend fun seedDatabase(dao: FitTrackDao) {
            val userEmail = "user@fittrack.com"
            // 1. Seed standard Profile
            val defaultProfile = UserProfile(
                email = userEmail,
                name = "Athletic Explorer",
                goalType = "Muscle Gain",
                heightCm = 178f,
                weightKg = 74.5f,
                targetWeightKg = 78f,
                dailyCalorieTarget = 2400,
                dailyProteinTarget = 155,
                dailyCarbsTarget = 265,
                dailyFatTarget = 80,
                dailyWaterTargetMl = 2800,
                points = 240
            )
            dao.insertProfile(defaultProfile)

            // 2. Seed default Exercises
            val exercises = listOf(
                Exercise(userEmail = userEmail, name = "Bench Press", category = "Chest"),
                Exercise(userEmail = userEmail, name = "Incline Dumbbell Press", category = "Chest"),
                Exercise(userEmail = userEmail, name = "Push ups", category = "Chest"),
                Exercise(userEmail = userEmail, name = "Chest Flyes", category = "Chest"),
                Exercise(userEmail = userEmail, name = "Overhead Press", category = "Shoulders"),
                Exercise(userEmail = userEmail, name = "Lateral Raises", category = "Shoulders"),
                Exercise(userEmail = userEmail, name = "Skull Crushers", category = "Arms"),
                Exercise(userEmail = userEmail, name = "Triceps Pushdowns", category = "Arms"),
                
                Exercise(userEmail = userEmail, name = "Deadlift", category = "Back"),
                Exercise(userEmail = userEmail, name = "Pull-ups", category = "Back"),
                Exercise(userEmail = userEmail, name = "Lat Pulldowns", category = "Back"),
                Exercise(userEmail = userEmail, name = "Bent Over Barbell Rows", category = "Back"),
                Exercise(userEmail = userEmail, name = "Barbell Curls", category = "Arms"),
                Exercise(userEmail = userEmail, name = "Hammer Curls", category = "Arms"),

                Exercise(userEmail = userEmail, name = "Barbell Squats", category = "Legs"),
                Exercise(userEmail = userEmail, name = "Leg Press", category = "Legs"),
                Exercise(userEmail = userEmail, name = "Romanian Deadlifts", category = "Legs"),
                Exercise(userEmail = userEmail, name = "Calf Raises", category = "Legs"),
                Exercise(userEmail = userEmail, name = "Plank", category = "Core"),
                Exercise(userEmail = userEmail, name = "Crunches", category = "Core")
            )
            for (exercise in exercises) {
                dao.insertExercise(exercise)
            }

            // 3. Seed default Templates
            dao.insertWorkoutPlan(WorkoutPlan(userEmail = userEmail, title = "Push Workout", description = "Targeting Chest, Shoulders, and Triceps", type = "Push"))
            dao.insertWorkoutPlan(WorkoutPlan(userEmail = userEmail, title = "Pull Workout", description = "Targeting Back, Biceps, and Rear Delts", type = "Pull"))
            dao.insertWorkoutPlan(WorkoutPlan(userEmail = userEmail, title = "Legs Workout", description = "Targeting Quads, Hamstrings, and Calves", type = "Legs"))

            // 4. Seed default Habits
            dao.insertHabit(Habit(userEmail = userEmail, title = "Drink 2.8L Water", category = "Water", streak = 5, isCustom = false))
            dao.insertHabit(Habit(userEmail = userEmail, title = "Complete Daily Workout Plan", category = "Workout", streak = 3, isCustom = false))
            dao.insertHabit(Habit(userEmail = userEmail, title = "7-8 Hours Restful Sleep", category = "Sleep", streak = 4, isCustom = false))
            dao.insertHabit(Habit(userEmail = userEmail, title = "Track Calories", category = "Custom", streak = 1, isCustom = true))

            // 5. Seed some starting water logs & meals & sleep for "today"
            val today = "2026-06-10" // hardcoded/today's date matching metadata for great initial look
            dao.insertWaterLog(WaterLog(userEmail = userEmail, amountMl = 500, dateString = today))
            dao.insertWaterLog(WaterLog(userEmail = userEmail, amountMl = 500, dateString = today))
            dao.insertWaterLog(WaterLog(amountMl = 250, userEmail = userEmail, dateString = today))

            dao.insertMeal(Meal(userEmail = userEmail, mealType = "Breakfast", title = "Oatmeal with Blueberries & Whey", calories = 480, proteinGrams = 35, carbsGrams = 60, fatGrams = 8, dateString = today))
            dao.insertMeal(Meal(userEmail = userEmail, mealType = "Lunch", title = "Grilled Chicken Rice & Broccoli", calories = 650, proteinGrams = 52, carbsGrams = 78, fatGrams = 12, dateString = today))
            
            dao.insertSleepLog(SleepLog(userEmail = userEmail, dateString = today, durationHours = 7.5f, qualityRating = 4, notes = "Felt rested and woke up naturally."))

            val dayBefore = "2026-06-09"
            dao.insertBodyMeasurement(BodyMeasurement(userEmail = userEmail, dateString = dayBefore, weightKg = 74.8f, chestCm = 101.5f, waistCm = 81f, bicepsCm = 37.2f, thighCm = 56.5f))
            dao.insertBodyMeasurement(BodyMeasurement(userEmail = userEmail, dateString = today, weightKg = 74.5f, chestCm = 101.8f, waistCm = 80.5f, bicepsCm = 37.5f, thighCm = 56.8f))
        }
    }
}
