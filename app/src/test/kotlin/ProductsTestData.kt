import com.vodovoz.app.domain.general.model.product.PriceModel
import com.vodovoz.app.domain.general.model.product.ProductModel

data object ProductsTestData {

    val product = ProductModel(
        1L,
        "",
        1,
        true,
        1f,
        "",
        1f,
        1,
        1,
        1,
        "",
        PriceModel(1f, 1f, 1, 1),
        emptyList(),
        emptyList(),
        null,
        null
    )


}