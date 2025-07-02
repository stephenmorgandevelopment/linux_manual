package com.stephenmorgandevelopment.thelinuxmanual.distros

/**
 * To add new releases to the app or remove broken ones.
 * Simply follow the pattern below.
 *
 * Available releases can be found at:   https://manpages.ubuntu.com/manpages/
 */
enum class AvailableReleases(
    val pathString: String,
) {
    BIONIC("bionic"),
    FOCAL("focal"),
    JAMMY("jammy"),
    KINETIC("kenetic"),
    LUNAR("lunar"),
    NOBLE("noble"),
    ORACULAR("oracular"),
    PLUCKY("plucky"),
    QUESTING("questing"),
    TRUSTY("trusty"),
    XENIAL("xenial");

    companion object {
        val releaseStrings get() = AvailableReleases.entries.map { it.pathString }
    }
}
