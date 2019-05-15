package io.plastique.api.common

import com.squareup.moshi.JsonQualifier

@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class NullIfDeleted
