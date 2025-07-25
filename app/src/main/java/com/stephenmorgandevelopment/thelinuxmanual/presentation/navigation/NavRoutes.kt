package com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation

import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.ITEM_ID_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.TITLE_KEY
import kotlinx.serialization.Serializable

@Serializable
object Lookup

@Serializable
object PrivacyPolicy

@Serializable
object Offline

@Serializable
data class ManPage(val title: String, val manPageId: Long)

val ManPage.Companion.routeName: String get() = ManPage::class.java.name
val ManPage.Companion.routeDef: String get() = "$routeName/{$TITLE_KEY}/{$ITEM_ID_KEY}"

fun ManPage.toRoutePath(): String = "${ManPage.routeName}/$title/$manPageId"

