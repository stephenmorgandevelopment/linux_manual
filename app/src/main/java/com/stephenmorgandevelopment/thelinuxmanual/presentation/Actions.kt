package com.stephenmorgandevelopment.thelinuxmanual.presentation

interface Action
interface OptionsMenuAction

sealed interface LookupAction : Action {
    data class UpdateSearchText(val text: String) : LookupAction
}

sealed interface MainScreenAction : Action {
    data class TabSelected(val tabIndex: Int) : MainScreenAction
    data object CloseDatabase : MainScreenAction
    data class AddTab(val title: String, val itemId: Long) : MainScreenAction
    data class ShowOfflineDialog(val manPageId: Long) : MainScreenAction
    data class UpdateSubtitle(val subtitle: String) : MainScreenAction
}

sealed interface MainScreenOptionsMenuAction : OptionsMenuAction {
    data object CloseTab : MainScreenOptionsMenuAction
    data object ReSync : MainScreenOptionsMenuAction
    data class ChangeVersion(val version: String) : MainScreenOptionsMenuAction
    data object ToggleTabsOnBottom : MainScreenOptionsMenuAction
    data object ToggleSearchOnBottom : MainScreenOptionsMenuAction
    data object ShowPrivacyPolicyDialog : MainScreenOptionsMenuAction
}

sealed interface ManPageAction : Action {
    data object OnSearchPressed : ManPageAction
    data class OnSearchTextUpdated(val text: String) : ManPageAction
    data object OnNextPressed : ManPageAction
    data object OnPrevPressed : ManPageAction
}

sealed interface ManPageOptionsMenuAction : OptionsMenuAction {
    data class JumpTo(val section: String, val offset: Int) : ManPageOptionsMenuAction
    data object ToggleSearch : ManPageOptionsMenuAction
    data object Close : ManPageOptionsMenuAction
}

sealed interface DialogEvent {
    data object PrivacyPolicy : DialogEvent
    data object NoInternet : DialogEvent
}

sealed interface MenuOptions {
    data object CloseTab : MenuOptions
    data object ReSync : MenuOptions
    data object ToggleTabsOnBottom : MenuOptions
    data object ToggleSearchOnBottom : MenuOptions
    data object ShowPrivacyPolicyDialog : MenuOptions
    data object ToggleSearch : MenuOptions
    data object Close : MenuOptions
    data class ChangeVersion(val version: String) : MenuOptions
    data class JumpTo(val section: String, val offset: Int) : MenuOptions
}
