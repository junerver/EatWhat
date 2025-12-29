package com.eatwhat

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.eatwhat.data.preferences.ThemeMode
import com.eatwhat.data.preferences.ThemePreferences
import com.eatwhat.data.sync.FileHelper
import com.eatwhat.navigation.EatWhatApp
import com.eatwhat.ui.theme.EatWhatTheme
import kotlinx.coroutines.launch
import xyz.junerver.compose.hooks._useGetState
import xyz.junerver.compose.hooks.invoke

/**
 * Main activity for EatWhat app
 * Entry point for the Compose UI
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

      val themePreferences = ThemePreferences(this)

        setContent {
          val themeMode by themePreferences.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

          // 处理分享意图
          val (initialPrompt, setInitialPrompt) = _useGetState<String?>(null)

          // 只在 Activity 创建时处理一次 Intent
          xyz.junerver.compose.hooks.useEffect(Unit) {
            handleIntent(intent) { prompt ->
              setInitialPrompt(prompt)
            }
          }

          EatWhatTheme(themeMode = themeMode) {
            EatWhatApp(
              initialPrompt = initialPrompt.value,
              onPromptConsumed = { setInitialPrompt(null) }
            )
          }
        }
    }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    // 注意：onNewIntent 此时无法直接更新 Compose 状态，
    // 除非我们把 handleIntent 逻辑移到 Compose 内部监听 intent 变化，
    // 或者简单重启 Activity。
    // 但由于我们使用了 Compose，通常我们会监听 State。
    // 这里为了简单，如果应用已经在运行，接收到新 Intent 可能需要重新触发 UI 更新。
    // 由于 Compose 的生命周期，这里可能不会自动刷新 initialPrompt。
    // 实际场景中可能需要更复杂的处理，或者直接 finish() 重启，但这里我们先假设从外部启动。
    // 为了更好的体验，可以在这里 recreate() 或者通过 event bus 通知。
    recreate()
  }

  private fun handleIntent(intent: Intent, onResult: (String?) -> Unit) {
    if (intent.action == Intent.ACTION_SEND) {
      when {
        intent.type?.startsWith("text/") == true -> {
          handleTextShare(intent, onResult)
        }
        intent.type?.startsWith("image/") == true -> {
          // 对于图片分享，尝试获取文字说明 (EXTRA_TEXT/EXTRA_TITLE)
          val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.getStringExtra(Intent.EXTRA_TITLE)
          onResult(text)
        }
        else -> onResult(null)
      }
    } else {
      onResult(null)
    }
  }

  private fun handleTextShare(intent: Intent, onResult: (String?) -> Unit) {
    intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
      onResult(text) // 直接分享的文本
    } ?: run {
      // 尝试获取文件流（针对文件分享）
      (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
        lifecycleScope.launch {
          try {
            val bytes = FileHelper.readFromUri(this@MainActivity, uri)
            val text = String(bytes)
            onResult(text)
          } catch (e: Exception) {
            e.printStackTrace()
            onResult(null)
          }
        }
      } ?: onResult(null)
        }
    }
}
