package io.plastique.api.common

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class AccessScope(vararg val value: String)
