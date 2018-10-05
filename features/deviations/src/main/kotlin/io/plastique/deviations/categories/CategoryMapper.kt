package io.plastique.deviations.categories

import io.plastique.api.deviations.Category
import javax.inject.Inject

class CategoryMapper @Inject constructor() {
    fun map(category: Category): CategoryEntity {
        return CategoryEntity(
                path = category.path,
                parent = category.parent,
                title = category.title,
                hasChildren = category.hasChildren
        )
    }
}
