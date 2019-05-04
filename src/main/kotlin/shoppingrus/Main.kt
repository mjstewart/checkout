package shoppingrus

import shoppingrus.catalogue.Catalogue
import shoppingrus.catalogue.catalogueOf
import shoppingrus.checkout.Checkout
import shoppingrus.domain.Product
import shoppingrus.domain.SkuConstants
import shoppingrus.pricing.DefaultPricingStrategy
import shoppingrus.pricing.RuleDefinitions

fun main() {
    val catalogue: Catalogue = catalogueOf(listOf(
            Product(SkuConstants.IPD.sku, "Super iPad", 549.99),
            Product(SkuConstants.MBP.sku, "MacBook Pro", 1399.99),
            Product(SkuConstants.ATV.sku, "Apple TV", 109.50),
            Product(SkuConstants.VGA.sku, "VGA adapter", 30.00)
    ))

    val checkout = Checkout(DefaultPricingStrategy(catalogue = catalogue, rules = RuleDefinitions.standard()))

    catalogue.get(SkuConstants.ATV.sku)?.also { checkout.scan(it.sku, qty = 4) }
    catalogue.get(SkuConstants.VGA.sku)?.also { checkout.scan(it.sku) }

    println(checkout.total())
}