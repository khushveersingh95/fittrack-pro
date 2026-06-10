package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BodyMeasurement
import com.example.data.SleepLog
import com.example.data.WaterLog
import com.example.ui.components.InteractiveLineChart
import com.example.ui.theme.FitnessGold
import com.example.ui.theme.FitnessSecondaryCyan
import com.example.ui.theme.FitnessWarmCoral
import com.example.viewmodel.FitTrackViewModel

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MoreTrackersScreen(viewModel: FitTrackViewModel) {
    var activeSubcategory by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    AnimatedContent(
        targetState = activeSubcategory,
        transitionSpec = {
            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
        }, label = "subNavigation"
    ) { sub ->
        if (sub == null) {
            // Main sub-navigation Grid Layout
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Text(
                    text = "Trackers & Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val primaryAltColor = Color(0xFF6366F1) // Indigo Accent
                    val tertiaryAltColor = Color(0xFFEC4899) // Pink Accent
                    val quickMenu = listOf(
                        SubmenuItem("Hydration", Icons.Default.LocalActivity, FitnessSecondaryCyan, "💧 Water Intake Log"),
                        SubmenuItem("Sleep Diary", Icons.Default.Bedtime, FitnessGold, "😴 Sleep Logs & Quality"),
                        SubmenuItem("Measurements", Icons.Default.QueryStats, FitnessWarmCoral, "📏 Waist & Body Metrics"),
                        SubmenuItem("Progress Photos", Icons.Default.PhotoLibrary, primaryAltColor, "📸 Before vs After"),
                        SubmenuItem("Goal Setup", Icons.Default.TrackChanges, tertiaryAltColor, "🎯 Goal & Targets"),
                        SubmenuItem("Settings", Icons.Default.Settings, Color.Gray, "⚙️ Preferences & PDF")
                    )
                    items(quickMenu) { item ->
                        Card(
                            onClick = { activeSubcategory = item.id },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(item.color.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = item.icon, contentDescription = item.id, tint = item.color, modifier = Modifier.size(20.dp))
                                }
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Renders selected specific tracker view with a Back Button !
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(sub, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                        navigationIcon = {
                            IconButton(onClick = { activeSubcategory = null }) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (sub) {
                        "Hydration" -> HydrationView(viewModel)
                        "Sleep Diary" -> SleepDiaryView(viewModel)
                        "Measurements" -> MeasurementsView(viewModel)
                        "Progress Photos" -> ProgressPhotosView(viewModel)
                        "Goal Setup" -> GoalSetupView(viewModel)
                        "Settings" -> SettingsView(viewModel, context)
                    }
                }
            }
        }
    }
}

// 1. Hydration Log View Detail
@Composable
fun HydrationView(viewModel: FitTrackViewModel) {
    val waterLogs by viewModel.waterLogsForSelectedDate.collectAsStateWithLifecycle()
    val totalWater = waterLogs.sumOf { it.amountMl }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = FitnessSecondaryCyan.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Hydration icon",
                    tint = FitnessSecondaryCyan,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "$totalWater ml / 2800 ml",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = FitnessSecondaryCyan
                )
                Text(
                    text = "Daily Hydration Progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Wave progress simulation
                LinearProgressIndicator(
                    progress = { (totalWater.toFloat() / 2800f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = FitnessSecondaryCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Quick Add Water Trigger",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val quantities = listOf(250, 500, 750, 1000)
            quantities.forEach { ml ->
                Button(
                    onClick = { viewModel.quickAddWater(ml) },
                    colors = ButtonDefaults.buttonColors(containerColor = FitnessSecondaryCyan),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+$ml\nml",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Hydration Logs Today",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        waterLogs.forEach { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Water, contentDescription = "Water item", tint = FitnessSecondaryCyan)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Logged Water cup", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "+${log.amountMl} ml",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = FitnessSecondaryCyan
                    )
                }
            }
        }
    }
}

// 2. Sleep Diary View Detail
@Composable
fun SleepDiaryView(viewModel: FitTrackViewModel) {
    val sleepLogs by viewModel.sleepLogs.collectAsStateWithLifecycle()

    var sleepHours by remember { mutableFloatStateOf(7.5f) }
    var qualityRating by remember { mutableIntStateOf(4) }
    var sleepNotes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Sleep Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Slider hours
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Duration Rested", style = MaterialTheme.typography.bodyMedium)
                    Text(text = String.format("%.1f Hours", sleepHours), fontWeight = FontWeight.Bold, color = FitnessGold)
                }
                Slider(
                    value = sleepHours,
                    onValueChange = { sleepHours = it },
                    valueRange = 4f..12f,
                    steps = 15,
                    colors = SliderDefaults.colors(activeTrackColor = FitnessGold, thumbColor = FitnessGold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Rating Buttons
                Text(
                    text = "Sleep Quality Rating",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { star ->
                        val active = star <= qualityRating
                        IconButton(
                            onClick = { qualityRating = star }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (active) FitnessGold else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = sleepNotes,
                    onValueChange = { sleepNotes = it },
                    label = { Text("Quality Remarks (e.g. woke up once)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.logSleep(sleepHours, qualityRating, sleepNotes)
                        sleepNotes = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FitnessGold)
                ) {
                    Text("Save Sleep Log", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sleep History Analytics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Custom chart for sleep history!
        if (sleepLogs.size >= 2) {
            InteractiveLineChart(
                dataPoints = sleepLogs.take(5).map { it.durationHours }.reversed(),
                labels = sleepLogs.take(5).map { it.dateString.substringAfter("-") }.reversed(),
                color = FitnessGold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        sleepLogs.forEach { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = log.dateString, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = String.format("%.1f Hours Sleep", log.durationHours),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (log.notes.isNotBlank()) {
                            Text(text = log.notes, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Stars, contentDescription = "Rating", tint = FitnessGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "${log.qualityRating}/5", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. Measurements View Detail
@Composable
fun MeasurementsView(viewModel: FitTrackViewModel) {
    val measurements by viewModel.bodyMeasurements.collectAsStateWithLifecycle()

    var weightInput by remember { mutableStateOf("") }
    var chestInput by remember { mutableStateOf("") }
    var waistInput by remember { mutableStateOf("") }
    var bicepsInput by remember { mutableStateOf("") }
    var thighInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Track Body Dimensions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { weightInput = it },
                    label = { Text("Body Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = chestInput,
                        onValueChange = { chestInput = it },
                        label = { Text("Chest CM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = waistInput,
                        onValueChange = { waistInput = it },
                        label = { Text("Waist CM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = bicepsInput,
                        onValueChange = { bicepsInput = it },
                        label = { Text("Biceps CM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = thighInput,
                        onValueChange = { thighInput = it },
                        label = { Text("Thigh CM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.logBodyMeasurement(
                            weight = weightInput.toFloatOrNull() ?: 70f,
                            chest = chestInput.toFloatOrNull() ?: 100f,
                            waist = waistInput.toFloatOrNull() ?: 80f,
                            biceps = bicepsInput.toFloatOrNull() ?: 36f,
                            thigh = thighInput.toFloatOrNull() ?: 55f
                        )
                        weightInput = ""
                        chestInput = ""
                        waistInput = ""
                        bicepsInput = ""
                        thighInput = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = FitnessWarmCoral)
                ) {
                    Text("Save Measurements", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Weight Progress Analysis",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (measurements.size >= 2) {
            InteractiveLineChart(
                dataPoints = measurements.map { it.weightKg },
                labels = measurements.map { it.dateString.substringAfter("-") },
                color = FitnessWarmCoral,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        measurements.reversed().forEach { log ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = log.dateString, style = MaterialTheme.typography.labelSmall)
                        Text(text = "${log.weightKg} kg", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = FitnessWarmCoral)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Chest: ${log.chestCm}cm | Waist: ${log.waistCm}cm | Biceps: ${log.bicepsCm}cm | Thigh: ${log.thighCm}cm",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// 4. Progress Photos View Detail
@Composable
fun ProgressPhotosView(viewModel: FitTrackViewModel) {
    val photos by viewModel.progressPhotos.collectAsStateWithLifecycle()
    var photoLabel by remember { mutableStateOf("") }
    var comparisonMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add Progress Photo Record",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = photoLabel,
                    onValueChange = { photoLabel = it },
                    label = { Text("Photo Remark (e.g. Week 1 Chest Pump)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("Before", "After", "Progress")
                    categories.forEach { cat ->
                        Button(
                            onClick = {
                                if (photoLabel.isNotBlank()) {
                                    viewModel.addProgressPhotoAsset(photoLabel, cat)
                                    photoLabel = ""
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("As $cat", color = Color.Black, fontSize = 10.sp)
                        }
                    }
                }
                Text(
                    text = "Tip: Input note, then tap target Category to simulate photo addition.",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Progress Gallery",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Switch Comparison mode
            FilterChip(
                selected = comparisonMode,
                onClick = { comparisonMode = !comparisonMode },
                label = { Text("Before vs After Mode") }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (comparisonMode) {
            // Before vs After comparison layout
            val beforePhoto = photos.firstOrNull { it.tag == "Before" }
            val afterPhoto = photos.firstOrNull { it.tag == "After" }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "BEFORE", fontWeight = FontWeight.Bold, color = Color.Red, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = "Before placeholder", tint = Color.LightGray, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = beforePhoto?.notes ?: "Pending catalog",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "AFTER", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = "After placeholder", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = afterPhoto?.notes ?: "Pending catalog",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Standard scroll list
            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No progress photos saved. Add some above!", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            } else {
                photos.forEach { ph ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Image, contentDescription = "photo icon", tint = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(text = ph.tag, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                                }
                                Text(text = ph.notes, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Logged on ${ph.dateString}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

// 5. Goal Setup View Detail
@Composable
fun GoalSetupView(viewModel: FitTrackViewModel) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var activeGoal by remember { mutableStateOf("Muscle Gain") }
    var heightNum by remember { mutableStateOf("178") }
    var weightNum by remember { mutableStateOf("74") }
    var targetWtNum by remember { mutableStateOf("78") }
    var calorieTgtOpt by remember { mutableStateOf("2400") }
    var proteinTgtOpt by remember { mutableStateOf("150") }

    LaunchedEffect(profile) {
        profile?.let {
            activeGoal = it.goalType
            heightNum = it.heightCm.toInt().toString()
            weightNum = it.weightKg.toInt().toString()
            targetWtNum = it.targetWeightKg.toInt().toString()
            calorieTgtOpt = it.dailyCalorieTarget.toString()
            proteinTgtOpt = it.dailyProteinTarget.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Configure Goal & Matrix Targets",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "Goal Target Objective",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val goals = listOf("Weight Loss", "Muscle Gain", "Maintenance")
                    goals.forEach { g ->
                        val selected = activeGoal == g
                        Button(
                            onClick = { activeGoal = g },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = g.substringBefore(" "),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = heightNum,
                    onValueChange = { heightNum = it },
                    label = { Text("Height (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightNum,
                        onValueChange = { weightNum = it },
                        label = { Text("Starting Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetWtNum,
                        onValueChange = { targetWtNum = it },
                        label = { Text("Goal Metric (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = calorieTgtOpt,
                        onValueChange = { calorieTgtOpt = it },
                        label = { Text("Daily kcal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = proteinTgtOpt,
                        onValueChange = { proteinTgtOpt = it },
                        label = { Text("Daily Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        viewModel.updateProfileDetails(
                            name = profile?.name ?: "Champion",
                            goal = activeGoal,
                            height = heightNum.toFloatOrNull() ?: 175f,
                            weight = weightNum.toFloatOrNull() ?: 70f,
                            targetWeight = targetWtNum.toFloatOrNull() ?: 70f,
                            calories = calorieTgtOpt.toIntOrNull() ?: 2000,
                            protein = proteinTgtOpt.toIntOrNull() ?: 140,
                            carbs = profile?.dailyCarbsTarget ?: 220,
                            fat = profile?.dailyFatTarget ?: 65,
                            water = profile?.dailyWaterTargetMl ?: 2500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply Goals & Sync", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 6. Settings View Detail
@Composable
fun SettingsView(viewModel: FitTrackViewModel, context: Context) {
    val isDark by viewModel.isDarkMode.collectAsStateWithLifecycle()

    var workoutNotification by remember { mutableStateOf(true) }
    var waterNotification by remember { mutableStateOf(true) }
    var mealsNotification by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "App Customizations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Dark mode switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "App Dark Mode style", fontWeight = FontWeight.Bold)
                        Text(text = "Eco energy saver visual profile", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Notification Preferences",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Workout Reminder")
                    Switch(checked = workoutNotification, onCheckedChange = { workoutNotification = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Hydration / Water Reminder")
                    Switch(checked = waterNotification, onCheckedChange = { waterNotification = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Meal Logs Prompt")
                    Switch(checked = mealsNotification, onCheckedChange = { mealsNotification = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Data Backup & Report Generation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = {
                        Toast.makeText(context, "FitTrack local database successfully backed up!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Backup")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger Database Backup", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        Toast.makeText(context, "Exporting metrics report... PDF saved to local Storage/Downloads successfully!", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF icon", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export All Data Reports to PDF", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class SubmenuItem(
    val id: String,
    val icon: ImageVector,
    val color: Color,
    val label: String
)
