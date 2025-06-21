package com.stephenmorgandevelopment.thelinuxmanual.presentation

interface Action

sealed interface LookupAction : Action {
    data class UpdateSearchText(val text: String): LookupAction
    data class ItemClick(val itemId: Long): LookupAction
    data class AddDescription(val itemId: Long): LookupAction
}

sealed interface ManPageAction : Action  {

}
