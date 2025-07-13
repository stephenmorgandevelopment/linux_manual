package com.stephenmorgandevelopment.thelinuxmanual.presentation

import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageViewModel.Companion.ITEM_ID_KEY
import com.stephenmorgandevelopment.thelinuxmanual.presentation.ManPageViewModel.Companion.TITLE_KEY
import kotlinx.serialization.Serializable

@Serializable
object Lookup

@Serializable
object PrivacyPolicy

@Serializable
data class ManPage(val title: String, val manPageId: Long)

val ManPage.Companion.routeName: String get() = ManPage::class.java.name
val ManPage.Companion.routeDef: String get() = "$routeName/{$TITLE_KEY}/{$ITEM_ID_KEY}"

fun ManPage.toRoutePath(): String = "${ManPage.routeName}/$title/$manPageId"

