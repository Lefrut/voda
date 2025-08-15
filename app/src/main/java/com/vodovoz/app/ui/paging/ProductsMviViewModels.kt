package com.vodovoz.app.ui.paging

import com.vodovoz.app.design_system.model.VodovozItemUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedCartRecursive
import com.vodovoz.app.design_system.model.withUpdatedFavoritesRecursive
import com.vodovoz.app.design_system.model.withUpdatedLoading
import kotlinx.coroutines.flow.Flow

abstract class PagingProductsMviViewModel<ITEM : VodovozItemUi<ITEM>, S : PagingState<ITEM, S>, E>(
    state: S,
    override val blockedProductsFlow: Flow<Set<Long>>,
    override val favoritesFlow: Flow<Map<Long, Boolean>>,
    override val cartFlow: Flow<Map<Long, Int>>,
    override val canViewAdultProducts: Flow<Boolean>,
) : PagingMviViewModel<ITEM, S, E>(state), VodovozItemsListeners<ITEM>

abstract class ProductsMviViewModel<ITEM : VodovozItemUi<ITEM>, S : ItemsState<ITEM, S>, E>(
    state: S,
    override val blockedProductsFlow: Flow<Set<Long>>,
    override val favoritesFlow: Flow<Map<Long, Boolean>>,
    override val cartFlow: Flow<Map<Long, Int>>,
    override val canViewAdultProducts: Flow<Boolean> ,
) : ItemsMviViewModel<ITEM, S, E>(state), VodovozItemsListeners<ITEM>


interface VodovozItemsListeners<ITEM : VodovozItemUi<ITEM>> {

    val blockedProductsFlow: Flow<Set<Long>>
    val favoritesFlow: Flow<Map<Long, Boolean>>
    val cartFlow: Flow<Map<Long, Int>>
    val canViewAdultProducts: Flow<Boolean>

    suspend fun <T> collectItemsWith(
        source: Flow<T>,
        updateItems: suspend (List<ITEM>, T) -> List<ITEM>,
    )

    suspend fun listenCanViewAdult() = collectItemsWith(
        source = canViewAdultProducts,
        updateItems = { items, canView ->
            if (!canView) return@collectItemsWith items
            items.map { product -> product.copyItem(forAdults = null) }
        }
    )

    suspend fun listenProductLoadings() = collectItemsWith(
        source = blockedProductsFlow,
        updateItems = { items, blocked ->
            items.withUpdatedLoading(blocked)
        }
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun listenFavorites() = collectItemsWith(
        source = favoritesFlow,
        updateItems = { items, favs ->
            items.withUpdatedFavoritesRecursive(favs).mapNotNull { it as? ITEM }
        }
    )

    @Suppress("UNCHECKED_CAST")
    suspend fun listenCart() = collectItemsWith(
        source = cartFlow,
        updateItems = { items, cart ->
            items.withUpdatedCartRecursive(cart).mapNotNull { it as? ITEM }
        }
    )
}



