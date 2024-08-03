package com.ylabz.aifitmeal

import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class MealAppViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _calorieData = MutableStateFlow(0.0) // Replace with actual data type
    val calorieData: StateFlow<Double> = _calorieData

    private val _mealAppUiState = MutableStateFlow<MealAppUiState>(MealAppUiState.Success())
    val mealAppUiState: StateFlow<MealAppUiState> = _mealAppUiState.asStateFlow()




    var currentLocation by mutableStateOf<Location?>(null)

    /*
    private val _MealApp_uiState: MutableStateFlow<MealAppUiState> =
        MutableStateFlow(MealAppUiState.Success())
    val mealAppUiState: StateFlow<MealAppUiState> = _MealApp_uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.apiKeyGem
    )*/


    val dangerSafety = SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    val unknownSafety = SafetySetting(HarmCategory.UNKNOWN, BlockThreshold.NONE)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        // modelName = "gemini-pro-vision",
        // modelName = "gemini-1.5-pro-latest",
        // modelName = "gemini-pro-vision",
        // modelName = "gemma-2-27b-it",
        // modelName = "gemini-1.0-pro",
        apiKey = BuildConfig.apiKeyGem,
        safetySettings = listOf(
            dangerSafety
        )
    )


    private val generativeModelChat = GenerativeModel(
        //modelName = "gemini-1.5-flash-latest",
        modelName =  "gemini-1.5-pro-latest",
        apiKey = BuildConfig.apiKeyGem,
        safetySettings = listOf(
            dangerSafety
        )
    )

    private var chat : Chat = generativeModelChat.startChat(
        history = listOf(
            content(role = "user") { text("Hello, Any DIY info you can provide it will be greatly appreciated. Thank you.") },
        )
    )

    fun onEvent(event: MLEvent) {
        when (event) {
            is MLEvent.GenAiPromptResponseImg -> {
                sendPromptWithImage(event.prompt, event.index, event.image)
                Log.d("MealApp", "Called with image")
            }

            is MLEvent.GenAiChatResponseImg -> {
                sendChatWithImage(event.prompt, event.index, event.image)
                Log.d("MealApp", "Called Chat with image")
            }

            is MLEvent.GenAiChatResponseTxt -> {
                continueChatWithoutImage(event.prompt, event.index)
                Log.d("MealApp", "Called Chat without image")
            }


            /*is MLEvent.SetMemo -> {
                val old = MealAppUiState.Success().geminiResponses
                viewModelScope.launch {
                    _MealApp_uiState.value = MealAppUiState.Success(
                        geminiResponses = old,
                    )
                }
            }*/


            /*is MLEvent.StartCaptureSpeech2Txt -> {
                viewModelScope.launch {
                    audioFun.startSpeechToText(event.updateText, event.finished)
                }
            }*/

        }
    }


    private fun sendPromptWithImage(prompt: String, index: Int, image: Bitmap? = null) {
        Log.d("MealApp", "sendWithImage: $prompt")
        viewModelScope.launch(Dispatchers.IO) {
            //NOTE: Need to fix the loading state
            /*val currentState =
                (_mealAppUiState.value as? MealAppUiState.Loading) ?: MealAppUiState.Loading()
            _mealAppUiState.value = currentState.copy(isLoading = true)
            _mealAppUiState.value = currentState*/
            try {
                val response = generativeModel.generateContent(
                    content(role = "user") {
                        text(prompt)
                        image?.let { image(it) }
                    }
                )
                response.text?.let { outputContent: String ->
                    val currentState =
                        (_mealAppUiState.value as? MealAppUiState.Success) ?: MealAppUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _mealAppUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithImage: ${response.text}")
            } catch (e: Exception) {
                _mealAppUiState.value = MealAppUiState.Error(e.localizedMessage ?: "Error")
                Log.d("MealApp", "sendChatWithImage: ${e.localizedMessage}")
            }
        }
    }


    private fun sendChatWithImage(prompt: String, index: Int, image: Bitmap? = null) {
        Log.d("MealApp", "sendChatWithImage: $prompt")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = chat.sendMessage(
                    content(role = "user") {
                        text(prompt)
                        image?.let { image(it) }
                    }
                )
                response.text?.let { outputContent: String ->
                    val currentState =
                        (_mealAppUiState.value as? MealAppUiState.Success) ?: MealAppUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _mealAppUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithImage: ${response.text}")
            } catch (e: Exception) {
                _mealAppUiState.value = MealAppUiState.Error(e.localizedMessage ?: "Error")
                Log.d("MealApp", "sendChatWithImage: ${e.localizedMessage}")
            }
        }
    }

    private fun continueChatWithoutImage(prompt: String, index: Int) {
        Log.d("MealApp", "sendChatWithImage: $prompt")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = chat.sendMessage(
                    content(role = "user") {
                        text(prompt)
                    }
                )
                response.text?.let { outputContent: String ->
                    val currentState =
                        (_mealAppUiState.value as? MealAppUiState.Success) ?: MealAppUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _mealAppUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithoutImage: ${response.text}")
            } catch (e: Exception) {
                _mealAppUiState.value = MealAppUiState.Error(e.localizedMessage ?: "Error")
                Log.d("MealApp", "sendChatWithoutImage: ${e.localizedMessage}")
            }
        }
    }


}
