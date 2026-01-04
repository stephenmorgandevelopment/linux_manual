@file:OptIn(DelicateCoroutinesApi::class)

package com.stephenmorgandevelopment.thelinuxmanual.presentation

import androidx.compose.runtime.Stable
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Stable
class OptionsMenuHandler {
    private val _events = MutableSharedFlow<MenuOptions>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events = _events.asSharedFlow()

    internal fun triggerEvent(menuAction: OptionsMenuAction) {
        val menuOption = when (menuAction) {
            MainScreenOptionsMenuAction.CloseTab -> MenuOptions.CloseTab
            MainScreenOptionsMenuAction.ReSync -> MenuOptions.ReSync
            MainScreenOptionsMenuAction.ToggleTabsOnBottom -> MenuOptions.ToggleTabsOnBottom
            MainScreenOptionsMenuAction.ToggleSearchOnBottom -> MenuOptions.ToggleSearchOnBottom
            MainScreenOptionsMenuAction.ShowPrivacyPolicyDialog -> MenuOptions.ShowPrivacyPolicyDialog
            ManPageOptionsMenuAction.ToggleSearch -> MenuOptions.ToggleSearch
            ManPageOptionsMenuAction.Close -> MenuOptions.Close

            is MainScreenOptionsMenuAction.ChangeVersion -> MenuOptions.ChangeVersion(menuAction.version)
            is ManPageOptionsMenuAction.JumpTo -> MenuOptions.JumpTo(
                menuAction.section,
                menuAction.offset
            )

            else -> return
        }

        _events.tryEmit(menuOption)
    }
}

internal fun MenuOptions.toAction() = when (this) {
    MenuOptions.CloseTab -> MainScreenOptionsMenuAction.CloseTab
    MenuOptions.ReSync -> MainScreenOptionsMenuAction.ReSync
    MenuOptions.ToggleTabsOnBottom -> MainScreenOptionsMenuAction.ToggleTabsOnBottom
    MenuOptions.ToggleSearchOnBottom -> MainScreenOptionsMenuAction.ToggleSearchOnBottom
    MenuOptions.ShowPrivacyPolicyDialog -> MainScreenOptionsMenuAction.ShowPrivacyPolicyDialog
    MenuOptions.ToggleSearch -> ManPageOptionsMenuAction.ToggleSearch
    MenuOptions.Close -> MainScreenOptionsMenuAction.CloseTab
    is MenuOptions.ChangeVersion -> MainScreenOptionsMenuAction.ChangeVersion(version)
    is MenuOptions.JumpTo -> ManPageOptionsMenuAction.JumpTo(
        section,
        offset
    )
}
