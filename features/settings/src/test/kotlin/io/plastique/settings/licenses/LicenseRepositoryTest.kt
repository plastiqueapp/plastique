package io.plastique.settings.licenses

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.moshi.Moshi
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Answers

class LicenseRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: LicenseRepository

    @BeforeEach
    fun setUp() {
        context = mock(defaultAnswer = Answers.RETURNS_DEEP_STUBS)
        repository = LicenseRepository(context, Moshi.Builder().build())
    }

    @Test
    fun getLicenses() {
        @Language("JSON")
        val json = """[
  {
    "name": "Library Name",
    "description": "Library Description",
    "license": "Apache License 2.0",
    "url": "https://acme.org"
  }
]
"""
        whenever(context.assets.open(any())).thenReturn(json.byteInputStream())

        repository.getLicenses()
                .test()
                .assertResult(listOf(License(
                        libraryName = "Library Name",
                        libraryDescription = "Library Description",
                        license = "Apache License 2.0",
                        url = "https://acme.org")))
    }
}
