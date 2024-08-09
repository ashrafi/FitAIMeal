package com.ylabz.aifitmeal

import android.graphics.Bitmap

// All direct subclasses of a sealed class are known at compile time.
sealed class RecipesEvent {
    data class GenAiPromptResponseImg(val prompt: String, val image: Bitmap, val index: Int) : RecipesEvent()
    data class GenAiChatResponseImg(val prompt: String, val image: Bitmap, val index: Int) : RecipesEvent()
    data class GenAiChatResponseTxt(val prompt: String, val index: Int) : RecipesEvent()
    object StartNewChat : RecipesEvent()

}

