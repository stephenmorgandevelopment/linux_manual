package com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation

import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.ITEM_ID_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.viewmodels.ManPageViewModel.Companion.TITLE_KEY
import kotlinx.serialization.Serializable

@Serializable
sealed interface NavRoutes

@Serializable
object Lookup: NavRoutes

@Serializable
object PrivacyPolicy: NavRoutes

@Serializable
object Offline: NavRoutes

@Serializable
data class ManPage(val title: String, val manPageId: Long): NavRoutes

val ManPage.Companion.routeName: String get() = ManPage::class.java.name
val ManPage.Companion.routeDef: String get() = "$routeName/{$TITLE_KEY}/{$ITEM_ID_KEY}"

fun ManPage.toRoutePath(): String = "${ManPage.routeName}/$title/$manPageId"

