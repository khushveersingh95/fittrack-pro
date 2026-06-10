package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.ui.FitTrackApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.FitTrackViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract container and construct Repository ViewModel
        val appContainer = (applicationContext as FitTrackApplication).container
        val factory = FitTrackViewModel.provideFactory(appContainer.repository)
        val viewModel = ViewModelProvider(this, factory)[FitTrackViewModel::class.java]

        setContent {
            val isDarkModeDesired by viewModel.isDarkMode.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            
            // Allow manual override if toggled, otherwise follow system
            val useDarkTheme = if (isDarkModeDesired) true else systemInDark

            MyApplicationTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    if (showSplash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        FitTrackApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernLogo(modifier: Modifier = Modifier, iconSize: androidx.compose.ui.unit.Dp = 60.dp) {
    Box(
        modifier = modifier
            .size(iconSize * 1.8f)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .border(4.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1200),
        label = "splashAlpha"
    )
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = tween(durationMillis = 1200),
        label = "splashScale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer(alpha = alphaAnim, scaleX = scaleAnim, scaleY = scaleAnim)
        ) {
            ModernLogo(iconSize = 80.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "FITTRACK PRO",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 4.sp
                )
            )
            Text(
                text = "EVOLVE YOUR STRENGTH",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
            )
        }
    }
}
