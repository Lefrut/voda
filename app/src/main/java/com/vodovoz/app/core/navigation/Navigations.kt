package com.vodovoz.app.core.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.vodovoz.app.R
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.design_system.model.PriceUi
import com.vodovoz.app.design_system.model.ProductMediaUi
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.filters.FilterUi
import com.vodovoz.app.design_system.model.filters.FiltersUi
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi
import com.vodovoz.app.feature.all.promotions.AllPromotionsFragment
import com.vodovoz.app.feature.buy_certificate.model.FAQUi
import com.vodovoz.app.feature.cart.model.CartPresentPopupWindowUi
import com.vodovoz.app.feature.cart.model.CartPresentUi
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.map.model.MapAddressUi
import com.vodovoz.app.feature.product_catalog.ProductCatalogFragment
import java.time.LocalDate


fun NavOptionsBuilder.slideAnim() {
    anim {
        exit = R.anim.fade_out
        enter = R.anim.slide_in_right
        popEnter = R.anim.slide_in_left
        popExit = R.anim.slide_out_right
    }
}

fun NavOptionsBuilder.expandAnim() {
    anim {
        exit = R.anim.fade_out
        enter = R.anim.slide_in_bottom
        popExit = R.anim.slide_out_bottom
    }
}

fun NavController.navigateToAddAddress(
    mapAddress: MapAddressUi? = null,
    addressId: Long? = null,
    addressName: String? = null,
    navOptions: NavOptions? = null,
) {

    navigate(
        R.id.addAddressFragment,
        bundleOf(
            "mapAddress" to mapAddress,
            "addressId" to addressId,
            "addressName" to addressName,
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

fun NavController.navigateToMap(addressName: String?) {
    navigate(
        R.id.mapFragment,
        bundleOf("addressName" to addressName),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToAboutProduct(
    productId: Long,
    prices: List<PriceUi>,
    analogButton: ColorfulButtonUi?,
    isAvailable: Boolean,
) {
    navigate(
        R.id.aboutProductFragment,
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

fun NavController.navigateToSpeechDialog() {
    navigate(R.id.speechDialogFragment)
}

fun NavController.navigateToQrCode() {
    navigate(R.id.qrCodeFragment, null)
}


fun NavController.navigateToAllBrands() {
    navigate(R.id.allBrandsFragment, null)
}

fun NavController.navigateToTraceOrder(dividerId: String, orderId: Long) {
    navigate(
        R.id.traceOrderFragment,
        bundleOf("driverId" to dividerId, "orderId" to orderId),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToWriteComment(
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
    navigate(R.id.writeCommentFragment, args)
}

fun NavController.navigateToWaitFeedbackProducts() {
    navigate(
        R.id.waitFeedbackProductsFragment,
        null,
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToServiceOrder(serviceType: String) {
    navigate(
        R.id.serviceOrderFragment, bundleOf("serviceType" to serviceType),
    )
}


fun NavController.navigateToServiceDetails(serviceId: Int) {
    navigate(
        R.id.serviceDetailFragment,
        bundleOf("serviceId" to serviceId),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToFAQ(faq: FAQUi) {
    navigate(
        R.id.faqFragment,
        bundleOf("faq" to faq),
        navOptions {
            slideAnim()
        }
    )
}


fun NavController.navigateToCancelOrder(orderId: Long) {
    navigate(
        R.id.cancelOrderFragment,
        bundleOf("orderId" to orderId),
        navOptions {
            expandAnim()
        }
    )
}


fun NavController.navigateToOrderQuestion(orderId: Long) {
    navigate(
        R.id.orderQuestionFragment,
        bundleOf("orderId" to orderId),
        navOptions {
            expandAnim()
        }
    )
}

fun NavController.navigateToAllBottles() {
    navigate(
        R.id.allBottlesFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToGifts(
    present: CartPresentUi? = null,
    popupWindow: CartPresentPopupWindowUi,
) {
    navigate(
        R.id.giftsFragment,
        bundleOf("present" to present, "popupWindow" to popupWindow),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToAddresses(addressScreenType: AddressScreenTypeUi) {
    navigate(
        R.id.addressesFragment,
        bundleOf("screenType" to addressScreenType),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToDeliveryDate(addressId: Long, navOptions: NavOptions? = null) {
    navigate(
        R.id.deliveryDateFragment,
        bundleOf("addressId" to addressId),
        navOptions ?: navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToPaymentMethod(addressId: Long, date: LocalDate) {
    navigate(
        R.id.paymentMethodFragment,
        bundleOf(
            "addressId" to addressId,
            "date" to date.toEpochDay()
        ),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToOrderCallYou(addressId: Long) {
    navigate(
        R.id.orderCallYouFragment,
        bundleOf("addressId" to addressId),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToOrderRecipient(addressId: Long) {
    navigate(
        R.id.orderRecipientFragment,
        bundleOf("addressId" to addressId),
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToRecoverPassword() {
    navigate(
        R.id.recoverPasswordFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToButtonProductList(buttonId: Int) {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.ButtonProducts(
                buttonId
            )
        )
    )
}

fun NavController.navigateToAboutApp() {
    navigate(
        R.id.aboutAppFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}


fun NavController.navigateToNotificationSettings() {
    navigate(
        R.id.notificationSettingsFragment, Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}


fun NavController.navigateToQuestionnaires() {
    navigate(
        R.id.questionnairesFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToPastPurchases() {
    navigate(
        R.id.pastPurchasesFragment, Bundle.EMPTY, navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToOrdersHistory() {
    navigate(R.id.allOrdersFragment, Bundle.EMPTY, navOptions {
        slideAnim()
    })
}

fun NavController.navigateToOrdering(coupon: String) {
    navigate(R.id.orderingFragment, bundleOf("coupon" to coupon), navOptions { expandAnim() })
}

fun NavController.navigateToOrderDetails(orderId: Long) {
    navigate(R.id.orderDetailsFragment, bundleOf("orderId" to orderId))
}

fun NavController.navigateToDetailMedia(media: ProductMediaUi, mediaList: List<ProductMediaUi>) {
    navigate(
        R.id.detailMedia,
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

fun NavController.navigateToStories(storyId: Long, stories: List<StoryUi>) {
    val bundle = bundleOf(
        "storyId" to storyId,
        "stories" to stories
    )
    navigate(
        R.id.fullScreenHistorySliderFragment,
        bundle,
        navOptions {
            anim {
                popExit = R.anim.slide_out_bottom
                exit = R.anim.fade_out
                enter = R.anim.scale_in
            }
        }
    )
}

fun NavController.navigateToRegister() {
    navigate(R.id.registerFragment)
}

fun NavController.navigateToLoginByPhone(phone: String, waitSeconds: Int) {
    navigate(
        R.id.loginByPhoneCodeFragment,
        bundleOf(
            "phoneNumber" to phone,
            "waitRequestCodeSeconds" to waitSeconds
        )
    )
}

fun NavController.navigateToChangePassword() {
    navigate(
        R.id.changePasswordFragment, bundleOf(), navOptions {
            slideAnim()
        })
}


fun NavController.navigateToUserData() {
    navigate(R.id.userDataFragment)
}

fun NavController.navigateToLoginByEmail() {
    navigate(R.id.loginByEmailFragment)
}


fun NavController.navigateToLogin() {
    navigate(R.id.loginFragment)
}

fun NavController.navigateToProductFilterValues(categoryId: Long, filter: FilterUi) {
    navigate(
        R.id.concreteFilterFragment,
        bundleOf("categoryId" to categoryId, "filter" to filter),
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()
    )
}

fun NavController.navigateToProductFilters(categoryId: Long, filters: FiltersUi) {
    navigate(
        R.id.productFiltersFragment,
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

fun NavController.navigateToProductComments(
    productId: Long,
    productName: String,
    productImage: String,
) {
    navigate(
        R.id.productCommentsFragment,
        bundleOf(
            "productId" to productId,
            "productName" to productName,
            "productImage" to productImage,
        )
    )

}

fun NavController.navigateToPreOrder(productId: Long) {
    navigate(
        R.id.preOrderFragment,
        bundleOf("productId" to productId),
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_bottom)
            .setExitAnim(R.anim.slide_out_bottom)
            .setPopEnterAnim(R.anim.slide_in_bottom)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()
    )

}

fun NavController.navigateToProductAnalogs(productId: Long) {
    navigate(
        R.id.productsCollectionFragment,
        bundleOf("productId" to productId)
    )
}

fun NavController.navigateToCertificateActivation() {
    navigate(
        R.id.certificateActivationFragment,
        Bundle.EMPTY,
        navOptions {
            slideAnim()
        }
    )
}

fun NavController.navigateToCategories(category: CategoryUi, categories: List<CategoryUi>) {
    navigate(
        R.id.categoriesFragment,
        bundleOf(
            "categoryList" to categories.toTypedArray(),
            "category" to category,
        )
    )
}


fun NavController.navigateToSubCategories(category: ParentCategoryUi) {
    navigate(
        R.id.subCategoriesFragment,
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

fun NavController.navigateToBrandProductList(brandId: Long) {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.Brand(
                brandId
            )
        )
    )
}

fun NavController.navigateToSearchProductList(query: String) {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.Search(
                query
            )
        )
    )
}

fun NavController.navigateToViewedProductList() {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.ViewedProducts
        )
    )
}


fun NavController.navigateToCategoryProductList(categoryId: Long) {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.Category(
                categoryId
            )
        )
    )
}

fun NavController.navigateToBannerProductList(bannerId: Long, blockId: Long) {
    navigate(
        R.id.productCatalogFragment,
        bundleOf(
            "dataSource" to ProductCatalogFragment.DataSource.Products(
                bannerId, blockId
            )
        )
    )

}

fun NavController.navigateToProductDetails(productId: Long) {
    navigate(
        R.id.productDetailFragment,
        bundleOf("productId" to productId)
    )
}

fun NavController.navigateToPromotionDetails(promotionId: Long) {
    navigate(
        R.id.promotionDetailFragment,
        bundleOf("promotionId" to promotionId)
    )
}

fun NavController.navigateToPromotions(blockId: Long, bannerId: Long) {
    navigate(
        R.id.allPromotionsFragment,
        bundleOf(
            "dataSource" to AllPromotionsFragment.DataSource.ByBanner(bannerId, blockId)
        )
    )
}

fun NavController.navigateToSearch(query: String = "") {
    navigate(R.id.searchFragment, bundleOf("query" to query))
}

fun NavController.navigateToHurryBuyUpProducts() {
    navigate(
        R.id.productCatalogFragment,
        bundleOf("dataSource" to ProductCatalogFragment.DataSource.HurryBuyUpProducts)
    )
}

fun NavController.navigateToNewProducts() {
    navigate(
        R.id.productCatalogFragment,
        bundleOf("dataSource" to ProductCatalogFragment.DataSource.NewProducts)
    )
}

fun NavController.navigateToPromotions() {
    navigate(R.id.allPromotionsFragment)
}

fun NavController.navigateToWaterApp() {
    navigate(R.id.waterAppFragment)
}

fun NavController.navigateToBuyCertificate() {
    navigate(R.id.buyCertificateFragment)
}

fun NavController.navigateToWebView(url: String, title: String = "") {
    navigate(R.id.webViewFragment, bundleOf("url" to url, "title" to title))

}

fun NavController.navigateToAllServices() {
    navigate(
        R.id.aboutServicesFragment,
        null,
        navOptions { slideAnim() }
    )
}