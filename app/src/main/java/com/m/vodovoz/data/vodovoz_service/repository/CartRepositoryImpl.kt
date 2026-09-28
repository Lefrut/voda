package com.m.vodovoz.data.vodovoz_service.repository

import com.m.vodovoz.domain.general.model.product.toCartProducts
import com.m.vodovoz.domain.general.respository.CartRepository
import com.m.vodovoz.domain.general.respository.CartRequest
import com.m.vodovoz.util.extensions.firstResult
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepositoryImpl
) : CartRepository {

    override suspend fun execute(request: CartRequest): Result<Unit> =
        with(vodovozServiceRepository) {
            when (request) {
                CartRequest.Clear -> {
                    clearCart()
                }

                is CartRequest.AddProducts -> {
                    addMultipleProductsToCart(
                        request.items.toCartProducts().productsIdsWithQuantity
                    )
                }

                is CartRequest.ReplaceBottles -> {
                    replaceMultipleBottlesToCart(request.items.toCartProducts())
                }

                is CartRequest.AddProduct -> {
                    addProductToCart(request.id, request.quantity)
                }

                is CartRequest.UpdateProduct -> {
                    updateProductInCart(request.id, request.quantity)
                }
            }.map { it.mapCatching { Unit } }.firstResult()
        }
}
