package com.pushback.app.usage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pushback.app.PushBackApp
import com.pushback.app.ui.components.PbButton
import com.pushback.app.ui.components.PbGhostButton
import com.pushback.app.ui.theme.Cream
import com.pushback.app.ui.theme.Debt
import com.pushback.app.ui.theme.Night
import com.pushback.app.ui.theme.PbTheme
import com.pushback.app.ui.theme.PbTypography
import kotlinx.coroutines.launch

class WasteInterruptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val remaining = intent.getIntExtra(EXTRA_REMAINING, 0)
        val appLabel = intent.getStringExtra(EXTRA_APP) ?: "that app"
        val repo = (application as PushBackApp).container.repo
        setContent {
            PbTheme {
                val colors = com.pushback.app.ui.theme.PushBackTheme.colors
                val typography = com.pushback.app.ui.theme.PushBackTheme.typography
                val scope = rememberCoroutineScope()
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(colors.debt)
                        .padding(28.dp),
                ) {
                    Text("★ DISTRACTION INTERCEPTED ★", style = typography.label.copy(color = colors.cream))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        remaining.toString(),
                        style = typography.display.copy(color = colors.cream, fontSize = 88.sp, lineHeight = 80.sp),
                    )
                    Text(
                        "You still have $remaining applications due today.",
                        style = typography.title.copy(color = colors.cream, fontSize = 26.sp),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "$appLabel is not the work. That is not how runway or ownership is built.",
                        style = typography.body.copy(color = colors.cream.copy(alpha = 0.9f)),
                    )
                    Spacer(Modifier.height(36.dp))
                    PbButton("Back to the Frog", modifier = Modifier.fillMaxWidth(), color = colors.night, textColor = colors.cream) {
                        finish()
                    }
                    Spacer(Modifier.height(12.dp))
                    PbGhostButton("5-Minute Emergency Break (Then Return)", borderColor = colors.cream, textColor = colors.cream) {
                        scope.launch {
                            repo.settings.update {
                                it.copy(allowedBreakUntil = System.currentTimeMillis() + 5 * 60_000)
                            }
                            finish()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_REMAINING = "remaining"
        const val EXTRA_APP = "app"
    }
}
