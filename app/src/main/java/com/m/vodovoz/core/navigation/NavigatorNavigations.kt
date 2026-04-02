package com.m.vodovoz.core.navigation


import android.os.Bundle

import androidx.navigation.NavOptions

import androidx.navigation.navOptions

import com.m.vodovoz.R
import com.m.vodovoz.common.media.api.ImagePickerNavKey

import com.m.vodovoz.design_system.model.ColorfulButtonUi

import com.m.vodovoz.design_system.model.ParentCategoryUi

import com.m.vodovoz.design_system.model.PriceUi

import com.m.vodovoz.design_system.model.ProductMediaUi

import com.m.vodovoz.design_system.model.StoryUi

import com.m.vodovoz.design_system.model.filters.FilterUi

import com.m.vodovoz.design_system.model.filters.FiltersUi

import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.about_app.api.AboutAppNavKey
import com.m.vodovoz.feature.about_product.api.AboutProductNavKey
import com.m.vodovoz.feature.all.brands.api.AllBrandsNavKey
import com.m.vodovoz.feature.all.orders.detail.api.OrderDetailsNavKey
import com.m.vodovoz.feature.all.orders.detail.traceorder.api.TraceOrderNavKey
import com.m.vodovoz.feature.all.orders.history.api.OrdersHistoryNavKey

import com.m.vodovoz.feature.all.promotions.AllPromotionsFragment
import com.m.vodovoz.feature.all.promotions.api.AllPromotionsNavKey
import com.m.vodovoz.feature.auth.login.api.LoginNavKey
import com.m.vodovoz.feature.auth.login_by_email.api.LoginByEmailNavKey
import com.m.vodovoz.feature.auth.login_by_phone_code.api.LoginByPhoneCodeNavKey
import com.m.vodovoz.feature.auth.recover_password.api.RecoverPasswordNavKey
import com.m.vodovoz.feature.auth.reg.api.RegisterNavKey
import com.m.vodovoz.feature.addresses.api.AddressesNavKey
import com.m.vodovoz.feature.addresses.add.api.AddAddressNavKey
import com.m.vodovoz.feature.bottom.services.api.AboutServicesNavKey
import com.m.vodovoz.feature.bottom.services.detail.api.ServiceDetailNavKey
import com.m.vodovoz.feature.buy_certificate.api.BuyCertificateNavKey

import com.m.vodovoz.feature.buy_certificate.model.FAQUi

import com.m.vodovoz.feature.cancel_order.api.CancelOrderNavKey
import com.m.vodovoz.feature.cart.bottles.model.BottleUi
import com.m.vodovoz.feature.cart.bottles.api.AllBottlesNavKey

import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi

import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.feature.cart.gifts.api.GiftsNavKey
import com.m.vodovoz.feature.cart.ordering.api.OrderingNavKey

import com.m.vodovoz.feature.certificate_activation.api.CertificateActivationNavKey

import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.feature.categories.api.CategoriesNavKey

import com.m.vodovoz.feature.faq.api.FAQNavKey
import com.m.vodovoz.feature.filter_values.api.FilterValuesNavKey
import com.m.vodovoz.feature.map.model.MapAddressUi
import com.m.vodovoz.feature.map.api.MapNavKey

import com.m.vodovoz.feature.main.Navigator
import com.m.vodovoz.feature.main.toContentKey
import com.m.vodovoz.feature.order_call_you.api.OrderCallYouNavKey
import com.m.vodovoz.feature.order_question.api.OrderQuestionNavKey
import com.m.vodovoz.feature.order_recipient.api.OrderRecipientNavKey
import com.m.vodovoz.feature.payment_method.api.PaymentMethodNavKey
import com.m.vodovoz.feature.preorder.api.PreOrderNavKey
import com.m.vodovoz.feature.product_analogs.api.ProductAnalogsNavKey
import com.m.vodovoz.feature.product_comments.api.ProductCommentsNavKey
import com.m.vodovoz.feature.product_details.api.ProductDetailsNavKey
import com.m.vodovoz.feature.product_details.detail_media.api.DetailMediaNavKey
import com.m.vodovoz.feature.product_filters.api.ProductFiltersNavKey
import com.m.vodovoz.feature.profile.change_password.api.ChangePasswordNavKey
import com.m.vodovoz.feature.profile.notification_settings.api.NotificationSettingsNavKey
import com.m.vodovoz.feature.profile.user_data.api.UserDataNavKey
import com.m.vodovoz.feature.profile.waterapp.api.WaterAppNavKey

import com.m.vodovoz.feature.product_catalog.api.ProductCatalogNavKey
import com.m.vodovoz.feature.promotion_details.api.PromotionDetailsNavKey
import com.m.vodovoz.feature.questionnaires.api.QuestionnairesNavKey
import com.m.vodovoz.feature.search.api.SearchNavKey
import com.m.vodovoz.feature.search.qrcode.api.QrCodeNavKey
import com.m.vodovoz.feature.service_order.api.ServiceOrderNavKey
import com.m.vodovoz.feature.stories_fragment.api.StoriesNavKey
import com.m.vodovoz.feature.sub_categories.api.SubCategoriesNavKey
import com.m.vodovoz.feature.delivery_date.api.DeliveryDateNavKey
import com.m.vodovoz.feature.wait_feedback_products.api.WaitFeedbackProductsNavKey
import com.m.vodovoz.feature.write_comment.api.WriteCommentNavKey
import com.m.vodovoz.feature.write_message.api.WriteMessageNavKey
import com.m.vodovoz.common.webview.api.WebViewNavKey
import com.m.vodovoz.ui.dialog.api.SpeechDialogNavKey

import java.time.LocalDate


fun Navigator.navigateToWriteMessage() {
    navigate(WriteMessageNavKey)
}

fun Navigator.navigateToAddAddress(
    mapAddress: MapAddressUi? = null,
    addressId: Long? = null,
    addressName: String? = null,
    addressType: Int? = null,
    navOptions: NavOptions? = null,
) {
    navigate(
        AddAddressNavKey(
            mapAddress = mapAddress,
            addressId = addressId,
            addressName = addressName,
            addressType = addressType
        )
    )

    currentBackStackEntry?.savedStateHandle?.set("mapAddress", mapAddress)
}

fun Navigator.navigateToMap(addressName: String?, navOptions: NavOptions? = null) {
    navigate(MapNavKey(addressName = addressName))
}

fun Navigator.navigateToAboutProduct(
    productId: Long,
    prices: List<PriceUi>,
    analogButton: ColorfulButtonUi?,
    isAvailable: Boolean,
) {
    navigate(
        AboutProductNavKey(
            productId = productId,
            prices = prices,
            analogButton = analogButton,
            isAvailable = isAvailable
        )
    )
}

fun Navigator.navigateToSpeechDialog() {
    navigate(SpeechDialogNavKey)
}

fun Navigator.navigateToQrCode() {
    navigate(QrCodeNavKey)
}

fun Navigator.navigateToAllBrands() {
    navigate(AllBrandsNavKey)
}

fun Navigator.navigateToTraceOrder(dividerId: String, orderId: Long) {
    navigate(TraceOrderNavKey(driverId = dividerId, orderId = orderId))
}

fun Navigator.navigateToWriteComment(
    productId: Long,
    productName: String,
    productImage: String,
    rating: Int,
) {
    navigate(
        WriteCommentNavKey(
            product_id = productId,
            product_name = productName,
            product_image = productImage,
            rating = rating
        )
    )
}

fun Navigator.navigateToWaitFeedbackProducts() {
    navigate(WaitFeedbackProductsNavKey)
}

fun Navigator.navigateToServiceOrder(serviceType: String) {
    navigate(ServiceOrderNavKey(serviceType = serviceType))
}

fun Navigator.navigateToServiceDetails(serviceId: Int) {
    navigate(ServiceDetailNavKey(serviceId = serviceId))
}

fun Navigator.navigateToFAQ(faq: FAQUi) {
    navigate(FAQNavKey(faq = faq))
}

fun Navigator.navigateToCancelOrder(orderId: Long) {
    navigate(CancelOrderNavKey(orderId = orderId))
}

fun Navigator.navigateToOrderQuestion(orderId: Long) {
    navigate(OrderQuestionNavKey(orderId = orderId))
}

fun Navigator.navigateToAllBottles(bottles: List<BottleUi>) {
    navigate(AllBottlesNavKey(bottles = bottles))
}

fun Navigator.navigateToGifts(
    present: CartPresentUi? = null,
    popupWindow: CartPresentPopupWindowUi,
) {
    navigate(
        GiftsNavKey(
            present = present,
            popupWindow = popupWindow
        )
    )
}

fun Navigator.navigateToAddresses(
    addressScreenType: AddressScreenTypeUi,
    addressId: Long? = null,
) {
    navigate(
        AddressesNavKey(
            screenType = addressScreenType,
            addressId = addressId
        )
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
    navigate(
        DeliveryDateNavKey(
            addressId = addressId,
            earlierDelivery = earlierDelivery,
            date = date,
            timeInterval = timeInterval,
            queryParams = queryParams,
            parentContentKey = state.currentKey.toContentKey()
        )
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
    navigate(
        PaymentMethodNavKey(
            addressId = addressId,
            date = date.toEpochDay(),
            paymentMethodId = paymentMethodId,
            paymentChange = paymentChange,
            balance = balance,
            bonuses = bonuses,
            bonusesValue = bonusesValue,
            queryParams = queryParams,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToOrderCallYou(
    addressId: Long,
    callYouId: String? = null,
    queryParams: Map<String, String>
) {
    navigate(
        OrderCallYouNavKey(
            addressId = addressId,
            callYouId = callYouId,
            queryParams = queryParams,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToOrderRecipient(addressId: Long) {
    navigate(
        OrderRecipientNavKey(
            addressId = addressId,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToRecoverPassword() {
    navigate(RecoverPasswordNavKey)
}

fun Navigator.navigateToButtonProductList(buttonId: Int) {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.ButtonId(buttonId)))
}

fun Navigator.navigateToAboutApp() {
    navigate(AboutAppNavKey)
}

fun Navigator.navigateToNotificationSettings() {
    navigate(NotificationSettingsNavKey)
}

fun Navigator.navigateToQuestionnaires() {
    navigate(QuestionnairesNavKey)
}

fun Navigator.navigateToOrdersHistory() {
    navigate(OrdersHistoryNavKey)
}

fun Navigator.navigateToOrdering(coupon: String) {
    navigate(OrderingNavKey(coupon = coupon))
}

fun Navigator.navigateToOrderDetails(orderId: Long) {
    navigate(OrderDetailsNavKey(orderId = orderId))
}

fun Navigator.navigateToDetailMedia(media: ProductMediaUi, mediaList: List<ProductMediaUi>) {
    navigate(
        DetailMediaNavKey(
            media = media,
            mediaList = mediaList,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToStories(storyId: Long, stories: List<StoryUi>) {
    navigate(StoriesNavKey(storyId = storyId, stories = stories))
}

fun Navigator.navigateToRegister() {
    navigate(RegisterNavKey)
}

fun Navigator.navigateToLoginByPhone(phone: String, waitSeconds: Int, userUrl: String) {
    navigate(
        LoginByPhoneCodeNavKey(
            phoneNumber = phone,
            waitRequestCodeSeconds = waitSeconds,
            userUrl = userUrl
        )
    )
}

fun Navigator.navigateToChangePassword() {
    navigate(ChangePasswordNavKey)
}

fun Navigator.navigateToUserData() {
    navigate(UserDataNavKey)
}

fun Navigator.navigateToLoginByEmail(selectedAccountTypeId: String? = null) {
    navigate(LoginByEmailNavKey(accountTypeId = selectedAccountTypeId))
}

fun Navigator.navigateToLogin(selectedAccountTypeId: String? = null) {
    navigate(LoginNavKey(accountTypeId = selectedAccountTypeId))
}

fun Navigator.navigateToProductFilterValues(categoryId: Long, filter: FilterUi) {
    navigate(
        FilterValuesNavKey(
            categoryId = categoryId,
            filter = filter,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToProductFilters(categoryId: Long, filters: FiltersUi) {
    navigate(
        ProductFiltersNavKey(
            categoryId = categoryId,
            filters = filters,
            parentContentKey = state.currentKey.toContentKey()
        )
    )
}

fun Navigator.navigateToProductComments(
    productId: Long,
    productName: String,
    productImage: String,
) {
    navigate(
        ProductCommentsNavKey(
            productId = productId,
            productName = productName,
            productImage = productImage
        )
    )
}

fun Navigator.navigateToPreOrder(productId: Long) {
    navigate(PreOrderNavKey(productId = productId))
}

fun Navigator.navigateToProductAnalogs(productId: Long) {
    navigate(ProductAnalogsNavKey(productId = productId))
}

fun Navigator.navigateToCertificateActivation() {
    navigate(CertificateActivationNavKey)
}

fun Navigator.navigateToCategories(category: CategoryUi, categories: List<CategoryUi>) {
    navigate(
        CategoriesNavKey(
            categoryList = categories.toTypedArray(),
            category = category
        )
    )
}

fun Navigator.navigateToSubCategories(category: ParentCategoryUi) {
    navigate(SubCategoriesNavKey(category = category))
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
    navigate(ProductDetailsNavKey(productId = productId))
}

fun Navigator.navigateToPromotionDetails(promotionId: Long) {
    navigate(PromotionDetailsNavKey(promotionId = promotionId))
}

fun Navigator.navigateToPromotions(blockId: Long, bannerId: Long) {
    navigate(
        AllPromotionsNavKey(
            dataSource = AllPromotionsFragment.DataSource.ByBanner(bannerId, blockId)
        )
    )
}

fun Navigator.navigateToSearch(query: String = "") {
    navigate(SearchNavKey(query = query))
}

fun Navigator.navigateToHurryBuyUpProducts() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.HurryBuyUpProducts))
}

fun Navigator.navigateToNewProducts() {
    navigate(ProductCatalogNavKey(ProductCatalogNavKey.DataSource.NewProducts))
}

fun Navigator.navigateToPromotions() {
    navigate(AllPromotionsNavKey())
}

fun Navigator.navigateToWaterApp() {
    navigate(WaterAppNavKey)
}

fun Navigator.navigateToBuyCertificate() {
    navigate(BuyCertificateNavKey)
}

fun Navigator.navigateToWebView(
    url: String,
    title: String = "",
    navOptions: NavOptions? = null,
) {
    navigate(WebViewNavKey(url = url, title = title))
}

fun Navigator.navigateToAllServices() {
    navigate(AboutServicesNavKey)
}

fun Navigator.navigateToImagePicker() {
    navigate(ImagePickerNavKey)
}
