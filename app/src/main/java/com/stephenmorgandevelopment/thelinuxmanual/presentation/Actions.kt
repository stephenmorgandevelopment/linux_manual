package com.stephenmorgandevelopment.thelinuxmanual.presentation

sealed interface Action

sealed interface LookupAction : Action {
    data class UpdateSearchText(val text: String): LookupAction
//    data class ItemClick(val itemId: Long, val url: String): LookupAction
    /**
     * Originally tried to initiate description fetching from the view model, but
     * decided the best way to choose which commands we fetch descriptions for is by
     * the ones that get rendered on screen to the user.
     */
    data class AddDescription(val itemId: Long): LookupAction
}

sealed interface MainScreenAction : Action {
    data object ReSync : MainScreenAction
    data object ToggleTabsOnBottom : MainScreenAction
    data object ToggleSearchOnBottom : MainScreenAction
    data class ChangeVersion(val version: String) : MainScreenAction
    data class TabSelected(val tabIndex: Int) : MainScreenAction
    data object CloseDatabase : MainScreenAction
    data class ItemClick(val itemId: Long, val url: String) : MainScreenAction
}

sealed interface ManPageAction : Action {
    data class JumpTo(val section: String) : ManPageAction
    data class Search(val text: String) : ManPageAction
    data object Close : ManPageAction
}
