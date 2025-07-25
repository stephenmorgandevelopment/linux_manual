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
    data object CloseTab : MainScreenAction, OptionsMenuAction
    data class ShowOfflineDialog(val manPageId: Long) : MainScreenAction
}

sealed interface MainScreenOptionsMenuAction : OptionsMenuAction {
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
    data class OnScroll(val sectionName: String) : ManPageAction
}

sealed interface ManPageOptionsMenuAction : OptionsMenuAction {
    data class JumpTo(val section: String, val offset: Int) : ManPageOptionsMenuAction
    data object ToggleSearch : ManPageOptionsMenuAction
    data object Close : ManPageOptionsMenuAction
}

sealed interface ShowDialogEvents {
    data object PrivacyPolicy : ShowDialogEvents
    data object NoInternet : ShowDialogEvents
}
