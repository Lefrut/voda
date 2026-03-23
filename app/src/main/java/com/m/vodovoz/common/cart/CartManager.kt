package com.m.vodovoz.common.cart

import com.m.vodovoz.common.di.DefaultDispatcher
import com.m.vodovoz.common.di.IoDispatcher
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
    var cartVersion = 0
        private set

    fun observeCarts() = cartSharedFlow.asSharedFlow()

    fun change(
        productId: Long,
        count: Int,
        onFailure: (Throwable) -> Unit = {},
    ) = coroutineScope.launch {
        val currentCartVersion = cartMutex.withLock {
            if (_blockedProductsFlow.value.contains(productId) || count < 0) return@launch
            val cartBeforeUpdate = cart.toMap()
            setCartItem(productId, count)
            if (firstCart == null) {
                firstCart = cartBeforeUpdate
            }
            return@withLock ++cartVersion
        }

        delay(365L)

        val (currentFirstCart, cartChanges) = cartMutex.withLock {
            if (currentCartVersion < cartVersion) return@launch
            val cartChanges = calculateCartChanges(
                firstCart ?: emptyMap(), cart
            )
            val firstCartCopy = firstCart?.toMap()
            if (cartChanges.isEmpty() || firstCartCopy == null) return@launch
            firstCart = null
            _blockedProductsFlow.update { s -> s + cartChanges.keys }
            firstCartCopy to cartChanges
        }

        sendOrRestoreData(
            firstCart = currentFirstCart,
            cartChanges = cartChanges,
            withLock = true
        ) {
            updateCartOnline(cartChanges, currentFirstCart)
        }.onFailure { throwable ->
            onFailure(throwable)
        }
        
        cartMutex.withLock {
            unblockProducts(cartChanges.keys, currentCartVersion)
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

    private suspend fun plusCart(otherCart: Map<Long, Int>) {
        for ((id, count) in otherCart) {
            val existing = cart[id] ?: 0
            cart[id] = existing + count
        }
        cartSharedFlow.emit(cart.toMap())
    }

    private suspend fun setCartItem(id: Long, count: Int) {
        cart[id] = count
        cartSharedFlow.emit(cart)
    }


    suspend fun clearCart() = cartMutex.withLock {
        cartVersion++
        setCart(emptyMap())
        _blockedProductsFlow.update { emptySet() }
        updateRefreshCart(true)
    }

    suspend fun syncCart(newCart: Map<Long, Int>) = cartMutex.withLock {
        if (firstCart != null || blockedProductsFlow.value.isNotEmpty()) {
            return@withLock
        }
        setCart(newCart)
    }


    suspend fun <T : Any> add(
        cartItems: T,
    ) = coroutineScope.launch {
        if (cartMutex.isLocked) return@launch

        cartMutex.withLock {
            val formattedCartItems = formatCart(cartItems).ifEmpty { return@launch }
            val addInCart = parseCart(formattedCartItems)
            if (blockedProductsFlow.value.any { id -> addInCart.contains(id) }) return@launch

            val currentCartVersion = ++cartVersion

            val firstCartCopy = firstCart?.toMap()
            val cartChanges = calculateCartChanges(
                firstCart = firstCart ?: emptyMap(),
                cart = this@CartManager.cart
            )
            firstCart = null

            val cartWithoutAdd = this@CartManager.cart.toMap()
            plusCart(addInCart)
            val cartWithAdd = this@CartManager.cart.toMap()

            _blockedProductsFlow.update { s -> s + addInCart.keys }

            if (cartChanges.isNotEmpty() && firstCartCopy != null) {
                sendOrRestoreData(
                    firstCart = firstCartCopy,
                    cartChanges = cartChanges,
                    withLock = false
                ) {
                    updateCartOnline(
                        firstCart = firstCartCopy,
                        needUpdate = cartChanges
                    )
                }
            }

            sendOrRestoreData(
                firstCart = cartWithoutAdd,
                cartChanges = cartWithAdd,
                withLock = false
            ) {
                vodovozServiceRepository.addMultipleProductsToCart(
                    formattedCartItems
                ).singleResult().getOrThrow()
            }

            unblockProducts(
                addInCart.keys,
                currentCartVersion
            )
        }
    }

    private suspend fun sendOrRestoreData(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
        withLock: Boolean = true,
        timeout: Long = 4000L,
        operation: suspend () -> Unit,
    ): Result<Unit> {
        val result = runCatching {
            withTimeout(timeout) {
                operation()
            }
        }

        result.onFailure {
            if (withLock) {
                cartMutex.lock()
            }

            val cartWithoutChanges = cart.calculateCartWithoutChanges(
                firstCart, cartChanges
            )
            setCart(cartWithoutChanges)

            if (withLock) {
                cartMutex.unlock()
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
): Map<Long, Int> {
    val firstCartCopy = firstCart.toMap()
    val cartChanges = cart.filter { (key, value) ->
        val oldValue = firstCartCopy[key]
        value != oldValue
    }

    return cartChanges
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
