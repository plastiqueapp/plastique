package io.plastique.settings.licenses

import com.github.technoir42.rxjava2.junit5.OverrideSchedulersExtension
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.plastique.common.ErrorMessageProvider
import io.plastique.core.content.EmptyState
import io.plastique.settings.licenses.LicensesEvent.RetryClickEvent
import io.reactivex.Single
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

@ExtendWith(OverrideSchedulersExtension::class)
class LicensesViewModelTest {
    private val licenseRepository = mock<LicenseRepository>()
    private val errorMessageProvider = mock<ErrorMessageProvider>()
    private val viewModel = LicensesViewModel(LicensesStateReducer(errorMessageProvider), LicensesEffectHandler(licenseRepository))

    @Test
    fun `Load success`() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.just(listOf(LICENSE)))

        viewModel.state.test()
            .assertValuesOnly(
                LicensesViewState.Loading,
                LicensesViewState.Content(items = listOf(HeaderItem, LicenseItem(LICENSE))))
    }

    @Test
    fun `Load error`() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.error(IOException()))
        whenever(errorMessageProvider.getErrorState(any(), any())).thenReturn(ERROR_STATE)

        viewModel.state.test()
            .assertValuesOnly(
                LicensesViewState.Loading,
                LicensesViewState.Empty(emptyState = ERROR_STATE))
    }

    @Test
    fun `Emits last state to each new observer`() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.just(listOf(LICENSE)))

        val observer = viewModel.state.test()
        observer.cancel()

        viewModel.state.test()
            .assertValuesOnly(observer.values().last())
    }

    @Test
    fun retry() {
        whenever(licenseRepository.getLicenses()).thenReturn(Single.error(IOException()), Single.just(listOf(LICENSE)))
        whenever(errorMessageProvider.getErrorState(any(), any())).thenReturn(ERROR_STATE)

        val observer = viewModel.state
            .skip(2)
            .test()

        viewModel.dispatch(RetryClickEvent)

        observer.assertValuesOnly(
            LicensesViewState.Loading,
            LicensesViewState.Content(items = listOf(HeaderItem, LicenseItem(LICENSE))))
    }

    companion object {
        private val LICENSE = License(
            libraryName = "My Library",
            license = "Apache License 2.0",
            url = "https://acme.org")

        private val ERROR_STATE = EmptyState.MessageWithButton(messageResId = 0, buttonTextId = 0)
    }
}
