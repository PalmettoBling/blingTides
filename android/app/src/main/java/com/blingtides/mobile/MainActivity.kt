package com.blingtides.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository = TideRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val status = remember { mutableStateOf("Sync not started") }
            val sync = remember { WearTideSync(this) }
            val scope = rememberCoroutineScope()

            TideScreen(
                statusText = status.value,
                onSync = {
                    scope.launch {
                        val predictions = repository.fetchPredictions()
                        sync.sync(predictions)
                        status.value = "Synced ${predictions.size} tide points to watch"
                    }
                }
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TideScreen(
    statusText: String,
    onSync: () -> Unit
) {
    Scaffold(
        floatingActionButton = {
            Button(onClick = onSync) {
                Text("Sync Watch")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = WebViewClient()
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadUrl("file:///android_asset/web/index.html")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
