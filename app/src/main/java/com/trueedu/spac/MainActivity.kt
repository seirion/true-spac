package com.trueedu.spac

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.trueedu.spac.analytics.LocalTrueAnalytics
import com.trueedu.spac.analytics.TrueAnalytics
import com.trueedu.spac.data.config.LocalFeature
import com.trueedu.spac.data.config.LocalFeatureConfig
import com.trueedu.spac.data.log.logD
import com.trueedu.spac.data.user.GoogleAuthClient
import com.trueedu.spac.data.user.LocalRemoteConfig
import com.trueedu.spac.data.user.LocalUserCycle
import com.trueedu.spac.data.user.RemoteConfig
import com.trueedu.spac.data.user.UserCycle
import com.trueedu.spac.repo.local.Local
import com.trueedu.spac.repo.local.LocalTrueLocal
import com.trueedu.spac.ui.ads.AdmobManager
import com.trueedu.spac.ui.components.snackbar.SimpleSnackbar
import com.trueedu.spac.ui.main.DisclaimerPopupView
import com.trueedu.spac.ui.main.ForceUpdateView
import com.trueedu.spac.ui.main.MainScreen
import com.trueedu.spac.ui.main.NoticePopupView
import com.trueedu.spac.ui.theme.TrueSpacTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias LoginCallback = (() -> Unit)?

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val vm by viewModels<MainViewModel>()
    @Inject
    lateinit var local: Local
    @Inject
    lateinit var trueAnalytics: TrueAnalytics
    @Inject
    lateinit var localFeature: LocalFeature
    @Inject
    lateinit var userCycle: UserCycle
    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager
    @Inject
    lateinit var googleAuthClient: GoogleAuthClient

    private lateinit var simpleSnackbar: SimpleSnackbar
    private var navController: NavHostController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        simpleSnackbar = SimpleSnackbar(lifecycleScope)

        setContent {
            val navController = rememberNavController()
            this.navController = navController
            val forceUpdateVisible by vm.forceUpdateVisible.collectAsState()
            val appNotice by vm.appNotice.collectAsState()
            var disclaimerPopupVisible by remember { mutableStateOf(!local.disclaimerAccepted) }

            LaunchedEffect(Unit) {
                intent?.let { navController.handleDeepLink(it) }
            }

            // 화면 항상 켜기 기능
            DisposableEffect(local.keepScreenOn) {
                if (local.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                onDispose {
                    // 정리 시 플래그 제거
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            CompositionLocalProvider(
                LocalTrueLocal provides local,
                LocalTrueAnalytics provides trueAnalytics,
                LocalFeatureConfig provides localFeature,
                LocalUserCycle provides userCycle,
                LocalRemoteConfig provides remoteConfig,
            ) {
                TrueSpacTheme {
                    if (forceUpdateVisible) {
                        ForceUpdateView(::gotoPlayStore)
                    } else {
                        MainScreen(
                            navController = navController,
                            admobManager = admobManager,
                            simpleSnackbar = simpleSnackbar,
                            openUrl = ::openUrl,
                            gotoPlayStore = ::gotoPlayStore,
                            loginWithGoogle = ::loginWithGoogle,
                        )
                    }

                    val noticeVisible = appNotice.available() && local.appNoticeId < appNotice.id
                    if (noticeVisible) {
                        NoticePopupView(
                            notice = appNotice,
                            onDismiss = {
                                if (appNotice.cancellable) {
                                    vm.dismissNotice()
                                }
                            },
                            onClick = {
                                trueAnalytics.clickButton("main__notice_close__click")
                                if (appNotice.cancellable) {
                                    local.appNoticeId = appNotice.id
                                    vm.dismissNotice()
                                } else {
                                    finishAffinity()
                                }
                            }
                        )
                    } // noticeVisible

                    // 면책 문구 팝업
                    if (disclaimerPopupVisible) {
                        DisclaimerPopupView(
                            onConfirm = {
                                trueAnalytics.clickButton("main__disclaimer_confirm__click")
                                disclaimerPopupVisible = false
                                local.disclaimerAccepted = true
                            },
                            onDismiss = {
                                disclaimerPopupVisible = false
                            }
                        )
                    }

                    simpleSnackbar.Host()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        navController?.handleDeepLink(intent)
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(intent)
        } catch (e: Exception) {
            logD("Failed to open URL: $url", e)
        }
    }

    private fun loginWithGoogle(
        onSuccess: LoginCallback = null,
    ) {
        logD("loginWithGoogle()")
        lifecycleScope.launch {
            val signInResult = googleAuthClient.signIn(this@MainActivity)
            if (signInResult.isSuccess) {
                logD("loginWithGoogle() success")
                onSuccess?.invoke()
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
