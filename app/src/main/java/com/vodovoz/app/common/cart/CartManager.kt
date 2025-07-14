package com.vodovoz.app.common.cart

import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    private val cartMutex = Mutex()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val updateCartListListener = MutableStateFlow(false)
    fun observeUpdateCartList() = updateCartListListener.asStateFlow()
    fun updateCartListState(update: Boolean) {
        updateCartListListener.value = update
    }

    private val cart = ConcurrentHashMap<Long, Int>()
    private var firstCart: Map<Long, Int>? = null
    private val cartSharedFlow = MutableSharedFlow<Map<Long, Int>>(replay = 1)
    private val _blockedProductsState = MutableStateFlow(emptySet<Long>())
    val blockedProductsState = _blockedProductsState.asStateFlow()
    var cartVersion = 0
        private set


    fun observeCarts() = cartSharedFlow.asSharedFlow()

    suspend fun change(productId: Long, count: Int) = coroutineScope.launch {
        val currentCartVersion = cartMutex.withLock {
            if (_blockedProductsState.value.contains(productId) || count < 0) return@launch

            val cartBeforeUpdate = cart.toMap()
            updateCartItem(productId, count)
            if (firstCart == null) {
                firstCart = cartBeforeUpdate
            }
            return@withLock ++cartVersion
        }

        delay(365L)

        val (currentFirstCart, cartChanges) = cartMutex.withLock {

            if (currentCartVersion < cartVersion) return@launch

            val firstCartCopy = firstCart?.toMap() ?: return@launch
            firstCart = null

            val cartChanges = cart.filter { (key, value) ->
                val oldValue = firstCartCopy[key]
                value != oldValue
            }

            if (cartChanges.isEmpty()) return@launch

            _blockedProductsState.update { s -> s + cartChanges.keys }

            firstCartCopy to cartChanges
        }


        kotlin.runCatching {
            withTimeout(4000) {
                updateCartOnline(cartChanges, currentFirstCart)
            }
        }.onFailure {
            cartMutex.withLock {
                val cartWithoutChanges = cart.keys.associateWith { key ->
                    val newValue = cartChanges[key] ?: return@associateWith cart[key] ?: 0
                    val oldValue = currentFirstCart[key] ?: 0
                    cart.getOrDefault(key, 0) - (newValue - oldValue)
                }
                updateCart(cartWithoutChanges)
            }
        }

        cartMutex.withLock {
            _blockedProductsState.update { productIds -> productIds - cartChanges.keys }
            if (currentCartVersion >= cartVersion) {
                updateCartListState(true)
            }
        }
    }

    suspend fun clearCart() {
        cartMutex.withLock {
            cartVersion++
            cart.clear()
            cartSharedFlow.emit(emptyMap())
            _blockedProductsState.update { emptySet() }
            updateCartListState(true)
        }
    }

    suspend fun syncCart(newCart: Map<Long, Int>) = cartMutex.withLock {
        if (firstCart != null || blockedProductsState.value.isNotEmpty()) {
            return@withLock
        }
        updateCart(newCart)
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

    private suspend fun updateCart(cart: Map<Long, Int>) {
        this.cart.clear()
        this.cart.putAll(cart)
        cartSharedFlow.emit(this.cart)
    }


    private suspend fun updateCartItem(id: Long, count: Int) {
        cart[id] = count
        cartSharedFlow.emit(cart)
    }


    suspend fun add(
        productIdWithQuantity: Map<Long, Int>,
    ) {
        val productIdsWithQuantity = formatCart(productIdWithQuantity)
        add(productIdsWithQuantity)
    }


    suspend fun add(
        vararg productIdWithQuantity: String,
    ) {
        val productIdsWithQuantity = formatCart(*productIdWithQuantity)
        add(productIdsWithQuantity)
    }


    suspend fun add(
        productIdsWithQuantity: String,
    ) = coroutineScope.launch {
        if (cartMutex.isLocked) return@launch

        cartMutex.withLock {

            val addInCart = parseCart(productIdsWithQuantity)


            if (blockedProductsState.value.any { id -> addInCart.contains(id) }) return@launch

            val currentCartVersion = ++cartVersion

            val firstCartCopy = firstCart?.toMap() ?: cart
            val cartChanges = cart.filter { (key, value) ->
                val oldValue = firstCartCopy[key]
                value != oldValue
            }
            firstCart = null
            _blockedProductsState.update { s -> s + addInCart.keys + cartChanges.keys }

            for ((key, value) in addInCart) {
                updateCartItem(key, (cart[key] ?: 0) + value)
            }


            runCatching {
                if (cartChanges.isNotEmpty()) {
                    updateCartOnline(cartChanges, firstCartCopy)
                }

                vodovozServiceRepository.addMultipleProductsToCart(
                    productIdsWithQuantity
                ).singleResult()
            }.onFailure {
                for ((key, value) in addInCart) {
                    updateCartItem(key, ((cart[key] ?: 0) - value).coerceAtLeast(0))
                }
            }

            _blockedProductsState.update { s -> s - addInCart.keys - cartChanges.keys }

            if (currentCartVersion >= cartVersion) {
                updateCartListState(true)
            }
        }
    }



    fun formatCart(cart: Map<Long, Int>): String {
        return cart.entries.joinToString(";") { "${it.key}-${it.value}" }
    }

    private fun formatCart(vararg productIdWithQuantity: String): String {
        return productIdWithQuantity.toList().joinToString(";")
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