package com.trueedu.spac

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.net.toUri
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
import com.trueedu.spac.ui.main.ForceUpdateView
import com.trueedu.spac.ui.main.MainScreen
import com.trueedu.spac.ui.theme.TrueSpacTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm by viewModels<MainViewModel>()
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
            val forceUpdateVisible by vm.forceUpdateVisible.collectAsState()

            LaunchedEffect(Unit) {
                intent?.let { navController.handleDeepLink(it) }
            }

            CompositionLocalProvider(
                LocalTrueAnalytics provides trueAnalytics,
                LocalFeatureConfig provides localFeature,
                LocalUserCycle provides userCycle,
            ) {
                TrueSpacTheme {
                    if (forceUpdateVisible) {
                        ForceUpdateView(::gotoPlayStore)
                    } else {
                        MainScreen(
                            navController = navController,
                            loginWithGoogle = ::loginWithGoogle,
                        )
                    }
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

    private fun gotoPlayStore() {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    data = "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri()
                }
            )
        } catch (e: Exception) {
            logD("Failed to open Play Store: ${e.message}")
        }
    }
}
