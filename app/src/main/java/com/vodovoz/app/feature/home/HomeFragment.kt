package com.vodovoz.app.feature.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.BaseFragment
import com.vodovoz.app.common.content.itemadapter.bottomitem.BottomProgressItem
import com.vodovoz.app.common.jivochat.JivoChatController
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.media.MediaManager
import com.vodovoz.app.common.permissions.PermissionsController
import com.vodovoz.app.common.product.rating.RatingProductManager
import com.vodovoz.app.common.speechrecognizer.SpeechDialogFragment
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.data.model.common.ActionEntity
import com.vodovoz.app.databinding.FragmentMainHomeFlowBinding
import com.vodovoz.app.feature.all.promotions.AllPromotionsFragment
import com.vodovoz.app.feature.productlistnofilter.ProductCatalogFragment
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.model.CommentUI
import com.vodovoz.app.ui.model.SectionDataUI
import com.vodovoz.app.util.extensions.addOnBackPressedCallback
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.snack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment1 : BaseFragment() {

    override fun layout(): Int = R.layout.fragment_main_home_flow
    override fun update() {
        flowViewModel.refresh()
    }

    private val binding: FragmentMainHomeFlowBinding by viewBinding {
        FragmentMainHomeFlowBinding.bind(
            contentView
        )
    }

    internal val flowViewModel: HomeFlowViewModel by activityViewModels()

    @Inject
    lateinit var ratingProductManager: RatingProductManager

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var siteStateManager: SiteStateManager

    @Inject
    lateinit var mediaManager: MediaManager

    @Inject
    lateinit var accountManager: AccountManager


    @Inject
    lateinit var cookieManager: com.vodovoz.app.common.cookie.CookieManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeTabReselect()
        observeEvents()
        observeDeepLinkFromSiteState()
        observePushFromSiteState()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewPager()
        initImageRv()
        initBottomSheetCallback()
        initJivoChatButton()

        bindErrorRefresh { flowViewModel.refresh() }
        bindBackPressed()
    }

    private fun initJivoChatButton() {
        binding.fabJivoSite.isVisible = JivoChatController.isActive()
        binding.fabJivoSite.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionToWebViewFragment(
                    JivoChatController.getLink(),
                    ""
                )
            )
        }
    }

    private fun initViewPager() {
        binding.rateViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.dotsIndicator.attachTo(binding.rateViewPager)
    }

    private fun initImageRv() {
        with(binding.collapsedRv) {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        }
    }

    private fun initBottomSheetCallback() {
        val behavior = BottomSheetBehavior.from(binding.rateBottom)
        val density = requireContext().resources.displayMetrics.density
        behavior.peekHeight = (100 * density).toInt()
//        behavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (slideOffset > 0) {
                    binding.collapsedLL.alpha = 1 - 2 * slideOffset
                    binding.expandedLL.alpha = slideOffset * slideOffset

                    if (slideOffset > 0.5) {
                        binding.collapsedLL.visibility = View.GONE
                        binding.expandedLL.visibility = View.VISIBLE
                        bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    }

                    if (slideOffset < 0.5 && binding.expandedLL.visibility == View.VISIBLE) {
                        binding.collapsedLL.visibility = View.VISIBLE
                        binding.expandedLL.visibility = View.INVISIBLE
                    }
                } else {
                    binding.rateBottom.visibility = View.GONE
                }
            }
        })
    }

    private fun observePushFromSiteState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                siteStateManager
                    .observePush()
                    .collect {
                        debugLog { "push ${it?.path} $siteStateManager" }
                        when (it?.path) {
                            "AKCII" -> {
                                val promotionId = it.id
                                if (!promotionId.isNullOrEmpty()) {
                                    val eventParameters = "\"ID_AKCII\": \"$promotionId\""
                                    accountManager.reportEvent(
                                        "Зашел в акцию (push)",
                                        eventParameters
                                    )

                                    findNavController().navigate(
                                        HomeFragmentDirections.actionToPromotionDetailFragment(
                                            promotionId.toLong()
                                        )
                                    )
                                }
                            }

                            "TOVAR" -> {
                                val productId = it.id
                                if (!productId.isNullOrEmpty()) {
                                    val eventParameters = "\"ID_Product\": \"$productId\""
                                    accountManager.reportEvent(
                                        "Зашел в товар (push)",
                                        eventParameters
                                    )

                                    findNavController().navigate(
                                        HomeFragmentDirections.actionToProductDetailFragment(
                                            productId.toLong()
                                        )
                                    )
                                }
                            }

                            "RAZDEL" -> {
                                val sectionId = it.id
                                if (!sectionId.isNullOrEmpty()) {
                                    val eventParameters = "\"Secition_ID\": \"$sectionId\""
                                    accountManager.reportEvent(
                                        "Зашел в раздел (push)",
                                        eventParameters
                                    )

//                                    findNavController().navigate(
//                                        HomeFragmentDirections.actionToPaginatedProductsCatalogFragment(
//                                            sectionId.toLong()
//                                        )
//                                    )
                                }
                            }

                            "Karta" -> {
                                val orderId = it.orderId
                                if (!orderId.isNullOrEmpty()) {
                                    val eventParameters = "\"ID_Zakaz\": \"$orderId\""
                                    accountManager.reportEvent(
                                        "Зашел в заказ, статус в пути (push)",
                                        eventParameters
                                    )

                                    findNavController().navigate(
                                        HomeFragmentDirections.actionToOrderDetailsFragment(
                                            orderId.toLong()
                                        )
                                    )
                                }
                            }

                            "vsenovinki" -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                                        ProductCatalogFragment.DataSource.NewProducts
                                    )
                                )
                            }

                            "vseskidki" -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                                        ProductCatalogFragment.DataSource.HurryBuyUpProducts
                                    )
                                )
                            }

                            "BRAND" -> {
                                val brandId = it.id
                                if (!brandId.isNullOrEmpty()) {
                                    findNavController().navigate(
                                        HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                                            ProductCatalogFragment.DataSource.Brand(
                                                brandId.toLong()
                                            )
                                        )
                                    )
                                } else {
                                    findNavController().navigate(HomeFragmentDirections.actionToAllBrandsFragment())
                                }
                            }

                            "BRANDY" -> {
                                //findNavController()
                                findNavController().navigate(HomeFragmentDirections.actionToAllBrandsFragment())
                                siteStateManager.clearPushListener()
                            }

                            "about" -> {
                                val section = it.section ?: return@collect
                                if (section == "О магазине") {
                                    findNavController().navigate(
                                        HomeFragmentDirections.actionToWebViewFragment(
                                            ApiConfig.ABOUT_SHOP_URL,
                                            "О магазине"
                                        )
                                    )
                                }
                                if (section == "Связаться с нами") {
                                    findNavController().navigate(HomeFragmentDirections.actionToContactsFragment())
                                }
                            }

                            "dostavka" -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToWebViewFragment(
                                        ApiConfig.ABOUT_DELIVERY_URL,
                                        "О доставке"
                                    )
                                )
                            }

                            "service" -> {
                                findNavController().navigate(HomeFragmentDirections.actionToAboutServicesDialogFragment())
                            }

                            "remont_kulerov" -> {
                                findNavController().navigate(HomeFragmentDirections.actionToAboutServicesDialogFragment())
                            }

                            "feedback" -> {
                                findNavController().navigate(HomeFragmentDirections.actionToContactsFragment())
                            }

                            "TOVARY" -> {

                            }

                            "ACTIONS" -> {

                            }

                            "vseakcii" -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToAllPromotionsFragment(
                                        AllPromotionsFragment.DataSource.All
                                    )
                                )
                            }

                            "URL" -> {
                                val url = it.id ?: return@collect
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToWebViewFragment(
                                        url,
                                        ""
                                    )
                                )
                            }

                            "trekervodi" -> {
                                val eventName = "trekervodi_push"
                                accountManager.reportEvent(eventName)
                                findNavController().navigate(HomeFragmentDirections.actionToWaterAppFragment())
                            }

                            "profil" -> {
                                flowViewModel.goToProfile()
                            }

                            "pokypkasertificat" -> {
                                debugLog { "pokypkasertificat push" }
                                findNavController().navigate(HomeFragmentDirections.actionToBuyCertificateFragment())
                            }

                            null -> {}
                        }
                        it?.action?.let { action ->
                            if (action.contains("SOBNEW")) {
                                val eventParameters = "\"SOBNEW_NAME\": \"${it.id}\""
                                accountManager.reportEvent(
                                    "Зашел в приложение (push)",
                                    eventParameters
                                )
                            }
                        }
                        debugLog { "clear push" }
                        siteStateManager.clearPushListener()
                    }
            }
        }
    }

    private fun observeDeepLinkFromSiteState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                siteStateManager
                    .observeDeepLinkPath()
                    .collect {
                        when (it) {
                            /*"catalog" -> {
                            tabManager.selectTab(R.id.graph_catalog)
                            siteStateManager.clearDeepLinkListener()
                        }
                        "action" -> {
                            findNavController().navigate(
                                HomeFragmentDirections.actionToAllPromotionsFragment(
                                    AllPromotionsFragment.DataSource.All()
                                )
                            )
                            siteStateManager.clearDeepLinkListener()
                        }
                        "brand" -> {
                            findNavController().navigate(HomeFragmentDirections.actionToAllBrandsFragment())
                            siteStateManager.clearDeepLinkListener()
                        }

                        "about" -> {
                            findNavController().navigate(
                                HomeFragmentDirections.actionToWebViewFragment(
                                    ApiConfig.ABOUT_SHOP_URL,
                                    "О магазине"
                                )
                            )
                            siteStateManager.clearDeepLinkListener()
                        }
                        "dostavka" -> {
                            findNavController().navigate(
                                HomeFragmentDirections.actionToWebViewFragment(
                                    ApiConfig.ABOUT_DELIVERY_URL,
                                    "О доставке"
                                )
                            )
                            siteStateManager.clearDeepLinkListener()
                        }
                        "service" -> {
                            findNavController().navigate(HomeFragmentDirections.actionToAboutServicesDialogFragment())
                            siteStateManager.clearDeepLinkListener()
                        }
                        "remont_kulerov" -> {
                            findNavController().navigate(HomeFragmentDirections.actionToAboutServicesDialogFragment())
                            siteStateManager.clearDeepLinkListener()
                        }
                        "feedback" -> {
                            findNavController().navigate(HomeFragmentDirections.actionToContactsFragment())
                            siteStateManager.clearDeepLinkListener()
                        }
                        "basket" -> {
                            tabManager.selectTab(R.id.graph_cart)
                        }*/
                            "mobile_app/" -> {
                                findNavController().navigate(HomeFragmentDirections.actionToAboutAppDialogFragment())
                            }

                            "gl/" -> {
                            }

                            "kalkulyator_vody/" -> {
                                val eventName = "trekervodi_ssilka"
                                accountManager.reportEvent(eventName)
                                findNavController().navigate(HomeFragmentDirections.actionToWaterAppFragment())
                            }
                        }

                        siteStateManager.clearDeepLinkListener()
                    }
            }
        }
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                flowViewModel.observeEvent()
                    .collect {
                        when (it) {
                            is HomeFlowViewModel.HomeEvents.GoToPreOrder -> {
//
                            }

                            is HomeFlowViewModel.HomeEvents.GoToProfile -> {
                                tabManager.setAuthRedirect(findNavController().graph.id)
                                tabManager.selectTab(R.id.graph_profile)
                            }

                            is HomeFlowViewModel.HomeEvents.GoToCart -> {
                                MaterialAlertDialogBuilder(requireContext())
                                    .setTitle("Товары добавлены в корзину")
                                    .setMessage("Перейти в корзину?")
                                    .setPositiveButton("Да") { dialog, _ ->
                                        dialog.dismiss()
                                        tabManager.selectTab(R.id.graph_cart)
                                    }
                                    .setNegativeButton("Нет") { dialog, _ -> dialog.dismiss() }
                                    .show()
                            }

                            is HomeFlowViewModel.HomeEvents.GoToStories -> {
                                findNavController().navigate(
                                    HomeFragmentDirections.actionToFullScreenHistorySliderFragment(
                                        it.storyId
                                    )
                                )
                            }
                            else -> {}
                        }
                    }
            }
        }
    }





    internal fun ActionEntity.invoke(
        navController: NavController = findNavController(),
        activity: FragmentActivity = requireActivity(),
    ) {
        val navDirect = when (this) {
            is ActionEntity.Brand ->
                HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                    ProductCatalogFragment.DataSource.Brand(brandId = this.brandId)
                )

            is ActionEntity.Brands -> {
                HomeFragmentDirections.actionToAllBrandsFragment(this.brandIdList.toLongArray())
            }

            is ActionEntity.Product ->
                HomeFragmentDirections.actionToProductDetailFragment(this.productId)

            is ActionEntity.Products -> HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                ProductCatalogFragment.DataSource.HurryBuyUpProducts
            )


            is ActionEntity.Promotion ->
                HomeFragmentDirections.actionToPromotionDetailFragment(this.promotionId)

            is ActionEntity.Promotions -> HomeFragmentDirections.actionToAllPromotionsFragment(
                AllPromotionsFragment.DataSource.ByBanner(-1, -1) //todo - put a actual realization
            )

            is ActionEntity.AllPromotions -> HomeFragmentDirections.actionToAllPromotionsFragment(
                AllPromotionsFragment.DataSource.All
            )

            is ActionEntity.Link -> {
                val openLinkIntent = Intent(Intent.ACTION_VIEW, Uri.parse(this.url))
                activity.startActivity(openLinkIntent)
                null
            }

            is ActionEntity.LinkWithCookies -> {
                setCookie()
                HomeFragmentDirections.actionToWebViewFragment(
                    url,
                    "",
                )
                null
            }

            is ActionEntity.Category -> HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                ProductCatalogFragment.DataSource.HurryBuyUpProducts
            )


            is ActionEntity.Discount -> HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                ProductCatalogFragment.DataSource.HurryBuyUpProducts
            )

            is ActionEntity.Novelties -> HomeFragmentDirections.actionToPaginatedProductsCatalogWithoutFiltersFragment(
                ProductCatalogFragment.DataSource.NewProducts
            )

            is ActionEntity.WaterApp -> {
                HomeFragmentDirections.actionToWaterAppFragment()
            }

            is ActionEntity.Delivery -> HomeFragmentDirections.actionToWebViewFragment(
                ApiConfig.ABOUT_DELIVERY_URL,
                "О доставке"
            )

            is ActionEntity.Profile -> {
                flowViewModel.goToProfile()
                null
            }

            is ActionEntity.BuyCertificate -> {
                HomeFragmentDirections.actionToBuyCertificateFragment()
            }
        }
        navDirect?.let { navController.navigate(navDirect) }
    }


    private fun observeTabReselect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager.observeTabReselect()
                    .collect {
                        if (it != TabManager.DEFAULT_STATE && it == R.id.homeFragment) {
                            binding.homeRv.post {
                                binding.homeRv.smoothScrollToPosition(0)
                            }
                            tabManager.setDefaultState()
                        }
                    }
            }
        }
    }

    private fun bindBackPressed() {
        var back = false
        addOnBackPressedCallback {
            if (!back) {
                requireActivity().snack("Нажмите назад еще раз, чтобы выйти")
                back = true
            } else {
                requireActivity().finish()
            }
        }
    }

    @Inject
    lateinit var permissionsControllerFactory: PermissionsController.Factory
    private val permissionsController by lazy { permissionsControllerFactory.create(requireActivity()) }

    private fun navigateToQrCodeFragment() {
        permissionsController.methodRequiresCameraPermission {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@methodRequiresCameraPermission
            }

            findNavController().navigate(R.id.qrCodeFragment)

        }
    }

    private fun startSpeechRecognizer() {
        permissionsController.methodRequiresRecordAudioPermission {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@methodRequiresRecordAudioPermission
            }

            SpeechDialogFragment().show(childFragmentManager, "TAG")

        }
    }

    private fun setCookie() {
        val webkitCookieManager = CookieManager.getInstance()
        webkitCookieManager.acceptCookie()
        webkitCookieManager.setCookie(
            ApiConfig.VODOVOZ_URL,
            cookieManager.fetchCookieSessionId()
        )
    }
}