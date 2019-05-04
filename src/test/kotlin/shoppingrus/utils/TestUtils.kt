package shoppingrus.utils

import shoppingrus.catalogue.InMemoryCatalogue
import shoppingrus.domain.LineItem
import shoppingrus.domain.PricingContext

fun pricingContextFrom(vararg lineItems: LineItem): PricingContext {
    val catalogue = lineItems.map { it.product }.let { products ->
        InMemoryCatalogue().also { catalogue ->
            products.forEach { catalogue.add(it.sku, it) }
        }
    }
    val lineItemMap = lineItems.associateBy { it.product.sku }
    return PricingContext(lineItemMap, catalogue)
}