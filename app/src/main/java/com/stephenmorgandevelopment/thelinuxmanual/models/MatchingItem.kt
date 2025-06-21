package com.stephenmorgandevelopment.thelinuxmanual.models

data class MatchingItem(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val url: String,
    val manN: Int,
) {
    constructor(simpleCommand: SimpleCommand) :
            this(
                simpleCommand.id,
                simpleCommand.name,
                simpleCommand.description,
                simpleCommand.url,
                simpleCommand.manN,
            )

    fun withId(id: Long): MatchingItem = this.copy(id = id)
    fun withDescription(description: String): MatchingItem = this.copy(description = description)
}

fun SimpleCommand.toMatchingItem() = MatchingItem(this)