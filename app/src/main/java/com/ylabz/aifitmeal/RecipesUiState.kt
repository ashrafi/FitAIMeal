package com.ylabz.aifitmeal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface RecipesUiState {

    /**
     * Still loading
     */
    data class Loading(
        var loading: Boolean = true,
    ) : RecipesUiState


    /**
     * Text has been generated
     */
    data class Success(
        var geminiResponses: List<String> = listOf("", ""),
        var calorieData : Double? = null
    ) : RecipesUiState {
        var responses by mutableStateOf(geminiResponses)
    }

    /**
     * There was an error generating text
     */
    data class Error(
        val errorMessage: String,
        var calorieData : Double? = null
    ) : RecipesUiState
}
