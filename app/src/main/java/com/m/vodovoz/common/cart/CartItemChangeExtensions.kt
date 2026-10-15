package com.m.vodovoz.common.cart

/** Вызывает action только для подтверждённого изменения Applied. */
inline fun CartItemChange.onSuccess(
    action: (CartItemChange.Applied) -> Unit,
): CartItemChange {
    if (this is CartItemChange.Applied) action(this)
    return this
}

/** Вызывает action только для Failed, передавая причину ошибки. */
inline fun CartItemChange.onFailure(
    action: (Throwable) -> Unit,
): CartItemChange {
    if (this is CartItemChange.Failed) action(error)
    return this
}
