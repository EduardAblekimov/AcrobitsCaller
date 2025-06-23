package net.acrobits.interview.test.core.di

import net.acrobits.interview.test.data.repository.SipRepositoryImpl
import net.acrobits.interview.test.data.sdk.SipSdk
import net.acrobits.interview.test.data.sdk.SipSdkImpl
import net.acrobits.interview.test.domain.repository.SipRepository

/**
 * A simple manual dependency injection container. Provides instances of repositories.
 * In a larger application, a DI framework like Hilt or Koin would be preferable.
 */
class AppModule() {

    private val sipSdk: SipSdk by lazy { SipSdkImpl() }
    val sipRepository: SipRepository by lazy { SipRepositoryImpl(sipSdk) }
}