package com.pushback.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pushback.app.notify.AlarmIds
import com.pushback.app.notify.CoachNotifications
import com.pushback.app.ui.PushBackNav
import com.pushback.app.ui.mapDeepLink
import com.pushback.app.ui.theme.Ink
import com.pushback.app.ui.theme.PbTheme
import com.pushback.app.usage.WatchdogService
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels {
        AppViewModel.Factory((application as PushBackApp).container.repo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val deep = mapDeepLink(intent.getStringExtra(AlarmIds.EXTRA_ROUTE))
        setContent {
            val settings by vm.settings.collectAsStateWithLifecycle()
            val themeMode = settings?.themeMode ?: "MORNING"
            PbTheme(themeMode = themeMode) {
                if (settings == null) {
                    Box(Modifier.fillMaxSize().background(com.pushback.app.ui.theme.PushBackTheme.colors.ink))
                } else {
                    PushBackNav(vm, settings!!, deep.takeIf { settings!!.onboardingDone })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val repo = (application as PushBackApp).container.repo
        lifecycleScope.launch {
            val remaining = repo.remainingRequired()
            val watch = repo.settings.current().watchdogEnabled
            CoachNotifications.stickyQuota(this@MainActivity, remaining)
            if (remaining > 0 && watch) WatchdogService.start(this@MainActivity)
            else if (remaining <= 0) WatchdogService.stop(this@MainActivity)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
