package com.stephenmorgandevelopment.thelinuxmanual.domain

import com.stephenmorgandevelopment.thelinuxmanual.repos.UbuntuRepository
import javax.inject.Inject

class LoadManPageUseCase @Inject constructor(
    private val ubuntuRepository: UbuntuRepository,

    ) {
}