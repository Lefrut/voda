package com.vodovoz.app.ui.paging

import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.withUpdatedCart
import com.vodovoz.app.design_system.model.withUpdatedFavorites
import com.vodovoz.app.design_system.model.withUpdatedLoading
import kotlinx.coroutines.flow.Flow

abstract class PagingProductsMviViewModel<S : PagingState<ProductUi, S>, E>(
    state: S,
    private val blockedProductsFlow: Flow<Set<Long>>,
    private val favoritesFlow: Flow<Map<Long, Boolean>>,
    private val cartFlow: Flow<Map<Long, Int>>,
) : PagingMviViewModel<ProductUi, S, E>(state) {

    suspend fun listenProductLoadings() = collectItemsWith(
        source = blockedProductsFlow,
        updateItems = { items, blockedProducts ->
            items.withUpdatedLoading(blockedProducts)
        }
    )

    suspend fun listenFavorites() = collectItemsWith(
        source = favoritesFlow,
        updateItems = { items, favorites ->
            items.withUpdatedFavorites(favorites)
        }
    )

    suspend fun listenCart() = collectItemsWith(
        source = cartFlow,
        updateItems = { items, cart ->
            items.withUpdatedCart(cart)
        }
    )
}

