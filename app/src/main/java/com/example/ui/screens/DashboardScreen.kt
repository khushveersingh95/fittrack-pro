package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AchievementPill
import com.example.ui.components.MetricCircle
import com.example.ui.theme.FitnessGold
import com.example.ui.theme.FitnessSecondaryCyan
import com.example.ui.theme.FitnessWarmCoral
import com.example.viewmodel.FitTrackViewModel

@Composable
fun DashboardScreen(
    viewModel: FitTrackViewModel,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToDiet: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val waterLogs by viewModel.waterLogsForSelectedDate.collectAsStateWithLifecycle()
    val meals by viewModel.mealsForSelectedDate.collectAsStateWithLifecycle()
    val logs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val habitCompletions by viewModel.habitLogsForSelectedDate.collectAsStateWithLifecycle()

    val totalCalories = meals.sumOf { it.calories }
    val calorieTarget = profile?.dailyCalorieTarget ?: 2000

    val totalWater = waterLogs.sumOf { it.amountMl }
    val waterTarget = profile?.dailyWaterTargetMl ?: 2500

    // XP calculation
    val xp = profile?.points ?: 100
    val level = (xp / 100) + 1
    val relativeXP = xp % 100
    val todaysWorkoutLogged = logs.any { it.dateString == viewModel.selectedDate.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcoming header styled exactly like HTML: Good morning, Name, and prominent Circle Avatar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Good morning,",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profile?.name ?: "Athlete Champion",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Circle Avatar: bg-[#D0BCFF] text-[#381E72] circular shape with initial
            val initial = profile?.name?.filter { it.isLetter() }?.take(2)?.uppercase() ?: "AJ"
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0BCFF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF381E72)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Daily Activity highlight card: bg-[#EADDFF] rounded-[28px] p-5 shadow-sm
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val percentage = if (calorieTarget > 0) (totalCalories * 100) / calorieTarget else 0
                val progressRatio = if (calorieTarget > 0) totalCalories.toFloat() / calorieTarget.toFloat() else 0f
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Daily Activity",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$totalCalories / $calorieTarget kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                // Smoothly animated progress indicator
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progressRatio.coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 800),
                        label = "dailyProgress"
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFD0BCFF),
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = Color(0xFF6750A4),
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF6750A4), CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Progression Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Experience Level $level Athlete",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$relativeXP / 100 XP to Level ${level + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { relativeXP / 100f },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid cards layout side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Water/Hydration Card: bg-white, rounded-2xl, border-[#CAC4D0]
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFD3E3FD), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 16.sp)
                        }
                        Text(
                            text = "Water",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF49454F)
                        )
                    }

                    Column {
                        val waterLiters = totalWater.toFloat() / 1000f
                        val targetLiters = waterTarget.toFloat() / 1000f
                        Text(
                            text = String.format("%.1f", waterLiters),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = String.format("/%.1fL target", targetLiters),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val waterProgress = if (waterTarget > 0) totalWater.toFloat() / waterTarget.toFloat() else 0f
                        LinearProgressIndicator(
                            progress = { waterProgress.coerceIn(0f, 1f) },
                            color = Color(0xFF0B57D0),
                            trackColor = Color(0xFFE1E3E1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            // Training status card: bg-white, rounded-2xl, border-[#CAC4D0]
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(132.dp)
                    .clickable { onNavigateToWorkouts() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFFFEEFC3), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⚡", fontSize = 16.sp)
                        }
                        Text(
                            text = "Sweat",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF49454F)
                        )
                    }

                    Column {
                        Text(
                            text = if (todaysWorkoutLogged) "Done" else "Ready",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (todaysWorkoutLogged) "Session logged today" else "Tap to log reps",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (todaysWorkoutLogged) Color(0xFF137333) else Color(0xFFB06000)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick add action pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.quickAddWater(250) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3E3FD)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.LocalCafe, contentDescription = "Water cup", tint = Color(0xFF041E49))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+250ml", color = Color(0xFF041E49), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.quickAddWater(500) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD3E3FD)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.LocalActivity, contentDescription = "Water bottle", tint = Color(0xFF041E49))
                Spacer(modifier = Modifier.width(6.dp))
                Text("+500ml", color = Color(0xFF041E49), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Current Workout / Active Session highlight card: bg-[#21005D], rounded-[28px], p-5 text-white
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToWorkouts() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF21005D))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Current Workout",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = if (todaysWorkoutLogged) "Finished Session" else "Live Training Session",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFEADDFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF21005D)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏋️", fontSize = 18.sp)
                    }
                    Column {
                        val loggedCount = logs.size
                        Text(
                            text = if (todaysWorkoutLogged) "Daily Target Achieved!" else "Ready to SWEAT?",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Logged $loggedCount exercises total",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onNavigateToWorkouts() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD0BCFF),
                        contentColor = Color(0xFF381E72)
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (todaysWorkoutLogged) "View Workouts Summary" else "Resume & Log Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Habit Streaks Today Horizontal Row (Direct translation of Design HTML)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Habit Streaks",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF49454F)
            )
            Text(
                text = "Tap to Complete",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Take up to 3 habits to show horizontally like design
            habits.take(3).forEach { habit ->
                val isDone = habitCompletions.any { it.habitId == habit.id }
                Column(
                    modifier = Modifier
                        .clickable { viewModel.toggleHabitCompletion(habit) }
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isDone) Color(0xFFEADDFF) else Color(0xFFF3EDF7),
                                shape = CircleShape
                            )
                            .border(
                                width = 2.dp,
                                color = if (isDone) Color(0xFF6750A4) else Color(0xFFCAC4D0),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${habit.streak}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isDone) Color(0xFF21005D) else Color(0xFF1D1B20)
                            )
                        )
                    }
                    Text(
                        text = habit.title.split(" ").firstOrNull() ?: habit.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        maxLines = 1,
                        color = Color(0xFF1D1B20)
                    )
                }
            }

            // A 'New' pill that can let user add habit
            Column(
                modifier = Modifier
                    .clickable { viewModel.createCustomHabit("Meditation", "Mindfulness") }
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(
                            width = 2.dp,
                            color = Color(0xFFCAC4D0),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF49454F)
                        )
                    )
                }
                Text(
                    text = "New",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF1D1B20)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Goal Milestone Targets List
        Text(
            text = "Goal Milestone Targets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AchievementPill("Water Master", "Hit 2.5L Hydration target 5 times.", requiredXP = 150, currentXP = xp)
        AchievementPill("Iron Pioneer", "Complete and log 3 workout templates.", requiredXP = 200, currentXP = xp)
        AchievementPill("Calorie Governor", "Balance target Calorie consumption for 7 days.", requiredXP = 300, currentXP = xp)
    }
}
