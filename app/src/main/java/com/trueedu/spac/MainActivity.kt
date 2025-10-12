package com.trueedu.spac

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.config.LocalFeature
import com.trueedu.spac.data.config.LocalFeatureConfig
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.GoogleAuthClient
import com.trueedu.spac.data.user.LocalUserCycle
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.ui.main.MainScreen
import com.trueedu.spac.ui.theme.TrueSpacTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var localFeature: LocalFeature
    @Inject
    lateinit var userCycle: UserCycle
    @Inject
    lateinit var googleAuthClient: GoogleAuthClient

    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            this.navController = navController

            LaunchedEffect(Unit) {
                intent?.let { navController.handleDeepLink(it) }
            }

            CompositionLocalProvider(
                LocalTrueAnalytics provides trueAnalytics,
                LocalFeatureConfig provides localFeature,
                LocalUserCycle provides userCycle,
            ) {
                TrueSpacTheme {
                    MainScreen(
                        navController = navController,
                        loginWithGoogle = ::loginWithGoogle,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }

    private fun loginWithGoogle() {
        logD("loginWithGoogle()")
        lifecycleScope.launch {
            val signInResult = googleAuthClient.signIn(this@MainActivity)
            if (signInResult.isSuccess) {
                logD("loginWithGoogle() success")
            } else {
                logD("loginWithGoogle() failed: ${signInResult.exceptionOrNull()?.message}")
            }
        }
    }
}
