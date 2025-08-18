package com.vodovoz.app.ui.paging

import com.vodovoz.app.design_system.model.VodovozItemUi
import com.vodovoz.app.design_system.model.withCanViewForAdults
import com.vodovoz.app.design_system.model.withUpdatedCartRecursive
import com.vodovoz.app.design_system.model.withUpdatedFavoritesRecursive
import com.vodovoz.app.design_system.model.withUpdatedLoadingsRecursive
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
    override val canViewAdultProducts: Flow<Boolean>,
) : ItemsMviViewModel<ITEM, S, E>(state), VodovozItemsListeners<ITEM>


internal interface VodovozItemsListeners<ITEM : VodovozItemUi<ITEM>> {

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
            items.withCanViewForAdults(canView)
        }
    )


    suspend fun listenProductLoadings() = collectItemsWith(
        source = blockedProductsFlow,
        updateItems = { items, blocked ->
            items.withUpdatedLoadingsRecursive(blocked)
        }
    )

    suspend fun listenFavorites() = collectItemsWith(
        source = favoritesFlow,
        updateItems = { items, favorites ->
            items.withUpdatedFavoritesRecursive(favorites)
        }
    )

    suspend fun listenCart() = collectItemsWith(
        source = cartFlow,
        updateItems = { items, cart ->
            items.withUpdatedCartRecursive(cart)
        }
    )
}



