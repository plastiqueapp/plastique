package io.plastique.collections

import io.plastique.api.collections.CollectionService
import io.plastique.core.exceptions.ApiResponseException
import io.plastique.deviations.DeviationDao
import io.plastique.util.Result
import io.plastique.util.toResult
import io.reactivex.Completable
import javax.inject.Inject

class FavoritesModelImpl @Inject constructor(
    private val collectionService: CollectionService,
    private val deviationDao: DeviationDao
) : FavoritesModel {

    override fun setFavorite(deviationId: String, favorite: Boolean): Completable = if (favorite) {
        addToFavorites(deviationId)
    } else {
        removeFromFavorites(deviationId)
    }

    private fun addToFavorites(deviationId: String): Completable {
        return collectionService.addToFolder(deviationId = deviationId, folderId = null)
                .toResult()
                .map { result ->
                    when (result) {
                        is Result.Success ->
                            deviationDao.setFavorite(deviationId, true, result.value.numFavorites)

                        is Result.Error -> {
                            val error = result.error
                            if (error is ApiResponseException && error.errorData.code == ERROR_CODE_ALREADY_FAVORITE) {
                                deviationDao.setFavorite(deviationId, true)
                            } else {
                                throw result.error
                            }
                        }
                    }
                }
                .ignoreElement()
    }

    private fun removeFromFavorites(deviationId: String): Completable {
        return collectionService.removeFromFolder(deviationId = deviationId, folderId = null)
                .toResult()
                .map { result ->
                    when (result) {
                        is Result.Success ->
                            deviationDao.setFavorite(deviationId, false, result.value.numFavorites)

                        is Result.Error -> {
                            val error = result.error
                            if (error is ApiResponseException && error.errorData.code == ERROR_CODE_NOT_FAVORITE) {
                                deviationDao.setFavorite(deviationId, false)
                            } else {
                                throw result.error
                            }
                        }
                    }
                }
                .ignoreElement()
    }

    companion object {
        private const val ERROR_CODE_ALREADY_FAVORITE = 1
        private const val ERROR_CODE_NOT_FAVORITE = 1
    }
}
