package com.m.vodovoz.feature.main

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.annotation.VisibleForTesting
import androidx.compose.ui.Alignment
import androidx.compose.material3.Icon
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import com.m.vodovoz.common.webview.WebViewEntry
import com.m.vodovoz.common.webview.api.WebViewNavKey
import com.m.vodovoz.R
import com.m.vodovoz.common.media.ImagePickerEntry
import com.m.vodovoz.common.media.api.ImagePickerNavKey
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavEntryDecorator
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey
import com.m.vodovoz.core.navigation.viewmodel.rememberSharedViewModelStoreNavEntryDecorator
import com.m.vodovoz.design_system.robotoFontFamily
import com.m.vodovoz.feature.about_app.AboutAppEntry
import com.m.vodovoz.feature.about_app.api.AboutAppNavKey
import com.m.vodovoz.feature.about_product.AboutProductEntry
import com.m.vodovoz.feature.about_product.api.AboutProductNavKey
import com.m.vodovoz.feature.addresses.AddressesEntry
import com.m.vodovoz.feature.addresses.add.AddAddressEntry
import com.m.vodovoz.feature.addresses.add.api.AddAddressNavKey
import com.m.vodovoz.feature.addresses.api.AddressesNavKey
import com.m.vodovoz.feature.all.brands.AllBrandsEntry
import com.m.vodovoz.feature.all.brands.api.AllBrandsNavKey
import com.m.vodovoz.feature.all.orders.detail.OrderDetailsEntry
import com.m.vodovoz.feature.all.orders.detail.api.OrderDetailsNavKey
import com.m.vodovoz.feature.all.orders.detail.traceorder.TraceOrderEntry
import com.m.vodovoz.feature.all.orders.detail.traceorder.api.TraceOrderNavKey
import com.m.vodovoz.feature.all.orders.history.OrdersHistoryEntry
import com.m.vodovoz.feature.all.orders.history.api.OrdersHistoryNavKey
import com.m.vodovoz.feature.all.promotions.AllPromotionsEntry
import com.m.vodovoz.feature.all.promotions.api.AllPromotionsNavKey
import com.m.vodovoz.feature.auth.login.LoginEntry
import com.m.vodovoz.feature.auth.login.api.LoginNavKey
import com.m.vodovoz.feature.auth.login_by_email.LoginByEmailEntry
import com.m.vodovoz.feature.auth.login_by_email.api.LoginByEmailNavKey
import com.m.vodovoz.feature.auth.login_by_phone_code.LoginByPhoneCodeEntry
import com.m.vodovoz.feature.auth.login_by_phone_code.api.LoginByPhoneCodeNavKey
import com.m.vodovoz.feature.auth.recover_password.RecoverPasswordEntry
import com.m.vodovoz.feature.auth.recover_password.api.RecoverPasswordNavKey
import com.m.vodovoz.feature.auth.reg.RegisterEntry
import com.m.vodovoz.feature.auth.reg.api.RegisterNavKey
import com.m.vodovoz.feature.bottom.services.AboutServicesEntry
import com.m.vodovoz.feature.bottom.services.api.AboutServicesNavKey
import com.m.vodovoz.feature.bottom.services.detail.ServiceDetailEntry
import com.m.vodovoz.feature.bottom.services.detail.api.ServiceDetailNavKey
import com.m.vodovoz.feature.buy_certificate.BuyCertificateEntry
import com.m.vodovoz.feature.buy_certificate.api.BuyCertificateNavKey
import com.m.vodovoz.feature.cancel_order.CancelOrderEntry
import com.m.vodovoz.feature.cancel_order.api.CancelOrderNavKey
import com.m.vodovoz.feature.cart.CartEntry
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.cart.api.CartNavKey
import com.m.vodovoz.feature.cart.bottles.AllBottlesEntry
import com.m.vodovoz.feature.cart.bottles.api.AllBottlesNavKey
import com.m.vodovoz.feature.cart.gifts.GiftsEntry
import com.m.vodovoz.feature.cart.gifts.api.GiftsNavKey
import com.m.vodovoz.feature.cart.ordering.OrderingEntry
import com.m.vodovoz.feature.cart.ordering.api.OrderingNavKey
import com.m.vodovoz.feature.catalog.CatalogEntry
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.catalog.api.CatalogNavKey
import com.m.vodovoz.feature.categories.CategoriesEntry
import com.m.vodovoz.feature.categories.api.CategoriesNavKey
import com.m.vodovoz.feature.certificate_activation.CertificateActivationEntry
import com.m.vodovoz.feature.certificate_activation.api.CertificateActivationNavKey
import com.m.vodovoz.feature.delivery_date.DeliveryDateEntry
import com.m.vodovoz.feature.delivery_date.api.DeliveryDateNavKey
import com.m.vodovoz.feature.document_viewer.DocumentViewerEntry
import com.m.vodovoz.feature.document_viewer.api.DocumentViewerNavKey
import com.m.vodovoz.feature.faq.FAQEntry
import com.m.vodovoz.feature.faq.api.FAQNavKey
import com.m.vodovoz.feature.favorite.FavoriteEntry
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.favorite.api.FavoriteNavKey
import com.m.vodovoz.feature.filter_values.FilterValuesEntry
import com.m.vodovoz.feature.filter_values.api.FilterValuesNavKey
import com.m.vodovoz.feature.home.HomeEntry
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.home.api.HomeNavKey
import com.m.vodovoz.feature.map.MapEntry
import com.m.vodovoz.feature.map.api.MapNavKey
import com.m.vodovoz.feature.order_call_you.OrderCallYouEntry
import com.m.vodovoz.feature.order_call_you.api.OrderCallYouNavKey
import com.m.vodovoz.feature.order_question.OrderQuestionEntry
import com.m.vodovoz.feature.order_question.api.OrderQuestionNavKey
import com.m.vodovoz.feature.order_recipient.OrderRecipientEntry
import com.m.vodovoz.feature.order_recipient.api.OrderRecipientNavKey
import com.m.vodovoz.feature.payment_method.PaymentMethodEntry
import com.m.vodovoz.feature.payment_method.api.PaymentMethodNavKey
import com.m.vodovoz.feature.preorder.PreOrderEntry
import com.m.vodovoz.feature.preorder.api.PreOrderNavKey
import com.m.vodovoz.feature.product_analogs.ProductAnalogsEntry
import com.m.vodovoz.feature.product_analogs.api.ProductAnalogsNavKey
import com.m.vodovoz.feature.product_catalog.ProductCatalogEntry
import com.m.vodovoz.feature.product_catalog.api.ProductCatalogNavKey
import com.m.vodovoz.feature.product_comments.ProductCommentsEntry
import com.m.vodovoz.feature.product_comments.api.ProductCommentsNavKey
import com.m.vodovoz.feature.product_details.ProductDetailsEntry
import com.m.vodovoz.feature.product_details.api.ProductDetailsNavKey
import com.m.vodovoz.feature.product_details.detail_media.DetailMediaEntry
import com.m.vodovoz.feature.product_details.detail_media.api.DetailMediaNavKey
import com.m.vodovoz.feature.product_filters.ProductFiltersEntry
import com.m.vodovoz.feature.product_filters.api.ProductFiltersNavKey
import com.m.vodovoz.feature.profile.ProfileEntry
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.feature.profile.api.ProfileNavKey
import com.m.vodovoz.feature.profile.change_password.ChangePasswordEntry
import com.m.vodovoz.feature.profile.change_password.api.ChangePasswordNavKey
import com.m.vodovoz.feature.profile.notification_settings.NotificationSettingsEntry
import com.m.vodovoz.feature.profile.notification_settings.api.NotificationSettingsNavKey
import com.m.vodovoz.feature.profile.user_data.UserDataEntry
import com.m.vodovoz.feature.profile.user_data.api.UserDataNavKey
import com.m.vodovoz.feature.profile.waterapp.WaterAppEntry
import com.m.vodovoz.feature.profile.waterapp.api.WaterAppNavKey
import com.m.vodovoz.feature.promotion_details.PromotionDetailsEntry
import com.m.vodovoz.feature.promotion_details.api.PromotionDetailsNavKey
import com.m.vodovoz.feature.questionnaires.QuestionnairesEntry
import com.m.vodovoz.feature.questionnaires.api.QuestionnairesNavKey
import com.m.vodovoz.feature.search.SearchEntry
import com.m.vodovoz.feature.search.api.SearchNavKey
import com.m.vodovoz.feature.search.qrcode.QrCodeEntry
import com.m.vodovoz.feature.search.qrcode.api.QrCodeNavKey
import com.m.vodovoz.feature.service_order.ServiceOrderEntry
import com.m.vodovoz.feature.service_order.api.ServiceOrderNavKey
import com.m.vodovoz.feature.stories_fragment.StoriesEntry
import com.m.vodovoz.feature.stories_fragment.api.StoriesNavKey
import com.m.vodovoz.feature.sub_categories.SubCategoriesEntry
import com.m.vodovoz.feature.sub_categories.api.SubCategoriesNavKey
import com.m.vodovoz.feature.wait_feedback_products.WaitFeedbackProductsEntry
import com.m.vodovoz.feature.wait_feedback_products.api.WaitFeedbackProductsNavKey
import com.m.vodovoz.feature.write_comment.WriteCommentEntry
import com.m.vodovoz.feature.write_comment.api.WriteCommentNavKey
import com.m.vodovoz.feature.write_message.WriteMessageEntry
import com.m.vodovoz.feature.write_message.api.WriteMessageNavKey
import com.m.vodovoz.ui.dialog.SpeechDialogEntry
import com.m.vodovoz.ui.dialog.api.SpeechDialogNavKey
import kotlinx.serialization.Serializable
import java.lang.reflect.Modifier as ReflectModifier


@Composable
fun BottmNav(
    homeViewModel: HomeFlowViewModel,
    catalogFlowViewModel: CatalogFlowViewModel,
    favoriteFlowViewModel: FavoriteFlowViewModel,
    profileFlowViewModel: ProfileFlowViewModel,
    cartFlowViewModel: CartFlowViewModel,
    tabManager: TabManager
) {

    val navigationState = rememberNavigationState(
        startKey = BottomNavKey.Home,
        topLevelKeys = BottomNavKey.values
    )
    val navigator = remember { Navigator(navigationState) }

    val showBottomBar by tabManager.observeShowBottomBar().collectAsStateWithLifecycle()
    val cartState by tabManager.observeBottomNavCartState().collectAsStateWithLifecycle()




    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .consumeWindowInsets(WindowInsets.systemBars),
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    selectedKey = navigationState.currentTopLevelKey as? BottomNavKey,
                    cartState = cartState,
                    onItemClick = navigator::navigate,
                )
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { paddingValues ->

        val entryProvider = entryProvider<NavKey> {
            entry<BottomNavKey.Home> {
                HomeEntry(homeViewModel)
            }
            entry<BottomNavKey.Profile> {
                ProfileEntry(profileFlowViewModel)
            }
            entry<BottomNavKey.Cart> {
                CartEntry(cartFlowViewModel)
            }
            entry<BottomNavKey.Catalog> {
                CatalogEntry(catalogFlowViewModel)
            }
            entry<BottomNavKey.Favorites> {
                FavoriteEntry(favoriteFlowViewModel)
            }
            entry<ImagePickerNavKey> {
                ImagePickerEntry()
            }
            entry<AboutAppNavKey> {
                AboutAppEntry(onRefreshApp = {})
            }
            entry<AboutProductNavKey> { key ->
                AboutProductEntry(key)
            }
            entry<AddressesNavKey> { key ->
                AddressesEntry(key)
            }
            entry<AddAddressNavKey> { key ->
                AddAddressEntry(key)
            }
            entry<AllBrandsNavKey> {
                AllBrandsEntry()
            }
            entry<OrderDetailsNavKey> { key ->
                OrderDetailsEntry(onOrderIdCopied = {}, navKey = key)
            }
            entry<TraceOrderNavKey> { key ->
                TraceOrderEntry(key)
            }
            entry<OrdersHistoryNavKey> {
                OrdersHistoryEntry()
            }
            entry<AllPromotionsNavKey> { key ->
                AllPromotionsEntry(key)
            }
            entry<LoginNavKey> { key ->
                LoginEntry(key)
            }
            entry<LoginByEmailNavKey> { key ->
                LoginByEmailEntry(onRefreshAll = {}, navKey = key)
            }
            entry<LoginByPhoneCodeNavKey> { key ->
                LoginByPhoneCodeEntry(navKey = key)
            }
            entry<RecoverPasswordNavKey> {
                RecoverPasswordEntry()
            }
            entry<RegisterNavKey> {
                RegisterEntry(onRefreshAll = {}, onFetchProfile = {})
            }
            entry<AboutServicesNavKey> {
                AboutServicesEntry()
            }
            entry<ServiceDetailNavKey> { key ->
                ServiceDetailEntry(key)
            }
            entry<BuyCertificateNavKey> {
                BuyCertificateEntry()
            }
            entry<CancelOrderNavKey> { key ->
                CancelOrderEntry(key)
            }
            entry<CartNavKey> {
                CartEntry(cartFlowViewModel)
            }
            entry<AllBottlesNavKey> { key ->
                AllBottlesEntry(key)
            }
            entry<GiftsNavKey> { key ->
                GiftsEntry(key)
            }
            entry<OrderingNavKey> { key ->
                OrderingEntry(onRefreshCart = {}, navKey = key)
            }
            entry<CatalogNavKey> {
                CatalogEntry(catalogFlowViewModel)
            }
            entry<CategoriesNavKey> { key ->
                CategoriesEntry(key)
            }
            entry<CertificateActivationNavKey> {
                CertificateActivationEntry()
            }
            entry<DeliveryDateNavKey> { key ->
                DeliveryDateEntry(key)
            }
            entry<DocumentViewerNavKey> { key ->
                DocumentViewerEntry(key)
            }
            entry<FAQNavKey> { key ->
                FAQEntry(key)
            }
            entry<FavoriteNavKey> {
                FavoriteEntry(favoriteFlowViewModel)
            }
            entry<FilterValuesNavKey> { key ->
                FilterValuesEntry(key)
            }
            entry<HomeNavKey> {
                HomeEntry(homeViewModel)
            }
            entry<MapNavKey> { key ->
                MapEntry(key)
            }
            entry<OrderCallYouNavKey> { key ->
                OrderCallYouEntry(key)
            }
            entry<OrderQuestionNavKey> { key ->
                OrderQuestionEntry(key)
            }
            entry<OrderRecipientNavKey> { key ->
                OrderRecipientEntry(key)
            }
            entry<PaymentMethodNavKey> { key ->
                PaymentMethodEntry(key)
            }
            entry<PreOrderNavKey> { key ->
                PreOrderEntry(key)
            }
            entry<ProductAnalogsNavKey> { key ->
                ProductAnalogsEntry(key)
            }
            entry<ProductCatalogNavKey> { key ->
                ProductCatalogEntry(key)
            }
            entry<ProductCommentsNavKey> { key ->
                ProductCommentsEntry(key)
            }
            entry<ProductDetailsNavKey> { key ->
                ProductDetailsEntry(key)
            }
            entry<DetailMediaNavKey> { key ->
                DetailMediaEntry(key)
            }
            entry<ProductFiltersNavKey> { key ->
                ProductFiltersEntry(key)
            }
            entry<ProfileNavKey> {
                ProfileEntry(profileFlowViewModel)
            }
            entry<ChangePasswordNavKey> {
                ChangePasswordEntry()
            }
            entry<NotificationSettingsNavKey> {
                NotificationSettingsEntry()
            }
            entry<UserDataNavKey> {
                UserDataEntry(onRefreshAll = {}, onUpdateProfile = {})
            }
            entry<WaterAppNavKey> {
                WaterAppEntry()
            }
            entry<PromotionDetailsNavKey> { key ->
                PromotionDetailsEntry(key)
            }
            entry<QuestionnairesNavKey> {
                QuestionnairesEntry()
            }
            entry<SearchNavKey> { key ->
                SearchEntry(key)
            }
            entry<WebViewNavKey> { key ->
                WebViewEntry(key)
            }
            entry<QrCodeNavKey> {
                QrCodeEntry()
            }
            entry<SpeechDialogNavKey> {
                SpeechDialogEntry()
            }
            entry<ServiceOrderNavKey> { key ->
                ServiceOrderEntry(key)
            }
            entry<StoriesNavKey> { key ->
                StoriesEntry(key)
            }
            entry<SubCategoriesNavKey> { key ->
                SubCategoriesEntry(key)
            }
            entry<WaitFeedbackProductsNavKey> {
                WaitFeedbackProductsEntry()
            }
            entry<WriteCommentNavKey> { key ->
                WriteCommentEntry(key)
            }
            entry<WriteMessageNavKey> {
                WriteMessageEntry()
            }
        }
        CompositionLocalProvider(LocalNavigator provides navigator) {
            NavDisplay(
                modifier = Modifier
                    .padding(paddingValues)
                    .consumeWindowInsets(paddingValues),
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                sceneStrategies = listOf(SinglePaneSceneStrategy()),
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedKey: BottomNavKey?,
    cartState: TabManager.BottomNavCartState?,
    onItemClick: (BottomNavKey) -> Unit,
) {
    val selectedColor = colorResource(R.color.bluePrimary)
    val unselectedColor = Color(0xFFBDBDBD)
    val backgroundColor = colorResource(R.color.white)

    Surface(
        color = backgroundColor,
        contentColor = selectedColor,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavKey.values.forEach { key ->
                val isSelected = key == selectedKey
                val itemColor = if (isSelected) selectedColor else unselectedColor

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            onClick = { onItemClick(key) },
                            role = Role.Tab,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier.size(28.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(key.iconResId),
                            contentDescription = bottomNavLabel(key, cartState),
                            tint = itemColor,
                            modifier = Modifier.size(24.dp),
                        )

                        if (key is BottomNavKey.Cart && cartState?.count.orZero() > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .background(
                                        color = colorResource(R.color.promo_red),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = cartState?.count.orZero().toString(),
                                    color = backgroundColor,
                                    style = TextStyle(
                                        fontFamily = robotoFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 9.sp,
                                        lineHeight = 9.sp,
                                        letterSpacing = 0.sp,
                                    ),
                                )
                            }
                        }
                    }

                    Text(
                        text = bottomNavLabel(key, cartState),
                        color = itemColor,
                        style = TextStyle(
                            fontFamily = robotoFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 0.sp,
                        ),
                        modifier = Modifier.padding(top = 2.dp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun bottomNavLabel(
    key: BottomNavKey,
    cartState: TabManager.BottomNavCartState?,
): String {
    return if (key is BottomNavKey.Cart && cartState?.count.orZero() > 0) {
        stringResource(R.string.price_text, cartState?.total.orZero())
    } else {
        stringResource(key.labelResId)
    }
}

private val BottomNavKey.iconResId: Int
    get() = when (this) {
        BottomNavKey.Home -> R.drawable.ic_home
        BottomNavKey.Catalog -> R.drawable.ic_catalog
        BottomNavKey.Cart -> R.drawable.ic_basket
        BottomNavKey.Favorites -> R.drawable.ic_like
        BottomNavKey.Profile -> R.drawable.ic_profile
    }

private val BottomNavKey.labelResId: Int
    get() = when (this) {
        BottomNavKey.Home -> R.string.home
        BottomNavKey.Catalog -> R.string.catalog
        BottomNavKey.Cart -> R.string.cart
        BottomNavKey.Favorites -> R.string.favorite
        BottomNavKey.Profile -> R.string.account
    }

private fun Int?.orZero(): Int = this ?: 0

@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
): NavigationState {
    val topLevelStack = rememberNavBackStack(startKey)
    val subStacks = topLevelKeys.associateWith { key ->
        rememberNavBackStack(key)
    }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks,
        )
    }
}

@Stable
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    val topLevelKeys
        get() = subStacks.keys

    @get:VisibleForTesting
    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey]
            ?: error("Sub stack for $currentTopLevelKey does not exist")

    @get:VisibleForTesting
    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberSharedViewModelStoreNavEntryDecorator<NavKey>(),
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    return topLevelStack
        .flatMap { decoratedEntries[it] ?: emptyList() }
        .toMutableStateList()
}

@Immutable
class Navigator(val state: NavigationState) {


    fun popBackStack(destinationId: Int, inclusive: Boolean): Boolean {
        val stack = state.currentSubStack
        val targetIndex = stack.indexOfLast { key ->
            destinationId(key) == destinationId
        }
        if (targetIndex == -1) return false
        val removeFrom = if (inclusive) targetIndex else targetIndex + 1
        if (removeFrom >= stack.size) return false
        stack.subList(removeFrom, stack.size).clear()
        return true
    }

    fun popBackStack(destinationId: Int, inclusive: Boolean, saveState: Boolean): Boolean =
        popBackStack(destinationId, inclusive)



    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    fun goBack() {
        when (state.currentKey) {
            state.startKey -> error("You cannot go back from the start route")
            state.currentTopLevelKey -> {
                state.topLevelStack.removeLastOrNull()
            }

            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {

            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }


    private fun destinationId(key: NavKey): Int? {
        return when (key) {
            is BottomNavKey.Home, is HomeNavKey -> R.id.homeFragment
            is BottomNavKey.Catalog, is CatalogNavKey -> R.id.catalogFragment
            is BottomNavKey.Cart, is CartNavKey -> R.id.cartFragment
            is BottomNavKey.Favorites, is FavoriteNavKey -> R.id.favoriteFragment
            is BottomNavKey.Profile, is ProfileNavKey -> R.id.profileFragment
            is AboutAppNavKey -> R.id.aboutAppFragment
            is AboutProductNavKey -> R.id.aboutProductFragment
            is AddressesNavKey -> R.id.addressesFragment
            is AddAddressNavKey -> R.id.addAddressFragment
            is AllBrandsNavKey -> R.id.allBrandsFragment
            is OrderDetailsNavKey -> R.id.orderDetailsFragment
            is TraceOrderNavKey -> R.id.traceOrderFragment
            is OrdersHistoryNavKey -> R.id.allOrdersFragment
            is AllPromotionsNavKey -> R.id.allPromotionsFragment
            is LoginNavKey -> R.id.loginFragment
            is LoginByEmailNavKey -> R.id.loginByEmailFragment
            is LoginByPhoneCodeNavKey -> R.id.loginByPhoneCodeFragment
            is RecoverPasswordNavKey -> R.id.recoverPasswordFragment
            is RegisterNavKey -> R.id.registerFragment
            is AboutServicesNavKey -> R.id.aboutServicesFragment
            is ServiceDetailNavKey -> R.id.serviceDetailFragment
            is BuyCertificateNavKey -> R.id.buyCertificateFragment
            is CancelOrderNavKey -> R.id.cancelOrderFragment
            is AllBottlesNavKey -> R.id.allBottlesFragment
            is GiftsNavKey -> R.id.giftsFragment
            is OrderingNavKey -> R.id.orderingFragment
            is CategoriesNavKey -> R.id.categoriesFragment
            is CertificateActivationNavKey -> R.id.certificateActivationFragment
            is DeliveryDateNavKey -> R.id.deliveryDateFragment
            is DocumentViewerNavKey -> R.id.documentViewerFragment
            is FAQNavKey -> R.id.faqFragment
            is FilterValuesNavKey -> R.id.concreteFilterFragment
            is MapNavKey -> R.id.mapFragment
            is OrderCallYouNavKey -> R.id.orderCallYouFragment
            is OrderQuestionNavKey -> R.id.orderQuestionFragment
            is OrderRecipientNavKey -> R.id.orderRecipientFragment
            is PaymentMethodNavKey -> R.id.paymentMethodFragment
            is PreOrderNavKey -> R.id.preOrderFragment
            is ProductAnalogsNavKey -> R.id.productsCollectionFragment
            is ProductCatalogNavKey -> R.id.productCatalogFragment
            is ProductCommentsNavKey -> R.id.productCommentsFragment
            is ProductDetailsNavKey -> R.id.productDetailFragment
            is DetailMediaNavKey -> R.id.detailMedia
            is ProductFiltersNavKey -> R.id.productFiltersFragment
            is ChangePasswordNavKey -> R.id.changePasswordFragment
            is NotificationSettingsNavKey -> R.id.notificationSettingsFragment
            is UserDataNavKey -> R.id.userDataFragment
            is WaterAppNavKey -> R.id.waterAppFragment
            is PromotionDetailsNavKey -> R.id.promotionDetailFragment
            is QuestionnairesNavKey -> R.id.questionnairesFragment
            is SearchNavKey -> R.id.searchFragment
            is WebViewNavKey -> R.id.webViewFragment
            is QrCodeNavKey -> R.id.qrCodeFragment
            is SpeechDialogNavKey -> R.id.speechDialogFragment
            is ServiceOrderNavKey -> R.id.serviceOrderFragment
            is StoriesNavKey -> R.id.fullScreenHistorySliderFragment
            is SubCategoriesNavKey -> R.id.subCategoriesFragment
            is WaitFeedbackProductsNavKey -> R.id.waitFeedbackProductsFragment
            is WriteCommentNavKey -> R.id.writeCommentFragment
            is WriteMessageNavKey -> R.id.writeMessageFragment
            else -> null
        }
    }
}

@Stable
sealed interface BottomNavKey : NavKey {


    @Serializable
    data object Home : BottomNavKey {
    }

    @Serializable
    data object Catalog : BottomNavKey {

    }


    @Serializable
    data object Cart : BottomNavKey {

    }

    @Serializable
    data object Favorites : BottomNavKey {

    }

    @Serializable
    data object Profile : BottomNavKey {

    }


    companion object {
        val values
            get() = setOf(Home, Catalog, Cart, Favorites, Profile)

    }

}


fun NavKey.toContentKey(): String {
    return when (this) {
        is SharedViewModelStoreNavKey -> parentContentKey ?: toString()
        else -> toString()
    }
}
