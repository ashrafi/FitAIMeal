package com.ylabz.aifitmeal

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
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


class RecipesViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val _calorieData = MutableStateFlow(0.0) // Replace with actual data type
    val calorieData: StateFlow<Double> = _calorieData

    private val _recipesUiState = MutableStateFlow<RecipesUiState>(RecipesUiState.Success())
    val recipesUiState: StateFlow<RecipesUiState> = _recipesUiState.asStateFlow()


    /*
    private val _MealApp_uiState: MutableStateFlow<RecipesUiState> =
        MutableStateFlow(RecipesUiState.Success())
    val recipesUiState: StateFlow<RecipesUiState> = _MealApp_uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash-latest",
        apiKey = BuildConfig.apiKeyGem
    )*/


    val dangerSafety = SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.NONE)
    val unknownSafety = SafetySetting(HarmCategory.UNKNOWN, BlockThreshold.NONE)

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-pro",
        // modelName = "gemini-1.5-flash",
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
        modelName =  "gemini-1.5-pro",
        apiKey = BuildConfig.apiKeyGem,
        safetySettings = listOf(
            dangerSafety
        )
    )

    private var chat : Chat = generativeModelChat.startChat(
        history = listOf(
            content(role = "user") { text("Hello,Thank you for your help.") },
        )
    )

    fun onEvent(event: RecipesEvent) {
        when (event) {
            is RecipesEvent.GenAiPromptResponseImg -> {
                sendPromptWithImage(event.prompt, event.index, event.image)
                Log.d("MealApp", "Called with image")
            }

            is RecipesEvent.StartNewChat -> {
                chat = generativeModelChat.startChat(
                    history = listOf(
                        content(role = "user") { text("Hello,Thank you for your help.") },
                    )
                )
            }

            is RecipesEvent.GenAiChatResponseImg -> {
                sendChatWithImage(event.prompt, event.index, event.image)
                Log.d("MealApp", "Called Chat with image")
            }

            is RecipesEvent.GenAiChatResponseTxt -> {
                continueChatWithoutImage(event.prompt, event.index)
                Log.d("MealApp", "Called Chat without image")
            }


            /*is RecipesEvent.SetMemo -> {
                val old = RecipesUiState.Success().geminiResponses
                viewModelScope.launch {
                    _MealApp_uiState.value = RecipesUiState.Success(
                        geminiResponses = old,
                    )
                }
            }*/


            /*is RecipesEvent.StartCaptureSpeech2Txt -> {
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
                (_recipesUiState.value as? RecipesUiState.Loading) ?: RecipesUiState.Loading()
            _recipesUiState.value = currentState.copy(isLoading = true)
            _recipesUiState.value = currentState*/
            try {
                val response = generativeModel.generateContent(
                    content(role = "user") {
                        text(prompt)
                        image?.let { image(it) }
                    }
                )
                response.text?.let { outputContent: String ->
                    val currentState =
                        (_recipesUiState.value as? RecipesUiState.Success) ?: RecipesUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _recipesUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithImage: ${response.text}")
            } catch (e: Exception) {
                _recipesUiState.value = RecipesUiState.Error(e.localizedMessage ?: "Error")
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
                        (_recipesUiState.value as? RecipesUiState.Success) ?: RecipesUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _recipesUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithImage: ${response.text}")
            } catch (e: Exception) {
                _recipesUiState.value = RecipesUiState.Error(e.localizedMessage ?: "Error")
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
                        (_recipesUiState.value as? RecipesUiState.Success) ?: RecipesUiState.Success()
                    val updatedResponses = currentState.geminiResponses.toMutableList().apply {
                        this[index] = outputContent
                    }
                    _recipesUiState.value = currentState.copy(geminiResponses = updatedResponses)
                }
                Log.d("MealApp", "sendChatWithoutImage: ${response.text}")
            } catch (e: Exception) {
                _recipesUiState.value = RecipesUiState.Error(e.localizedMessage ?: "Error")
                Log.d("MealApp", "sendChatWithoutImage: ${e.localizedMessage}")
            }
        }
    }


}
