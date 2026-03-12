package com.m.vodovoz.core.navigation



import android.os.Bundle

import android.view.View

import androidx.core.os.bundleOf

import androidx.lifecycle.SavedStateHandle

import androidx.navigation.NavOptions

import androidx.navigation.NavOptionsBuilder

import androidx.navigation.findNavController

import androidx.navigation.navOptions

import com.m.vodovoz.R

import com.m.vodovoz.design_system.model.ColorfulButtonUi

import com.m.vodovoz.design_system.model.ParentCategoryUi

import com.m.vodovoz.design_system.model.PriceUi

import com.m.vodovoz.design_system.model.ProductMediaUi

import com.m.vodovoz.design_system.model.StoryUi

import com.m.vodovoz.design_system.model.filters.FilterUi

import com.m.vodovoz.design_system.model.filters.FiltersUi

import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi

import com.m.vodovoz.feature.all.promotions.AllPromotionsFragment

import com.m.vodovoz.feature.buy_certificate.model.FAQUi

import com.m.vodovoz.feature.cart.bottles.model.BottleUi

import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi

import com.m.vodovoz.feature.cart.model.CartPresentUi

import com.m.vodovoz.feature.cart.ordering.OrderingFlowViewModel

import com.m.vodovoz.feature.home.model.CategoryUi

import com.m.vodovoz.feature.map.model.MapAddressUi

import com.m.vodovoz.feature.main.Navigator

import com.m.vodovoz.feature.product_catalog.api.ProductCatalogNavKey

import java.time.LocalDate

private fun Navigator.navigateLegacy(
    destinationId: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
) {
    navigate(
        LegacyDestinationNavKey(
            destinationId = destinationId,
            args = args,
            navOptions = navOptions
        )
    )
}



fun Navigator.navigateToWriteMessage() {
    navigateLegacy(R.id.writeMessageFragment,
        Bundle.EMPTY,
        navOptions {
            launchSingleTop = true
            slideAnim()
        }
    )
}

fun Navigator.navigateToAddAddress(
    mapAddress: MapAddressUi? = null,
    addressId: Long? = null,
    addressName: String? = null,
    addressType: Int? = null,
    navOptions: NavOptions? = null,
) {

    navigateLegacy(R.id.addAddressFragment,
        bundleOf(
            "mapAddress" to mapAddress,
            "addressId" to addressId,
            "addressName" to addressName,
            "addressType" to addressType
        ),
        navOptions ?: navOptions {
            slideAnim()
        }
    )

    val backStack = try {
        getBackStackEntry(R.id.addAddressFragment)
    } catch (_: Throwable) {
        null
    }

    if (backStack != null) {
        backStack.savedStateHandle["mapAddress"] = mapAddress
    }

}

fun Navigator.navigateToMap(addressName: String?, navOptions: NavOptions? = null) {
    navigateLegacy(R.id.mapFragment,
        bundleOf("addressName" to addressName),
        navOptions ?: navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToAboutProduct(
    productId: Long,
    prices: List<PriceUi>,
    analogButton: ColorfulButtonUi?,
    isAvailable: Boolean,
) {
    navigateLegacy(R.id.aboutProductFragment,
        bundleOf(
            "productId" to productId,
            "prices" to prices,
            "analogButton" to analogButton,
            "isAvailable" to isAvailable
        ),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToSpeechDialog() {
    navigateLegacy(R.id.speechDialogFragment)
}

fun Navigator.navigateToQrCode() {
    navigateLegacy(R.id.qrCodeFragment, null)
}

fun Navigator.navigateToAllBrands() {
    navigateLegacy(R.id.allBrandsFragment, null)
}

fun Navigator.navigateToTraceOrder(dividerId: String, orderId: Long) {
    navigateLegacy(R.id.traceOrderFragment,
        bundleOf("driverId" to dividerId, "orderId" to orderId),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToWriteComment(
    productId: Long,
    productName: String,
    productImage: String,
    rating: Int,
) {
    val args = bundleOf(
        "product_id" to productId,
        "product_name" to productName,
        "product_image" to productImage,
        "rating" to rating
    )
    navigateLegacy(R.id.writeCommentFragment, args)
}

fun Navigator.navigateToWaitFeedbackProducts() {
    navigateLegacy(R.id.waitFeedbackProductsFragment,
        null,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToServiceOrder(serviceType: String) {
    navigateLegacy(R.id.serviceOrderFragment, bundleOf("serviceType" to serviceType),
    )
}

fun Navigator.navigateToServiceDetails(serviceId: Int) {
    navigateLegacy(R.id.serviceDetailFragment,
        bundleOf("serviceId" to serviceId),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToFAQ(faq: FAQUi) {
    navigateLegacy(R.id.faqFragment,
        bundleOf("faq" to faq),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToCancelOrder(orderId: Long) {
    navigateLegacy(R.id.cancelOrderFragment,
        bundleOf("orderId" to orderId),
        navOptions {
            expandAnim()
        }
    )
}

fun Navigator.navigateToOrderQuestion(orderId: Long) {
    navigateLegacy(R.id.orderQuestionFragment,
        bundleOf("orderId" to orderId),
        navOptions {
            expandAnim()
        }
    )
}

fun Navigator.navigateToAllBottles(bottles: List<BottleUi>) {
    navigateLegacy(R.id.allBottlesFragment,
        bundleOf("bottles" to bottles),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToGifts(
    present: CartPresentUi? = null,
    popupWindow: CartPresentPopupWindowUi,
) {
    navigateLegacy(R.id.giftsFragment,
        bundleOf("present" to present, "popupWindow" to popupWindow),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToAddresses(
    addressScreenType: AddressScreenTypeUi,
    addressId: Long? = null,
) {
    navigateLegacy(R.id.addressesFragment,
        bundleOf(
            "screenType" to addressScreenType,
            "addressId" to addressId
        ),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToDeliveryDate(
    addressId: Long,
    earlierDelivery: Boolean = false,
    date: String? = null,
    timeInterval: String? = null,
    navOptions: NavOptions? = null,
    queryParams: Map<String, String>
) {
    navigateLegacy(R.id.deliveryDateFragment,
        bundleOf(
            "addressId" to addressId,
            "date" to date,
            "timeInterval" to timeInterval,
            "earlierDelivery" to earlierDelivery,
            CommonArgs.queryParamsTo(queryParams)
        ),
        navOptions ?: navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToPaymentMethod(
    addressId: Long,
    date: LocalDate,
    paymentMethodId: String? = null,
    paymentChange: String?,
    balance: Boolean? = null,
    bonuses: Boolean?,
    bonusesValue: Int?,
    queryParams: Map<String, String>,
) {
    navigateLegacy(R.id.paymentMethodFragment,
        bundleOf(
            "addressId" to addressId,
            "date" to date.toEpochDay(),
            "paymentMethodId" to paymentMethodId,
            "paymentChange" to paymentChange,
            "balance" to balance,
            "bonuses" to bonuses,
            "bonusesValue" to bonusesValue,
            CommonArgs.queryParamsTo(queryParams)
        ),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToOrderCallYou(
    addressId: Long,
    callYouId: String? = null,
    queryParams: Map<String, String>
) {
    navigateLegacy(R.id.orderCallYouFragment,
        bundleOf(
            "addressId" to addressId,
            "callYouId" to callYouId,
            CommonArgs.queryParamsTo(queryParams)
        ),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToOrderRecipient(addressId: Long) {
    navigateLegacy(R.id.orderRecipientFragment,
        bundleOf("addressId" to addressId),
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToRecoverPassword() {
    navigateLegacy(R.id.recoverPasswordFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToButtonProductList(buttonId: Int) {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.ButtonId(buttonId)))
}

fun Navigator.navigateToAboutApp() {
    navigateLegacy(R.id.aboutAppFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToNotificationSettings() {
    navigateLegacy(R.id.notificationSettingsFragment, Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToQuestionnaires() {
    navigateLegacy(R.id.questionnairesFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToOrdersHistory() {
    navigateLegacy(R.id.allOrdersFragment, Bundle.EMPTY, navOptions {
        slideAnim()
    })
}

fun Navigator.navigateToOrdering(coupon: String) {
    navigateLegacy(R.id.orderingFragment, bundleOf("coupon" to coupon), navOptions { expandAnim() })
}

fun Navigator.navigateToOrderDetails(orderId: Long) {
    navigateLegacy(R.id.orderDetailsFragment, bundleOf("orderId" to orderId))
}

fun Navigator.navigateToDetailMedia(media: ProductMediaUi, mediaList: List<ProductMediaUi>) {
    navigateLegacy(R.id.detailMedia,
        bundleOf(
            "media" to media,
            "mediaList" to mediaList
        ),
        navOptions {
            anim {
                enter = R.anim.fade_in
                popExit = R.anim.fade_out
            }
        }
    )

}

fun Navigator.navigateToStories(storyId: Long, stories: List<StoryUi>) {
    val bundle = bundleOf(
        "storyId" to storyId,
        "stories" to stories
    )
    navigateLegacy(R.id.fullScreenHistorySliderFragment,
        bundle,
        navOptions {
            anim {
                exit = R.anim.fade_out
                enter = R.anim.scale_in
            }
        }
    )
}

fun Navigator.navigateToRegister() {
    navigateLegacy(R.id.registerFragment)
}

fun Navigator.navigateToLoginByPhone(phone: String, waitSeconds: Int, userUrl: String) {
    navigateLegacy(R.id.loginByPhoneCodeFragment,
        bundleOf(
            LoginByPhoneCodeArgs.PHONE to phone,
            LoginByPhoneCodeArgs.WAIT_SECONDS to waitSeconds,
            LoginByPhoneCodeArgs.USER_URL to userUrl
        )
    )
}

fun Navigator.navigateToChangePassword() {
    navigateLegacy(R.id.changePasswordFragment, bundleOf(), navOptions {
            slideAnim()
        })
}

fun Navigator.navigateToUserData() {
    navigateLegacy(R.id.userDataFragment)
}

fun Navigator.navigateToLoginByEmail(selectedAccountTypeId: String? = null) {
    navigateLegacy(R.id.loginByEmailFragment,
        selectedAccountTypeId?.let {
            bundleOf(AuthArgs.ACCOUNT_TYPE_ID to it)
        }
    )
}

fun Navigator.navigateToLogin(selectedAccountTypeId: String? = null) {
    navigateLegacy(R.id.loginFragment,
        selectedAccountTypeId?.let {
            bundleOf(AuthArgs.ACCOUNT_TYPE_ID to it)
        }
    )
}

fun Navigator.navigateToProductFilterValues(categoryId: Long, filter: FilterUi) {
    navigateLegacy(R.id.concreteFilterFragment,
        bundleOf("categoryId" to categoryId, "filter" to filter),
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    )
}

fun Navigator.navigateToProductFilters(categoryId: Long, filters: FiltersUi) {
    navigateLegacy(R.id.productFiltersFragment,
        bundleOf(
            "categoryId" to categoryId,
            "filters" to filters
        ),
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.fade_out)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()
    )
}

fun Navigator.navigateToProductComments(
    productId: Long,
    productName: String,
    productImage: String,
) {
    navigateLegacy(R.id.productCommentsFragment,
        bundleOf(
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
        )
    )

}

fun Navigator.navigateToPreOrder(productId: Long) {
    navigateLegacy(R.id.preOrderFragment,
        bundleOf("productId" to productId),
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.slide_out_bottom)
            .setPopEnterAnim(R.anim.slide_in_bottom)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()
    )

}

fun Navigator.navigateToProductAnalogs(productId: Long) {
    navigateLegacy(R.id.productsCollectionFragment,
        bundleOf("productId" to productId)
    )
}

fun Navigator.navigateToCertificateActivation() {
    navigateLegacy(R.id.certificateActivationFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun Navigator.navigateToCategories(category: CategoryUi, categories: List<CategoryUi>) {
    navigateLegacy(R.id.categoriesFragment,
        bundleOf(
            "categoryList" to categories.toTypedArray(),
            "category" to category,
        )
    )
}

fun Navigator.navigateToSubCategories(category: ParentCategoryUi) {
    navigateLegacy(R.id.subCategoriesFragment,
        bundleOf("category" to category),
        navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    )
}

fun Navigator.navigateToBrandProductList(brandId: Long) {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.BrandId(brandId)))
}

fun Navigator.navigateToPastPurchases() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.PastPurchases))
}

fun Navigator.navigateToSearchProductList(query: String) {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.SearchQuery(query)))
}

fun Navigator.navigateToViewedProductList() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.ViewedProducts))
}

fun Navigator.navigateToCategoryProductList(categoryId: Long) {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.CategoryId(categoryId)))
}

fun Navigator.navigateToBannerProductList(bannerId: Long, blockId: Long) {
    navigate(
        ProductCatalogNavKey(
            ProductCatalogNavKey.DataSource.BannerProducts(
                bannerId = bannerId,
                blockId = blockId
            )
        )
    )

}

fun Navigator.navigateToProductDetails(productId: Long) {
    navigateLegacy(R.id.productDetailFragment,
        bundleOf("productId" to productId)
    )
}

fun Navigator.navigateToPromotionDetails(promotionId: Long) {
    navigateLegacy(R.id.promotionDetailFragment,
        bundleOf("promotionId" to promotionId)
    )
}

fun Navigator.navigateToPromotions(blockId: Long, bannerId: Long) {
    navigateLegacy(R.id.allPromotionsFragment,
        bundleOf(
            "dataSource" to AllPromotionsFragment.DataSource.ByBanner(bannerId, blockId)
        )
    )
}

fun Navigator.navigateToSearch(query: String = "") {
    navigateLegacy(R.id.searchFragment, bundleOf("query" to query))
}

fun Navigator.navigateToHurryBuyUpProducts() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.HurryBuyUpProducts))
}

fun Navigator.navigateToNewProducts() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.NewProducts))
}

fun Navigator.navigateToPromotions() {
    navigateLegacy(R.id.allPromotionsFragment)
}

fun Navigator.navigateToWaterApp() {
    navigateLegacy(R.id.waterAppFragment,
        null,
        navOptions {
            anim {
                enter = R.anim.water_slide_in
            }
        }
    )
}

fun Navigator.navigateToBuyCertificate() {
    navigateLegacy(R.id.buyCertificateFragment)
}

fun Navigator.navigateToWebView(
    url: String,
    title: String = "",
    navOptions: NavOptions? = null,
) {
    navigateLegacy(R.id.webViewFragment, bundleOf("url" to url, "title" to title), navOptions)

}

fun Navigator.navigateToAllServices() {
    navigateLegacy(R.id.aboutServicesFragment,
        null,
        navOptions { slideAnim() }
    )
}
