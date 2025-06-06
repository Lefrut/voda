package com.vodovoz.app.feature.addresses.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
enum class AddressScreenTypeUi: Parcelable {
    Add, Choose;
}