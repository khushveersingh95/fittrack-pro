package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Meal
import com.example.ui.components.NutrientMiniRow
import com.example.ui.theme.FitnessPrimaryNeon
import com.example.ui.theme.FitnessSecondaryCyan
import com.example.ui.theme.FitnessWarmCoral
import com.example.viewmodel.FitTrackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(viewModel: FitTrackViewModel) {
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var mealTypeSelected by remember { mutableStateOf("Breakfast") }
    var mealTitle by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var proteinInput by remember { mutableStateOf("") }
    var carbsInput by remember { mutableStateOf("") }
    var fatInput by remember { mutableStateOf("") }

    val totalCalories = meals.sumOf { it.calories }
    val totalProtein = meals.sumOf { it.proteinGrams }
    val totalCarbs = meals.sumOf { it.carbsGrams }
    val totalFat = meals.sumOf { it.fatGrams }

    val calorieTarget = profile?.dailyCalorieTarget ?: 2000
    val proteinTarget = profile?.dailyProteinTarget ?: 140
    val carbsTarget = profile?.dailyCarbsTarget ?: 220
    val fatTarget = profile?.dailyFatTarget ?: 65

    // Helper: Seed quick food base
    val foodDatabase = listOf(
        QuickFoodItem("Grilled Chicken Rice & Broccoli", 650, 52, 78, 12),
        QuickFoodItem("Protein Whey Shake", 140, 25, 3, 2),
        QuickFoodItem("Greek Yogurt & Berries", 180, 15, 20, 4),
        QuickFoodItem("Scrambled Eggs (x3) with Toast", 380, 24, 28, 16),
        QuickFoodItem("Oatmeal & Banana with Peanut Butter", 420, 12, 65, 14),
        QuickFoodItem("Mixed Mixed Nuts (50g)", 300, 10, 12, 24),
        QuickFoodItem("Baked Salmon & Sweet Potato Bowl", 550, 42, 45, 18)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Topic Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Diet & Nutrition Tracker",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Meal")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Tracker Status Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daily Intake Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Large Calorie Progress display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$totalCalories kcal",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "of $calorieTarget kcal goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // Simple Circular progress ring for calories
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(60.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { if (calorieTarget > 0) totalCalories.toFloat() / calorieTarget.toFloat() else 0f },
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp
                        )
                        Text(
                            text = String.format("%.0f%%", if (calorieTarget > 0) (totalCalories.toFloat() / calorieTarget.toFloat() * 100f) else 0f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nutrient Progress bars
                NutrientMiniRow(label = "Protein Focus", value = totalProtein, target = proteinTarget, color = FitnessPrimaryNeon)
                Spacer(modifier = Modifier.height(8.dp))
                NutrientMiniRow(label = "Carbohydrates", value = totalCarbs, target = carbsTarget, color = FitnessSecondaryCyan)
                Spacer(modifier = Modifier.height(8.dp))
                NutrientMiniRow(label = "Healthy Fat", value = totalFat, target = fatTarget, color = FitnessWarmCoral)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Database / Common Foods
        Text(
            text = "Quick Log Food Database",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Click to instantly log a classic fit meal:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        foodDatabase.take(3).forEach { food ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .clickable {
                                        viewModel.logMeal(
                                            type = "Lunch",
                                            title = food.name,
                                            calories = food.calories,
                                            protein = food.protein,
                                            carbs = food.carbs,
                                            fat = food.fat
                                        )
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = food.name,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Meals History List
        Text(
            text = "Today's Tracked Meals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (meals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No meals logged for today.\nClick the + button to add Breakfast, Lunch, Dinner or Snacks!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            meals.forEach { meal ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = "Meal status",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp).padding(end = 8.dp)
                            )
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = meal.mealType,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = meal.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "P: ${meal.proteinGrams}g | C: ${meal.carbsGrams}g | F: ${meal.fatGrams}g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${meal.calories} cal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.deleteMeal(meal.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove item",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Meal Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Log New Meal Track") },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Meal Type Choice
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val types = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                            types.forEach { t ->
                                val selected = mealTypeSelected == t
                                Button(
                                    onClick = { mealTypeSelected = t },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.height(34.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = t,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = mealTitle,
                            onValueChange = { mealTitle = it },
                            label = { Text("Food Title / Description") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = caloriesInput,
                            onValueChange = { caloriesInput = it },
                            label = { Text("Estimated Calories (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proteinInput,
                                onValueChange = { proteinInput = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = carbsInput,
                                onValueChange = { carbsInput = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fatInput,
                                onValueChange = { fatInput = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Instant helper template button
                        Text(
                            text = "Or choose standard helper template:",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            foodDatabase.take(4).forEach { option ->
                                Button(
                                    onClick = {
                                        mealTitle = option.name
                                        caloriesInput = option.calories.toString()
                                        proteinInput = option.protein.toString()
                                        carbsInput = option.carbs.toString()
                                        fatInput = option.fat.toString()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    modifier = Modifier.weight(1f).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(option.name.take(8) + "..", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (mealTitle.isNotBlank()) {
                                viewModel.logMeal(
                                    type = mealTypeSelected,
                                    title = mealTitle,
                                    calories = caloriesInput.toIntOrNull() ?: 100,
                                    protein = proteinInput.toIntOrNull() ?: 10,
                                    carbs = carbsInput.toIntOrNull() ?: 10,
                                    fat = fatInput.toIntOrNull() ?: 5
                                )
                                showAddDialog = false
                                // reset variables
                                mealTitle = ""
                                caloriesInput = ""
                                proteinInput = ""
                                carbsInput = ""
                                fatInput = ""
                            }
                        }
                    ) {
                        Text("Add Track")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

data class QuickFoodItem(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)
