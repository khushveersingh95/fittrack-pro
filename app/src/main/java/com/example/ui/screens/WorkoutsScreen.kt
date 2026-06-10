package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ExerciseSetLog
import com.example.data.WorkoutPlan
import com.example.ui.theme.FitnessGold
import com.example.ui.theme.FitnessWarmCoral
import com.example.viewmodel.FitTrackViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(viewModel: FitTrackViewModel) {
    val plans by viewModel.workoutPlans.collectAsStateWithLifecycle()
    val logs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val allLoggedSets by viewModel.allSets.collectAsStateWithLifecycle()

    var showAddPlanDialog by remember { mutableStateOf(false) }
    var newPlanTitle by remember { mutableStateOf("") }
    var newPlanDesc by remember { mutableStateOf("") }
    var newPlanType by remember { mutableStateOf("Push") }

    // Selected plan for an Active Session
    var activeWorkoutPlan by remember { mutableStateOf<WorkoutPlan?>(null) }
    var activeSetsList by remember { mutableStateOf(listOf<ActiveSetState>()) }
    var activeWorkoutNotes by remember { mutableStateOf("") }
    var activeWorkoutMinutes by remember { mutableStateOf("45") }

    // Simple Rest Timer details
    var timerSecondsRemaining by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Trigger counting loop
    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (timerSecondsRemaining > 0) {
                delay(1000)
                timerSecondsRemaining--
                if (timerSecondsRemaining == 0) {
                    timerRunning = false
                    // optional play audio, but we show toast/message reactively!
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Active rest timer tracking banner
        if (timerSecondsRemaining > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = FitnessWarmCoral.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Rest timer active",
                            tint = FitnessWarmCoral,
                            modifier = Modifier
                                .size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Rest Timer Ticking",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "$timerSecondsRemaining seconds left to hydrate",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = { timerSecondsRemaining += 30 }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+30s")
                        }
                        IconButton(
                            onClick = { timerSecondsRemaining = 0; timerRunning = false }
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel timer", tint = Color.Red)
                        }
                    }
                }
            }
        }

        // Active Set Entry State if a routine template is clicked
        AnimatedContent(
            targetState = activeWorkoutPlan,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }, label = "activeSession"
        ) { targetPlan ->
            if (targetPlan != null) {
                // Renders Active workout screen logging exercises
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Active Session",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = targetPlan.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = { activeWorkoutPlan = null },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Duration inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = activeWorkoutMinutes,
                                onValueChange = { activeWorkoutMinutes = it },
                                label = { Text("Duration (Minutes)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = activeWorkoutNotes,
                                onValueChange = { activeWorkoutNotes = it },
                                label = { Text("Session General Notes") },
                                modifier = Modifier.weight(1.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom internal rest timer launchers
                        Text(
                            text = "Launch Active Rest Timer",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val times = listOf(45, 60, 90, 120)
                            times.forEach { t ->
                                Button(
                                    onClick = {
                                        timerSecondsRemaining = t
                                        timerRunning = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("${t}s", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        // Set Logs List
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exercises List & Sets",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    val nextSetNum = activeSetsList.size + 1
                                    val defaultName = if (targetPlan.type == "Push") "Bench Press" else if (targetPlan.type == "Pull") "Deadlift" else "Barbell Squats"
                                    activeSetsList = activeSetsList + ActiveSetState(
                                        exerciseName = defaultName,
                                        setNum = nextSetNum,
                                        reps = "10",
                                        weight = "60"
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Exercise Set")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Set", color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (activeSetsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap 'Add Set' to log sets, reps, and load",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            activeSetsList.forEachIndexed { idx, set ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Simple dropdown estimation
                                        Text(
                                            text = "S${set.setNum}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        OutlinedTextField(
                                            value = set.exerciseName,
                                            onValueChange = { name ->
                                                activeSetsList = activeSetsList.toMutableList().apply {
                                                    this[idx] = set.copy(exerciseName = name)
                                                }
                                            },
                                            label = { Text("Exercise") },
                                            modifier = Modifier.weight(1.5f),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = set.reps,
                                            onValueChange = { repNum ->
                                                activeSetsList = activeSetsList.toMutableList().apply {
                                                    this[idx] = set.copy(reps = repNum)
                                                }
                                            },
                                            label = { Text("Reps") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = set.weight,
                                            onValueChange = { wt ->
                                                activeSetsList = activeSetsList.toMutableList().apply {
                                                    this[idx] = set.copy(weight = wt)
                                                }
                                            },
                                            label = { Text("Kg") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                            singleLine = true
                                        )

                                        IconButton(
                                            onClick = {
                                                activeSetsList = activeSetsList.toMutableList().apply {
                                                    removeAt(idx)
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val userEmail = viewModel.currentUserEmail.value ?: ""
                                val finalizedSets = activeSetsList.map { item ->
                                    ExerciseSetLog(
                                        userEmail = userEmail,
                                        workoutLogId = 0,
                                        exerciseName = item.exerciseName,
                                        setNumber = item.setNum,
                                        reps = item.reps.toIntOrNull() ?: 10,
                                        weightKg = item.weight.toFloatOrNull() ?: 60f
                                    )
                                }
                                viewModel.logWorkoutSession(
                                    planTitle = targetPlan.title,
                                    durationMin = activeWorkoutMinutes.toIntOrNull() ?: 45,
                                    notes = activeWorkoutNotes,
                                    sets = finalizedSets
                                )
                                activeWorkoutPlan = null
                                activeSetsList = emptyList()
                                activeWorkoutNotes = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Log Completed Daily Workout Plan", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- View templates grid ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Workout Routines & Plans",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(
                onClick = { showAddPlanDialog = true }
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = "New plan", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        plans.forEach { plan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        // Start an Active Workout Session based on template !
                        activeWorkoutPlan = plan
                        // Seed starting defaults for specific routine type!
                        activeSetsList = if (plan.type == "Push") {
                            listOf(
                                ActiveSetState("Bench Press", 1, "10", "60"),
                                ActiveSetState("Bench Press", 2, "8", "70"),
                                ActiveSetState("Overhead Press", 3, "10", "40")
                            )
                        } else if (plan.type == "Pull") {
                            listOf(
                                ActiveSetState("Deadlift", 1, "5", "100"),
                                ActiveSetState("Pull-ups", 2, "10", "0"),
                                ActiveSetState("Barbell Curls", 3, "12", "25")
                            )
                        } else {
                            listOf(
                                ActiveSetState("Barbell Squats", 1, "8", "80"),
                                ActiveSetState("Barbell Squats", 2, "8", "90"),
                                ActiveSetState("Leg Press", 3, "12", "160")
                            )
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = plan.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = plan.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start Routine",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History logs
        Text(
            text = "Your Workout Log History",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history logged yet. Complete a workout to see entries here!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            logs.forEach { log ->
                val matchingSets = allLoggedSets.filter { it.workoutLogId == log.id }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = log.planTitle,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = FitnessGold
                            )
                            Text(
                                text = log.dateString,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Text(
                            text = "Duration: ${log.durationMinutes} mins | ${matchingSets.size} sets logged",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        if (log.notes.isNotBlank()) {
                            Text(
                                text = "Notes: ${log.notes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        if (matchingSets.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(4.dp))
                            matchingSets.forEach { set ->
                                Text(
                                    text = "• ${set.exerciseName}: Set ${set.setNumber} (Reps: ${set.reps} @ ${set.weightKg}Kg)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Workout Plan Dialog
    if (showAddPlanDialog) {
        AlertDialog(
            onDismissRequest = { showAddPlanDialog = false },
            title = { Text("Create Custom Workout Routine") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newPlanTitle,
                        onValueChange = { newPlanTitle = it },
                        label = { Text("Routine Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPlanDesc,
                        onValueChange = { newPlanDesc = it },
                        label = { Text("Summary Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Workout Category",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val types = listOf("Push", "Pull", "Legs", "Custom")
                        types.forEach { t ->
                            val selected = newPlanType == t
                            FilterChip(
                                selected = selected,
                                onClick = { newPlanType = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlanTitle.isNotBlank()) {
                            viewModel.addCustomWorkoutPlan(
                                title = newPlanTitle,
                                description = newPlanDesc,
                                type = newPlanType
                            )
                            showAddPlanDialog = false
                            newPlanTitle = ""
                            newPlanDesc = ""
                        }
                    }
                ) {
                    Text("Save Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class ActiveSetState(
    val exerciseName: String,
    val setNum: Int,
    val reps: String,
    val weight: String
)
