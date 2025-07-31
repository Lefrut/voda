package com.vodovoz.app.common.cart

import com.vodovoz.app.common.di.IoDispatcher
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartManager @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    @IoDispatcher
    private val dispatcher: CoroutineDispatcher,
) {

    private val cartMutex = Mutex()
    private val coroutineScope = CoroutineScope(dispatcher)

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

    private fun calculateCartWithoutChanges(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
    ): Map<Long, Int> {
        return cart.keys.associateWith { key ->
            val newValue = cartChanges[key] ?: return@associateWith cart[key] ?: 0
            val oldValue = firstCart[key] ?: 0
            cart.getOrDefault(key, 0) - (newValue - oldValue)
        }
    }


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

            val cartChanges = calculateCartChanges(
                firstCart ?: emptyMap(), cart
            )
            val firstCartCopy = firstCart?.toMap()

            if (cartChanges.isEmpty() || firstCartCopy == null) return@launch

            firstCart = null
            _blockedProductsState.update { s -> s + cartChanges.keys }

            firstCartCopy to cartChanges
        }

        sendOrRestoreData(
            firstCart = currentFirstCart,
            cartChanges = cartChanges,
            withLock = true
        ) {
            updateCartOnline(cartChanges, currentFirstCart)
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
            updateCart(emptyMap())
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

    private suspend fun addCart(cart: Map<Long, Int>) {
        for ((id, count) in cart) {
            val existing = this.cart[id] ?: 0
            this.cart[id] = existing + count
        }
        cartSharedFlow.emit(this.cart)
    }

    private suspend fun updateCartItem(id: Long, count: Int) {
        cart[id] = count
        cartSharedFlow.emit(cart)
    }


    suspend fun add(
        productIdWithQuantity: Map<Long, Int>,
    ) = coroutineScope.launch {
        val productIdsWithQuantity = formatCart(productIdWithQuantity)
        add(productIdsWithQuantity).join()
    }


    suspend fun add(
        vararg productIdWithQuantity: String,
    ) = coroutineScope.launch {
        val productIdsWithQuantity = formatCart(*productIdWithQuantity)
        add(productIdsWithQuantity).join()
    }


    suspend fun add(
        productIdsWithQuantity: String,
    ) = coroutineScope.launch {
        if (cartMutex.isLocked) return@launch

        cartMutex.withLock {
            val addInCart = parseCart(productIdsWithQuantity)
            if (blockedProductsState.value.any { id -> addInCart.contains(id) }) return@launch

            val currentCartVersion = ++cartVersion

            val firstCartCopy = firstCart?.toMap()
            val cartChanges = calculateCartChanges(
                firstCart = firstCart ?: emptyMap(),
                cart = cart
            )
            firstCart = null

            val cartWithoutAdd = cart.toMap()
            addCart(addInCart)
            val cartWithAdd = cart.toMap()

            _blockedProductsState.update { s -> s + addInCart.keys }

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
                    productIdsWithQuantity
                ).singleResult().getOrThrow()
            }


            _blockedProductsState.update { s ->
                s - addInCart.keys
            }
            if (currentCartVersion >= cartVersion) {
                updateCartListState(true)
            }
        }
    }

    private suspend fun sendOrRestoreData(
        firstCart: Map<Long, Int>,
        cartChanges: Map<Long, Int>,
        withLock: Boolean = true,
        timeout: Long = 4000L,
        operation: suspend () -> Unit,
    ) {
        runCatching {
            withTimeout(timeout) {
                operation()
            }
        }.onFailure {
            if (withLock) {
                cartMutex.lock()
            }

            val cartWithoutChanges = calculateCartWithoutChanges(
                firstCart, cartChanges
            )
            updateCart(cartWithoutChanges)

            if (withLock) {
                cartMutex.unlock()
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