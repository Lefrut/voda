package com.m.vodovoz.common.cart

import com.m.vodovoz.common.di.DefaultDispatcher
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    @DefaultDispatcher
    private val dispatcher: CoroutineDispatcher,
) : AbstractCartManager() {

    private val coroutineScope = CoroutineScope(dispatcher)
    private val refreshCartState = MutableStateFlow(false)
    fun observeRefreshCart() = refreshCartState.asStateFlow()

    fun updateRefreshCart(refresh: Boolean) {
        refreshCartState.update { refresh }
    }

    private val cart = ConcurrentHashMap<Long, Int>()
    private var firstCart: Map<Long, Int>? = null
    private val cartSharedFlow = MutableSharedFlow<Map<Long, Int>>(1)
    private val _blockedProductsFlow = MutableStateFlow(emptySet<Long>())
    val blockedProductsFlow = _blockedProductsFlow.asStateFlow()

    private var firstCartProductId: Long? = null
    var cartVersion = 0
        private set

    fun observeCarts() = cartSharedFlow.asSharedFlow()


    private fun takePendingProductChangeLocked(
        productId: Long,
    ): Pair<Map<Long, Int>, Map<Long, Int>>? {
        val firstCartCopy = (firstCart ?: return null).toMap()

        val cartChanges = calculateCartChanges(
            firstCart = firstCartCopy,
            cart = cart,
            productId = productId,
        )
        firstCart = null
        firstCartProductId = null

        if (cartChanges.isEmpty()) return null

        _blockedProductsFlow.update { s ->
            s + cartChanges.keys
        }

        return firstCartCopy to cartChanges
    }

    fun change(
        productId: Long,
        count: Int,
        onSuccess: (CartItemQuantityChange) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) = coroutineScope.launch {
        var forcedChange: Pair<Map<Long, Int>, Map<Long, Int>>? = null
        var forcedCartVersion = 0

        val currentCartVersion = sendMutex.withLock {
            if (_blockedProductsFlow.value.contains(productId) || count < 0) return@launch
            val pendingProductId = firstCartProductId

            if (
                firstCart != null &&
                pendingProductId != null &&
                pendingProductId != productId
            ) {
                forcedCartVersion = cartVersion
                forcedChange = takePendingProductChangeLocked(
                    productId = pendingProductId,
                )
            }

            val cartBeforeUpdate = cart.toMap()

            setCartItem(productId, count)

            if (firstCart == null) {
                firstCart = cartBeforeUpdate
                firstCartProductId = productId
            }

            return@withLock ++cartVersion
        }

        forcedChange?.let { (forcedFirstCart, forcedCartChanges) ->
            coroutineScope.launch {
                sendChange(
                    firstCart = forcedFirstCart,
                    cartChanges = forcedCartChanges,
                    currentCartVersion = forcedCartVersion
                )
            }
        }

        delay(365L)

        val (currentFirstCart, cartChanges) = sendMutex.withLock {
            if (firstCartProductId != productId) return@launch
            if (currentCartVersion < cartVersion) return@launch

            takePendingProductChangeLocked(productId) ?: return@launch
        }

        sendChange(
            firstCart = currentFirstCart,
            cartChanges = cartChanges,
            currentCartVersion = currentCartVersion,
            onSuccess = onSuccess,
            onFailure = onFailure,
        )
    }

    private suspend fun sendChange(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
        currentCartVersion: Int,
        onSuccess: (CartItemQuantityChange) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) {

        sendOrRestoreData(
            firstCart = firstCart,
            cartChanges = cartChanges,
            withLock = true,
        ) {
            updateCartOnline(
                needUpdate = cartChanges,
                firstCart = firstCart,
            )
        }.onFailure { throwable ->
            onFailure(throwable)
        }.onSuccess {
            val quantityChange = createCartItemQuantityChange(
                firstCart = firstCart,
                cartChanges = cartChanges,
            )
            if (quantityChange != null) {
                onSuccess(quantityChange)
            } else {
                onFailure(
                    IllegalStateException(
                        "Expected single cart change, but got ${cartChanges.keys}"
                    )
                )
            }
        }

        sendMutex.withLock {
            unblockProducts(
                blockedProductsIds = cartChanges.keys,
                currentCartVersion = currentCartVersion,
            )
        }
    }

    private fun unblockProducts(blockedProductsIds: Set<Long>, currentCartVersion: Int) {
        _blockedProductsFlow.update { productIds -> productIds - blockedProductsIds }
        if (currentCartVersion >= cartVersion) {
            updateRefreshCart(true)
        }
    }


    private suspend fun updateCartOnline(
        needUpdate: Map<Long, Int>,
        firstCart: Map<Long, Int>,
    ) = coroutineScope {
        needUpdate.map { (productId, quantity) ->
            async {
                val exists = (firstCart[productId] ?: 0) > 0
                val flow = if (exists) {
                    vodovozServiceRepository.updateProductInCart(productId, quantity)
                } else {
                    vodovozServiceRepository.addProductToCart(productId, quantity)
                }
                flow.singleResult().getOrThrow()
            }
        }.awaitAll()
    }

    private suspend fun setCart(newCart: Map<Long, Int>) {
        cart.clear()
        cart.putAll(newCart)
        cartSharedFlow.emit(cart.toMap())
    }

    private fun createCartItemQuantityChange(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
    ): CartItemQuantityChange? {
        val (productId, currentCount) = cartChanges.entries.singleOrNull() ?: return null

        return CartItemQuantityChange(
            productId = productId,
            previousCount = firstCart[productId] ?: 0,
            currentCount = currentCount,
        )
    }


    private suspend fun plusCart(otherCart: Map<Long, Int>) {
        for ((id, count) in otherCart) {
            val existing = cart[id] ?: 0
            cart[id] = existing + count
        }
        cartSharedFlow.emit(cart.toMap())
    }

    private suspend fun setCartItem(id: Long, count: Int) {
        cart[id] = count
        cartSharedFlow.emit(cart.toMap())
    }


    suspend fun clearCart() = sendMutex.withLock {
        cartVersion++
        setCart(emptyMap())
        _blockedProductsFlow.update { emptySet() }
        updateRefreshCart(true)
    }

    suspend fun syncCart(newCart: Map<Long, Int>) = sendMutex.withLock {
        if (firstCart != null || blockedProductsFlow.value.isNotEmpty()) {
            return@withLock
        }
        setCart(newCart)
    }


    fun <T : Any> addMultiple(
        cartItems: T,
        onSuccess: (Map<Long, CartItemQuantityChange>) -> Unit = {},
        onFailure: (Throwable) -> Unit = {},
    ) = coroutineScope.launch {
        var formattedCartItems = ""
        var cartBeforeAdd = emptyMap<Long, Int>()
        var addCartChanges = emptyMap<Long, Int>()
        var addProductIds = emptySet<Long>()

        val currentCartVersion = sendMutex.withLock {
            formattedCartItems = formatCart(cartItems).ifEmpty {
                return@launch
            }

            val addInCart = parseCart(formattedCartItems)
            addProductIds = addInCart.keys

            val loadingProductIds = _blockedProductsFlow.value.intersect(addProductIds)

            if (loadingProductIds.isNotEmpty()) {
                return@launch
            }

            cartBeforeAdd = cart.toMap()

            plusCart(addInCart)

            addCartChanges = addInCart.mapValues { (id, addedCount) ->
                (cartBeforeAdd[id] ?: 0) + addedCount
            }

            _blockedProductsFlow.update { blockedIds ->
                blockedIds + addProductIds
            }

            return@withLock ++cartVersion
        }

        sendOrRestoreData(
            firstCart = cartBeforeAdd,
            cartChanges = addCartChanges,
            withLock = true,
        ) {
            vodovozServiceRepository.addMultipleProductsToCart(
                formattedCartItems,
            ).singleResult().getOrThrow()
        }.onSuccess {
            val successChanges = addCartChanges.mapValues { (id, currentCount) ->
                CartItemQuantityChange(
                    productId = id,
                    previousCount = cartBeforeAdd[id] ?: 0,
                    currentCount = currentCount,
                )
            }
            onSuccess(successChanges)
        }.onFailure { throwable ->
            onFailure(throwable)
        }

        sendMutex.withLock {
            unblockProducts(
                blockedProductsIds = addProductIds,
                currentCartVersion = currentCartVersion,
            )
        }
    }

    private suspend fun sendOrRestoreData(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
        withLock: Boolean = true,
        timeout: Long = 5000L,
        operation: suspend () -> Unit,
    ): Result<Unit> {
        val result = runCatching {
            withTimeout(timeout) {
                operation()
            }
        }

        result.onFailure {
            if (withLock) {
                sendMutex.lock()
            }

            val cartWithoutChanges = cart.calculateCartWithoutChanges(
                firstCart, cartChanges
            )
            setCart(cartWithoutChanges)

            if (withLock) {
                sendMutex.unlock()
            }
        }

        return result
    }


    fun <T : Any> formatCart(
        cart: T,
        formatters: List<CartFormatter<*>> = listOf(
            mapCartFormatter,
            listCartFormatter,
        ),
    ): String {
        formatters.forEach { formatter ->
            try {
                @Suppress("UNCHECKED_CAST")
                return (formatter as CartFormatter<T>).format(cart)
            } catch (_: ClassCastException) {
            }
        }
        return ""
    }

    private fun parseCart(productsIdsWithQuantity: String): Map<Long, Int> {
        return productsIdsWithQuantity
            .split(";")
            .filter { it.isNotBlank() }.associate {
                val (id, count) = it.split("-")
                id.toLong() to count.toInt()
            }
    }
}

data class CartItemQuantityChange(
    val productId: Long,
    val previousCount: Int,
    val currentCount: Int,
) {

    val delta = currentCount - previousCount

}

fun interface CartFormatter<in T : Any> {
    fun format(cart: T): String
}

val listCartFormatter = CartFormatter<List<*>> { list ->
    list.joinToString(";")
}
val mapCartFormatter = CartFormatter<Map<*, *>> { map ->
    map.entries.joinToString(";") { "${it.key}-${it.value}" }
}


private fun calculateCartChanges(
    firstCart: Map<Long, Int>,
    cart: Map<Long, Int>,
    productId: Long? = null,
): Map<Long, Int> {
    val productIds = productId
        ?.let { setOf(it) }
        ?: (firstCart.keys + cart.keys)

    return productIds.mapNotNull { id ->
        val oldCount = firstCart[id] ?: 0
        val newCount = cart[id] ?: 0

        if (oldCount == newCount) {
            null
        } else {
            id to newCount
        }
    }.toMap()
}

private fun Map<Long, Int>.calculateCartWithoutChanges(
    firstCart: Map<Long, Int>,
    cartChanges: Map<Long, Int>,
): Map<Long, Int> {
    return keys.associateWith { key ->
        val newValue = cartChanges[key] ?: return@associateWith get(key) ?: 0
        val oldValue = firstCart[key] ?: 0
        getOrDefault(key, 0) - (newValue - oldValue)
    }
}
