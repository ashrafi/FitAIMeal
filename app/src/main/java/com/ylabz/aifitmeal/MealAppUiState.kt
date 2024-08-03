package com.ylabz.aifitmeal

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * A sealed hierarchy describing the state of the text generation.
 */
sealed interface MealAppUiState {

    /**
     * Still loading
     */
    data class Loading(
        var loading: Boolean = true,
    ) : MealAppUiState


    /**
     * Text has been generated
     */
    data class Success(
        var geminiResponses: List<String> = listOf("", ""),
        var currLocation : Location? = null
    ) : MealAppUiState {
        var responses by mutableStateOf(geminiResponses)
    }

    /**
     * There was an error generating text
     */
    data class Error(
        val errorMessage: String,
        var currLocation : Location? = null
    ) : MealAppUiState
}
