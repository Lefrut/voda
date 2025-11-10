package com.m.vodovoz.ui.paging

import com.m.vodovoz.design_system.model.VodovozItemUi
import com.m.vodovoz.design_system.model.withCanViewForAdults
import com.m.vodovoz.design_system.model.withUpdatedCartRecursive
import com.m.vodovoz.design_system.model.withUpdatedFavoritesRecursive
import com.m.vodovoz.design_system.model.withUpdatedLoadingsRecursive
import kotlinx.coroutines.flow.Flow

abstract class PagingProductsMviViewModel<ITEM : VodovozItemUi<ITEM>, S : PagingState<ITEM, S>, E>(
    state: S,
    override val blockedProductsFlow: Flow<Set<Long>>,
    override val favoritesFlow: Flow<Map<Long, Boolean>>,
    override val cartFlow: Flow<Map<Long, Int>>,
    override val canViewAdultProducts: Flow<Boolean>,
) : PagingMviViewModel<ITEM, S, E>(state), VodovozItemsListeners<ITEM>

abstract class PagingProductsMviViewModel2<ITEM1 : VodovozItemUi<ITEM1>, ITEM2 : VodovozItemUi<ITEM2>, S : PagingState2<ITEM1, ITEM2, S>, E>(
    state: S,
    override val blockedProductsFlow: Flow<Set<Long>>,
    override val favoritesFlow: Flow<Map<Long, Boolean>>,
    override val cartFlow: Flow<Map<Long, Int>>,
    override val canViewAdultProducts: Flow<Boolean>,
) : PagingMviViewModel2<ITEM1, ITEM2, S, E>(state), VodovozItemsListeners<ITEM1>

abstract class ProductsMviViewModel<ITEM : VodovozItemUi<ITEM>, S : ItemsState<ITEM, S>, E>(
    state: S,
    override val blockedProductsFlow: Flow<Set<Long>>,
    override val favoritesFlow: Flow<Map<Long, Boolean>>,
    override val cartFlow: Flow<Map<Long, Int>>,
    override val canViewAdultProducts: Flow<Boolean>,
) : ItemsMviViewModel<ITEM, S, E>(state), VodovozItemsListeners<ITEM>


internal interface VodovozItemsListeners<ITEM1 : VodovozItemUi<ITEM1>> {

    val blockedProductsFlow: Flow<Set<Long>>
    val favoritesFlow: Flow<Map<Long, Boolean>>
    val cartFlow: Flow<Map<Long, Int>>
    val canViewAdultProducts: Flow<Boolean>

    suspend fun <T> collectItemsWith(
        source: Flow<T>,
        map: suspend (List<ITEM1>, T) -> List<ITEM1>,
    )

    suspend fun listenCanViewAdult() = collectItemsWith(
        source = canViewAdultProducts,
        map = { items, canView ->
            items.withCanViewForAdults(canView)
        }
    )


    suspend fun listenProductLoadings() = collectItemsWith(
        source = blockedProductsFlow,
        map = { items, blocked ->
            items.withUpdatedLoadingsRecursive(blocked)
        }
    )

    suspend fun listenFavorites() = collectItemsWith(
        source = favoritesFlow,
        map = { items, favorites ->
            items.withUpdatedFavoritesRecursive(favorites)
        }
    )

    suspend fun listenCart() = collectItemsWith(
        source = cartFlow,
        map = { items, cart ->
            items.withUpdatedCartRecursive(cart)
        }
    )
}

internal interface VodovozItemsListeners2<ITEM1 : VodovozItemUi<ITEM1>, ITEM2 : VodovozItemUi<ITEM2>> {

    val blockedProductsFlow: Flow<Set<Long>>
    val favoritesFlow: Flow<Map<Long, Boolean>>
    val cartFlow: Flow<Map<Long, Int>>
    val canViewAdultProducts: Flow<Boolean>

    suspend fun <T> collectItemsWith1(
        source: Flow<T>,
        map: suspend (List<ITEM1>, T) -> List<ITEM1>,
    )

    suspend fun <T> collectItemsWith2(
        source: Flow<T>,
        map: suspend (List<ITEM2>, T) -> List<ITEM2>,
    )


    suspend fun listenCanViewAdult1() = collectItemsWith1(
        source = canViewAdultProducts,
        map = { items, canView -> items.withCanViewForAdults(canView) }
    )


    suspend fun listenProductLoadings1() = collectItemsWith1(
        source = blockedProductsFlow,
        map = { items, blocked ->
            items.withUpdatedLoadingsRecursive(blocked)
        }
    )

    suspend fun listenFavorites1() = collectItemsWith1(
        source = favoritesFlow,
        map = { items, favorites ->
            items.withUpdatedFavoritesRecursive(favorites)
        }
    )

    suspend fun listenCart1() = collectItemsWith1(
        source = cartFlow,
        map = { items, cart ->
            items.withUpdatedCartRecursive(cart)
        }
    )

    suspend fun listenCanViewAdult2() = collectItemsWith2(
        source = canViewAdultProducts,
        map = { items, canView -> items.withCanViewForAdults(canView) }
    )

    suspend fun listenProductLoadings2() = collectItemsWith2(
        source = blockedProductsFlow,
        map = { items, blocked -> items.withUpdatedLoadingsRecursive(blocked) }
    )

    suspend fun listenFavorites2() = collectItemsWith2(
        source = favoritesFlow,
        map = { items, favorites -> items.withUpdatedFavoritesRecursive(favorites) }
    )

    suspend fun listenCart2() = collectItemsWith2(
        source = cartFlow,
        map = { items, cart -> items.withUpdatedCartRecursive(cart) }
    )
}