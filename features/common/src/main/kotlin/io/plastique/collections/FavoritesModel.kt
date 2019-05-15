package io.plastique.collections

import io.reactivex.Completable

interface FavoritesModel {
    fun setFavorite(deviationId: String, favorite: Boolean): Completable
}
