package me.shetj.mp3recorder.compose.demo

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.shetj.mp3recorder.compose.demo.ui.theme.Mp3RecorderComposeTheme

@ExperimentalMaterial3Api
@ExperimentalPagerApi
@ExperimentalPermissionsApi
class MainActivity2 : ComponentActivity() {

    private var splashScreen: SplashScreen? =null
    private var isKeep = true


    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        lifecycleScope.launch {
            delay(1000)
            isKeep = false
        }
        splashScreen!!.setKeepOnScreenCondition(SplashScreen.KeepOnScreenCondition {
            //指定保持启动画面展示的条件
            return@KeepOnScreenCondition isKeep
        })
        splashScreen!!.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
//            // Get the duration of the animated vector drawable.
//            val animationDuration = splashScreenViewProvider.iconAnimationDurationMillis
//// Get the start time of the animation.
//            val animationStart = splashScreenViewProvider.iconAnimationStartMillis
//// Calculate the remaining duration of the animation.
//            val remainingDuration = (animationDuration - (animationStart - System.currentTimeMillis()))
//                .coerceAtLeast(0L)


            val slideUp = ObjectAnimator.ofFloat(
                splashScreenView,
                View.ALPHA,
                1f,
                0f,
            )
            slideUp.duration = 800
            slideUp.doOnEnd {
//                splashScreenViewProvider.remove()
            }
            slideUp.start()
        }
        setContent {
            DefTheme {
                RecordUI()
            }
        }
    }
}

