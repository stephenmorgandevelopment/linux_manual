package com.stephenmorgandevelopment.thelinuxmanual.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object Lookup

@Serializable
object PrivacyPolicy

@Serializable
object Offline

@Serializable
data class ManPage(val title: String, val manPageId: Long)
