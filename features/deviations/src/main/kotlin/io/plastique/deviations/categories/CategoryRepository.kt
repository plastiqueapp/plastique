package io.plastique.deviations.categories

import io.plastique.api.deviations.CategoryList
import io.plastique.api.deviations.DeviationService
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.internal.functions.Functions
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
    private val deviationService: DeviationService
) {
    fun getCategories(parent: Category): Single<List<Category>> {
        if (!parent.hasChildren) {
            throw IllegalArgumentException("Category has no subcategories")
        }
        return getCategoriesFromDb(parent.path)
                .switchIfEmpty(getCategoriesFromServer(parent.path))
                .flattenAsObservable(Functions.identity())
                .map { categoryEntity -> categoryEntity.toCategory(parent) }
                .toList()
    }

    private fun getCategoriesFromDb(path: String): Maybe<List<CategoryEntity>> {
        return categoryDao.getSubcategories(path)
                .filter { categories -> !categories.isEmpty() }
    }

    private fun getCategoriesFromServer(path: String): Single<List<CategoryEntity>> {
        return deviationService.getCategories(path)
                .map { categoryList -> persistCategories(categoryList) }
    }

    private fun persistCategories(categoryList: CategoryList): List<CategoryEntity> {
        val categories = categoryList.categories.map { category -> category.toCategoryEntity() }
        categoryDao.insertOrUpdate(categories)
        return categories
    }
}
