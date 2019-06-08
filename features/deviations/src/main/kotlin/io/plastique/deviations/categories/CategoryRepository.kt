package io.plastique.deviations.categories

import io.plastique.api.deviations.CategoryDto
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
        require(parent.hasChildren) { "Category has no children" }

        return getCategoriesFromDb(parent.path)
            .switchIfEmpty(getCategoriesFromServer(parent.path))
            .flattenAsObservable(Functions.identity())
            .map { categoryEntity -> categoryEntity.toCategory(parent) }
            .toList()
    }

    private fun getCategoriesFromDb(path: String): Maybe<List<CategoryEntity>> {
        return categoryDao.getSubcategories(path)
            .filter { categories -> categories.isNotEmpty() }
    }

    private fun getCategoriesFromServer(path: String): Single<List<CategoryEntity>> {
        return deviationService.getCategories(path)
            .map { categoryList -> persist(categoryList.categories) }
    }

    private fun persist(categories: List<CategoryDto>): List<CategoryEntity> {
        val entities = categories.map { it.toCategoryEntity() }
        categoryDao.insertOrUpdate(entities)
        return entities
    }
}

private fun CategoryDto.toCategoryEntity(): CategoryEntity =
    CategoryEntity(path = path, parent = parent, title = title, hasChildren = hasChildren)

private fun CategoryEntity.toCategory(parent: Category): Category {
    require(this.parent == parent.path) { "Expected Category with id ${this.parent} but got ${parent.path}" }
    return Category(path = path, title = title, parent = parent, hasChildren = hasChildren)
}
