package com.dramallama.app

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dramallama.app.ui.navigation.NavGraph
import com.dramallama.app.ui.theme.LlamaDramaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        // Add smooth exit animation
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val fadeOut = ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply {
                interpolator = DecelerateInterpolator()
                duration = 300L
                doOnEnd { splashScreenView.remove() }
            }
            fadeOut.start()
        }
        
        enableEdgeToEdge()
        
        val app = application as LlamaDramaApp
        
        setContent {
            LlamaDramaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        repository = app.repository
                    )
                }
            }
        }
    }
}