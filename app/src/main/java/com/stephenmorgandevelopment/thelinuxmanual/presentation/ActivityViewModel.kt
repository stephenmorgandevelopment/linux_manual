package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel<ScreenState>(savedStateHandle = savedStateHandle) {

    override val _state: MutableStateFlow<ScreenState>
        get() = TODO("Not yet implemented")

    override fun onAction(action: Action) {
        TODO("Not yet implemented")
    }
}