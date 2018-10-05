package io.plastique.deviations.categories

import javax.inject.Inject

class CategoryEntityMapper @Inject constructor() {
    fun map(entity: CategoryEntity, parent: Category): Category {
        if (parent.path != entity.parent) {
            throw IllegalArgumentException("Expected Category with id " + entity.parent + " but got " + parent.path)
        }
        return Category(
                path = entity.path,
                title = entity.title,
                parent = parent,
                hasChildren = entity.hasChildren)
    }
}
