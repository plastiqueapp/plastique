package io.plastique.settings.licenses

import com.github.technoir42.rxjava2.junit5.OverrideSchedulersExtension
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.ContentState
import io.plastique.core.content.EmptyState
import io.reactivex.Single
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

@ExtendWith(OverrideSchedulersExtension::class)
class LicensesViewModelTest {
    private val licenseRepository = mock<LicenseRepository>()
    private val errorMessageProvider = mock<ErrorMessageProvider>()
    private val viewModel = LicensesViewModel(LicensesStateReducer(errorMessageProvider), LicensesEffectHandler(licenseRepository))

    @AfterEach
    fun tearDown() {
        viewModel.destroy()
    }

    @Test
    fun `Load success`() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.just(listOf(LICENSE)))

        viewModel.state.test()
            .assertValuesOnly(
                LicensesViewState(contentState = ContentState.Loading),
                LicensesViewState(contentState = ContentState.Content, items = listOf(HeaderItem, LicenseItem(LICENSE))))
    }

    @Test
    fun `Load error`() {
        val errorState = EmptyState.MessageWithButton(message = "Error", button = "Retry")
        whenever(licenseRepository.getLicenses()).thenReturn(Single.error(IOException()))
        whenever(errorMessageProvider.getErrorState(any(), any())).thenReturn(errorState)

        viewModel.state.test()
            .assertValuesOnly(
                LicensesViewState(contentState = ContentState.Loading),
                LicensesViewState(contentState = ContentState.Empty(isError = true, emptyState = errorState)))
    }

    @Test
    fun `Emits last state to each new observer`() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.just(listOf(LICENSE)))

        val ts = viewModel.state.test()
        ts.cancel()

        viewModel.state.test()
            .assertValuesOnly(ts.values().last())
    }
}

private val LICENSE = License(
    libraryName = "My Library",
    license = "Apache License 2.0",
    url = "https://acme.org")
