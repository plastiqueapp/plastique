package io.plastique.deviations.categories.list

import com.sch.neon.Effect
import io.plastique.deviations.categories.Category

sealed class CategoryListEffect : Effect() {
    data class LoadCategoryEffect(val category: Category) : CategoryListEffect()
}
