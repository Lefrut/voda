import com.vodovoz.app.design_system.model.CategoryWithProductsUi
import com.vodovoz.app.design_system.model.ProductUi

object VodovozItemTestData {

    val apple = ProductUi(
        rating = 4.5f,
        price = 1.2f,
        oldPrice = 1.5f,
        name = "Apple",
        image = "apple.png",
        labels = emptyList(),
        isAvailable = true,
        pricePerUnit = 120,
        unitOfMeasurement = "kg",
        button = null,
        id = 101L,
        isFavorite = false,
        cartQuantity = 0,
        cartLoading = false,
        forAdults = null
    )

    val banana = ProductUi(
        rating = 4.0f,
        price = 0.8f,
        oldPrice = 1.0f,
        name = "Banana",
        image = "banana.png",
        labels = emptyList(),
        isAvailable = true,
        pricePerUnit = 80,
        unitOfMeasurement = "kg",
        button = null,
        id = 102L,
        isFavorite = true,
        cartQuantity = 2,
        cartLoading = false,
        forAdults = null
    )

    val beer = ProductUi(
        rating = 4.7f,
        price = 2.5f,
        oldPrice = 3.0f,
        name = "Beer",
        image = "beer.png",
        labels = emptyList(),
        isAvailable = true,
        pricePerUnit = 250,
        unitOfMeasurement = "l",
        button = null,
        id = 201L,
        isFavorite = false,
        cartQuantity = 1,
        cartLoading = false,
        forAdults = null
    )

    val fruitsCategory = CategoryWithProductsUi(
        id = 1L,
        name = "Fruits",
        items = listOf(apple, banana)
    )

    val drinksCategory = CategoryWithProductsUi(
        id = 2L,
        name = "Drinks",
        items = listOf(beer)
    )

    val products = listOf(apple, banana, beer)
    val categories = listOf(fruitsCategory, drinksCategory)
}