package com.m.vodovoz.ui.dialog

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.VodovozTheme
import java.util.Locale

@Composable
fun SpeechDialogEntry() {
    VodovozTheme {
        val context = LocalContext.current
        val navigator = LocalNavigator.current
        val getSpeechResultLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    val query = result.data
                        ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                        ?.firstOrNull()
                        ?.ifBlank { null }
                        ?: return@rememberLauncherForActivityResult

                    navigator.navigateToSearch(query)
                }

                else -> {
                    navigator.goBack()
                }
            }
        }

        LaunchedEffect(Unit) {
            val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.speak))
            }

            getSpeechResultLauncher.launch(speechRecognizerIntent)
        }
    }
}
