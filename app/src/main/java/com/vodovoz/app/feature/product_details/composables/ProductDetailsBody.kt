package com.vodovoz.app.feature.product_details.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vodovoz.app.design_system.model.BrandCategoryItemUi
import com.vodovoz.app.design_system.model.CommentUi
import com.vodovoz.app.design_system.model.ProductDetailsButtonsUi
import com.vodovoz.app.design_system.model.ProductDetailsUi
import com.vodovoz.app.design_system.model.ProductMediaUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.design_system.model.SectionUi

@Suppress("NonSkippableComposable")
@Composable
fun ProductDetailsBody(
    modifier: Modifier = Modifier,
    mediaPagerState: PagerState,
    productDetails: ProductDetailsUi,
    comments: List<CommentUi>,
    moreProductSections: List<SectionUi<ProductUi>>,
    buttons: ProductDetailsButtonsUi,
    totalPrice: Int,
    productCartQuantity: Int,
    showDetailText: Boolean,
    showAllProperties: Boolean,
    quantityButtonIsLoading: Boolean,
    onFloatingButtonChange: (Boolean) -> Unit,
    onProductMediaClick: (ProductMediaUi) -> Unit,
    onDescriptionShowOrHide: () -> Unit,
    onAllPropertiesShowOrHide: () -> Unit,
    onDecrementProduct: () -> Unit,
    onIncrementProduct: () -> Unit,
    onAboutProductClick: () -> Unit,
    onShowAllCommentsClick: () -> Unit,
    onMultiButtonClick: () -> Unit,
    onPresentButtonClick: () -> Unit,
    onPreOrderButtonClick: () -> Unit,
    onAnalogButtonClick: () -> Unit,
    onPresentBlockButtonClick: () -> Unit,
    onQueryClick: (String) -> Unit,
    onBrandClick: (BrandCategoryItemUi) -> Unit,
    onCategoryClick: (BrandCategoryItemUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onProductLikeClick: (ProductUi) -> Unit,
    onCopyArticleNumberClick: () -> Unit,
    onProductAnalogsClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onWriteCommentClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        ProductDetailsMediaPager(
            productMediaList = productDetails.mediaList,
            pagerState = mediaPagerState,
            onMediaClick = { media ->
                onProductMediaClick(media)
            },
        )

        ProductDetailsLabels(
            modifier = Modifier.padding(top = 24.dp),
            labels = productDetails.labels
        )

        Text(
            text = productDetails.name,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
        )

        ProductDetailsRatingBar(
            modifier = Modifier.padding(top = 16.dp),
            rating = productDetails.rating,
            numberOfReviews = productDetails.commentsCount,
            articleNumber = productDetails.articleNumber,
            onReviewsClick = onShowAllCommentsClick,
            onCopyClick = onCopyArticleNumberClick,
            onZeroReviewsClick = onWriteCommentClick
        )

        ProductDetailsPriceInfo(
            modifier = Modifier.padding(top = 24.dp),
            deposit = productDetails.deposit,
            firstPrice = productDetails.firstPrice,
            pricePerUnit = productDetails.pricePerUnit ?: ""
        )


        ProductDetailsButtonsBlock(
            isAvailable = productDetails.isAvailable,
            quantityButtonIsLoading = quantityButtonIsLoading,
            cartQuantity = productCartQuantity,
            buttons = buttons,
            totalPrice = totalPrice,
            onProductMinus = onDecrementProduct,
            onProductPlus = onIncrementProduct,
            onFloatingButtonChange = onFloatingButtonChange,
            onPresentButtonClick = onPresentButtonClick,
            onMultiButtonClick = onMultiButtonClick,
            onPreOrderButtonClick = onPreOrderButtonClick,
            onAnalogButtonClick = onAnalogButtonClick,
            onPresentBlockButtonClick = onPresentBlockButtonClick
        )

        ProductDetailsInfo(
            modifier = Modifier.padding(top = 32.dp),
            onAboutProductClick = onAboutProductClick,
            detailInfo = productDetails.detailInfo,
            showDetailText = showDetailText,
            onDescriptionArrowClick = onDescriptionShowOrHide,
            showAllProperties = showAllProperties,
            onPropertiesArrowClick = onAllPropertiesShowOrHide,
            contentBlockCharacteristics = productDetails.characteristics
        )

        val blockBrandCategory = productDetails.blockBrandCategory

        ProductDetailsCategoryAndBrand(
            modifier = Modifier.padding(top = 32.dp),
            category = blockBrandCategory.category,
            brand = blockBrandCategory.brand,
            onBrandClick = onBrandClick,
            onCategoryClick = onCategoryClick
        )


        if (productDetails.sectionQueries.items.any { s -> s.isNotBlank() }) {
            ProductDetailsSearchQueries(
                modifier = Modifier.padding(top = 32.dp),
                sectionQueries = productDetails.sectionQueries,
                onQueryClick = onQueryClick
            )
        }


        ProductDetailsComments(
            modifier = Modifier.padding(top = 32.dp),
            commentsCount = productDetails.commentsCount,
            comments = comments,
            onShowAllCommentsClick = onShowAllCommentsClick,
            onWriteCommentClick = onWriteCommentClick,
        )


        if(moreProductSections.isNotEmpty()){
            moreProductSections.forEach { section ->
                ProductDetailsAccessoryProducts(
                    modifier = Modifier.padding(top = 32.dp),
                    productSection = section,
                    onProductLike = onProductLikeClick,
                    onProductClick = onProductClick,
                    onIncrementProductToCart = onIncrementProductToCart,
                    onDecrementProductToCart = onDecrementProductToCart,
                    onProductAnalogsClick = onProductAnalogsClick
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

    }
}




















