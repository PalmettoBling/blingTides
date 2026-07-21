package com.blingtides.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repository = TideRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val statusText = remember { mutableStateOf("Syncing on startup…") }
            val sync = remember { WearTideSync(this) }
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }

            // Auto-sync tide data to the watch as soon as the app opens.
            LaunchedEffect(Unit) {
                runCatching {
                    val predictions = repository.fetchPredictions()
                    sync.sync(predictions)
                    statusText.value = "Synced ${predictions.size} tide points"
                }.onFailure {
                    statusText.value = "Sync failed — check connection"
                    snackbarHostState.showSnackbar(statusText.value)
                }
            }

            MaterialTheme {
                TideScreen(
                    statusText = statusText.value,
                    snackbarHostState = snackbarHostState,
                    onSync = {
                        scope.launch {
                            statusText.value = "Syncing…"
                            runCatching {
                                val predictions = repository.fetchPredictions()
                                sync.sync(predictions)
                                statusText.value = "Synced ${predictions.size} tide points"
                            }.onFailure {
                                statusText.value = "Sync failed — check connection"
                                snackbarHostState.showSnackbar(statusText.value)
                            }
                        }
                    }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TideScreen(
    statusText: String,
    snackbarHostState: SnackbarHostState,
    onSync: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Button(
                    onClick = onSync,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sync Watch")
                }
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
