package com.m.vodovoz.feature.main

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.SinglePaneSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.m.vodovoz.common.webview.WebViewEntry
import com.m.vodovoz.common.webview.api.WebViewNavKey
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.LegacyDestinationNavKey
import com.m.vodovoz.core.navigation.LocalNavigator
import com.m.vodovoz.design_system.VodovozTheme
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
    cartFlowViewModel: CartFlowViewModel
) {

    val navigationState = rememberNavigationState(
        startKey = BottomNavKey.Home,
        topLevelKeys = BottomNavKey.values
    )
    val navigator = remember { Navigator(navigationState) }
    SideEffect {
        AppNavigatorStore.navigator = navigator
    }


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                BottomNavKey.values.forEach { key ->
                    NavigationBarItem(
                        selected = key == navigationState.currentKey,
                        label = {
                            Text(text = key.title)
                        },
                        icon = {

                        },
                        onClick = {
                            navigator.navigate(key)
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        val navController = LocalView.current.findNavController()
        navigator.attachNavController(navController)


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
            entry<AboutAppNavKey> {
                AboutAppEntry(onRefreshApp = {})
            }
            entry<AboutProductNavKey> {
                AboutProductEntry()
            }
            entry<AddressesNavKey> {
                AddressesEntry()
            }
            entry<AddAddressNavKey> {
                AddAddressEntry()
            }
            entry<AllBrandsNavKey> {
                AllBrandsEntry()
            }
            entry<OrderDetailsNavKey> {
                OrderDetailsEntry(onOrderIdCopied = {})
            }
            entry<TraceOrderNavKey> {
                TraceOrderEntry()
            }
            entry<OrdersHistoryNavKey> {
                OrdersHistoryEntry()
            }
            entry<AllPromotionsNavKey> {
                AllPromotionsEntry()
            }
            entry<LoginNavKey> {
                LoginEntry()
            }
            entry<LoginByEmailNavKey> {
                LoginByEmailEntry(onRefreshAll = {})
            }
            entry<LoginByPhoneCodeNavKey> {
                LoginByPhoneCodeEntry()
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
            entry<ServiceDetailNavKey> {
                ServiceDetailEntry()
            }
            entry<BuyCertificateNavKey> {
                BuyCertificateEntry()
            }
            entry<CancelOrderNavKey> {
                CancelOrderEntry()
            }
            entry<CartNavKey> {
                CartEntry(cartFlowViewModel)
            }
            entry<AllBottlesNavKey> {
                AllBottlesEntry()
            }
            entry<GiftsNavKey> {
                GiftsEntry()
            }
            entry<OrderingNavKey> {
                OrderingEntry(onRefreshCart = {})
            }
            entry<CatalogNavKey> {
                CatalogEntry(catalogFlowViewModel)
            }
            entry<CategoriesNavKey> {
                CategoriesEntry()
            }
            entry<CertificateActivationNavKey> {
                CertificateActivationEntry()
            }
            entry<DeliveryDateNavKey> {
                DeliveryDateEntry()
            }
            entry<DocumentViewerNavKey> {
                DocumentViewerEntry()
            }
            entry<FAQNavKey> {
                FAQEntry()
            }
            entry<FavoriteNavKey> {
                FavoriteEntry(favoriteFlowViewModel)
            }
            entry<FilterValuesNavKey> {
                FilterValuesEntry()
            }
            entry<HomeNavKey> {
                HomeEntry(homeViewModel)
            }
            entry<MapNavKey> {
                MapEntry()
            }
            entry<OrderCallYouNavKey> {
                OrderCallYouEntry()
            }
            entry<OrderQuestionNavKey> {
                OrderQuestionEntry()
            }
            entry<OrderRecipientNavKey> {
                OrderRecipientEntry()
            }
            entry<PaymentMethodNavKey> {
                PaymentMethodEntry()
            }
            entry<PreOrderNavKey> {
                PreOrderEntry()
            }
            entry<ProductAnalogsNavKey> {
                ProductAnalogsEntry()
            }
            entry<ProductCatalogNavKey> {
                ProductCatalogEntry()
            }
            entry<ProductCommentsNavKey> {
                ProductCommentsEntry()
            }
            entry<ProductDetailsNavKey> {
                ProductDetailsEntry()
            }
            entry<DetailMediaNavKey> {
                DetailMediaEntry()
            }
            entry<ProductFiltersNavKey> {
                ProductFiltersEntry()
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
            entry<PromotionDetailsNavKey> {
                PromotionDetailsEntry()
            }
            entry<QuestionnairesNavKey> {
                QuestionnairesEntry()
            }
            entry<SearchNavKey> {
                SearchEntry()
            }
            entry<WebViewNavKey> {
                WebViewEntry()
            }
            entry<QrCodeNavKey> {
                QrCodeEntry()
            }
            entry<SpeechDialogNavKey> {
                SpeechDialogEntry()
            }
            entry<ServiceOrderNavKey> {
                ServiceOrderEntry()
            }
            entry<StoriesNavKey> {
                StoriesEntry()
            }
            entry<SubCategoriesNavKey> {
                SubCategoriesEntry()
            }
            entry<WaitFeedbackProductsNavKey> {
                WaitFeedbackProductsEntry()
            }
            entry<WriteCommentNavKey> {
                WriteCommentEntry()
            }
            entry<WriteMessageNavKey> {
                WriteMessageEntry()
            }
        }
        CompositionLocalProvider(LocalNavigator provides navigator) {
            NavDisplay(
                modifier = Modifier.padding(paddingValues),
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigator.goBack() },
                sceneStrategy = SinglePaneSceneStrategy()
            )
        }
    }
}

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
            rememberViewModelStoreNavEntryDecorator<NavKey>(),
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

    @Immutable
    data class Destination(val id: Int)

    @Immutable
    data class BackStackEntry(
        val savedStateHandle: SavedStateHandle,
        val destination: Destination?,
    )

    private var _navController: NavController? = null
    val navController: NavController
        get() = _navController ?: error("Navigator is not attached to NavController")

    private val backStackEntries = mutableMapOf<NavKey, BackStackEntry>()

    val currentBackStackEntry: BackStackEntry?
        get() = state.currentSubStack.lastOrNull()?.let(::entryOf)

    val previousBackStackEntry: BackStackEntry?
        get() = state.currentSubStack
            .getOrNull(state.currentSubStack.lastIndex - 1)
            ?.let(::entryOf)


    val graph: NavGraph
        get() = navController.graph

    fun attachNavController(navController: NavController) {
        _navController = navController
    }

    fun popBackStack(): Boolean {
        val canPop = state.currentKey != state.startKey
        if (canPop) goBack()
        return canPop
    }

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

    fun getBackStackEntry(destinationId: Int): BackStackEntry =
        state.currentSubStack
            .lastOrNull { key -> destinationId(key) == destinationId }
            ?.let(::entryOf)
            ?: error("No back stack entry for destinationId=$destinationId")

    fun navigate(resId: Int, args: android.os.Bundle? = null, navOptions: androidx.navigation.NavOptions? = null) {
        navController.navigate(resId, args, navOptions)
    }

    fun navigate(key: NavKey) {
        if (key is LegacyDestinationNavKey) {
            navController.navigate(
                key.destinationId,
                key.args,
                key.navOptions
            )
            return
        }
        updateEntryArgs(key)
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
                // We're at the base of the current sub stack, go back to the previous top level
                // stack.
                state.topLevelStack.removeLastOrNull()
            }

            else -> state.currentSubStack.removeLastOrNull()
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // Remove it if it's already in the stack so it's added at the end.
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

    private fun entryOf(key: NavKey): BackStackEntry {
        return backStackEntries.getOrPut(key) {
            BackStackEntry(
                savedStateHandle = SavedStateHandle(),
                destination = destinationId(key)?.let(::Destination)
            )
        }
    }

    private fun updateEntryArgs(key: NavKey) {
        val savedStateHandle = entryOf(key).savedStateHandle
        key.javaClass.declaredFields
            .asSequence()
            .filterNot { field ->
                field.isSynthetic || ReflectModifier.isStatic(field.modifiers)
            }
            .forEach { field ->
                runCatching {
                    field.isAccessible = true
                    savedStateHandle[field.name] = field.get(key)
                }
            }
    }

    private fun destinationId(key: NavKey): Int? {
        return when (key) {
            is LegacyDestinationNavKey -> key.destinationId
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

    val title: String

    @Serializable
    data object Home : BottomNavKey {
        override val title: String
            get() = "Дом"


    }

    @Serializable
    data object Catalog : BottomNavKey {
        override val title: String
            get() = "Каталог"
    }


    @Serializable
    data object Cart : BottomNavKey {
        override val title: String
            get() = "Корзина"
    }

    @Serializable
    data object Favorites : BottomNavKey {
        override val title: String
            get() = "Избранные"
    }

    @Serializable
    data object Profile : BottomNavKey {
        override val title: String
            get() = "Профиль"
    }


    companion object {
        val values
            get() = setOf(Home, Catalog, Cart, Favorites, Profile)

    }

}
