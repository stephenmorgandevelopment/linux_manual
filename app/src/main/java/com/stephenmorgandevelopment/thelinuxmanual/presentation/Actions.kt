package com.stephenmorgandevelopment.thelinuxmanual.presentation

interface Action
interface OptionsMenuAction

sealed interface LookupAction : Action {
    data class UpdateSearchText(val text: String) : LookupAction
    /**
     * Originally tried to initiate description fetching from the view model, but
     * decided the best way to choose which commands we fetch descriptions for is by
     * the ones that get rendered on screen to the user.
     */
//    data class AddDescription(val itemId: Long) : LookupAction
}

sealed interface MainScreenAction : Action {
    data class TabSelected(val tabIndex: Int) : MainScreenAction
    data object CloseDatabase : MainScreenAction
    data class AddTab(val title: String, val itemId: Long) : MainScreenAction
    data object CloseTab : MainScreenAction, OptionsMenuAction
}

sealed interface MainScreenOptionsMenuAction : OptionsMenuAction {
    data object ReSync : MainScreenOptionsMenuAction
    data object ToggleTabsOnBottom : MainScreenOptionsMenuAction
    data object ToggleSearchOnBottom : MainScreenOptionsMenuAction
    data class ChangeVersion(val version: String) : MainScreenOptionsMenuAction
    data object TogglePrivacyPolicyVisible : MainScreenOptionsMenuAction
}

sealed interface ManPageAction : Action {
    data object Search : ManPageAction
    data class UpdateSearchText(val text: String) : ManPageAction
    data object NextSearchMatch : ManPageAction
    data object PrevSearchMatch : ManPageAction
    data class RestoreSearchState(val searchState: ManPageSearchState) : ManPageAction
}

sealed interface ManPageOptionsMenuAction : OptionsMenuAction {
    data class JumpTo(val section: String, val offset: Int = 0) : ManPageOptionsMenuAction
    data object ToggleSearch : ManPageOptionsMenuAction
    data object Close : ManPageOptionsMenuAction
}
