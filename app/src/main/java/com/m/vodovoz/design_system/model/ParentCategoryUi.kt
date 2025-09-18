package com.m.vodovoz.design_system.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.common.model.DataAllAction
import com.m.vodovoz.domain.general.model.product.ParentCategoryModel
import com.m.vodovoz.feature.home.model.CategoryUi
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class ParentCategoryUi(
    val id: Long,
    val name: String,
    val picture: String,
    val action: DataAllAction?,
    val countChildren: Int,
    val childCategories: List<ParentCategoryUi>,
) : Parcelable {

    companion object {
        val Empty = ParentCategoryUi(-1, "", "", DataAllAction.Unknown, -1, emptyList())
    }

}

fun List<ParentCategoryUi>.traverseTree(
    block: (node: ParentCategoryUi, parent: ParentCategoryUi?) -> Boolean,
): ParentCategoryUi? {
    fun traverse(node: ParentCategoryUi, parent: ParentCategoryUi?): ParentCategoryUi? {
        if (block(node, parent)) return node
        node.childCategories.forEach { child ->
            traverse(child, node)?.let { return it }
        }
        return null
    }

    for (root in this) {
        traverse(root, null)?.let { return it }
    }
    return null
}

fun List<ParentCategoryUi>.findSiblingsOf(categoryId: Int): List<ParentCategoryUi> {
    return traverseTree { node, _ ->
        node.childCategories.any { it.id == categoryId.toLong() }
    }?.childCategories.orEmpty()
}

fun List<ParentCategoryUi>.findParentOfOnlyLeaf(): ParentCategoryUi? {
    val leafParentPairs = mutableListOf<Pair<ParentCategoryUi, ParentCategoryUi>>()

    traverseTree { node, parent ->
        if (node.childCategories.isEmpty() && parent != null) {
            leafParentPairs += node to parent
        }
        false
    }

    return if (leafParentPairs.size == 1) leafParentPairs.first().second else null
}

fun ParentCategoryUi.allCategories(): List<ParentCategoryUi> {
    return listOf(this) + childCategories.flatMap { category ->
        category.allCategories()
    }
}

fun List<ParentCategoryUi>.allCategories(): List<ParentCategoryUi> {
    return map { it.allCategories() }.flatten()
}


fun ParentCategoryUi.toCategory(): CategoryUi {
    return CategoryUi(name, id.toInt())
}

fun ParentCategoryModel.toUi(): ParentCategoryUi {
    return ParentCategoryUi(
        id = id,
        name = name,
        picture = picture,
        action = action,
        countChildren = countChildren,
        childCategories = childCategories.map { it.toUi() }
    )
}
