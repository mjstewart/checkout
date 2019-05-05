package shoppingrus

import shoppingrus.catalogue.Catalogue
import shoppingrus.catalogue.catalogueOf
import shoppingrus.checkout.Checkout
import shoppingrus.domain.Product
import shoppingrus.domain.SkuConstants.*
import shoppingrus.pricing.DefaultPricingStrategy
import shoppingrus.pricing.RuleDefinitions

fun main() {
    val catalogue: Catalogue = catalogueOf(listOf(
            Product(IPD.sku, "Super iPad", 549.99),
            Product(MBP.sku, "MacBook Pro", 1399.99),
            Product(ATV.sku, "Apple TV", 109.50),
            Product(VGA.sku, "VGA adapter", 30.00)
    ))

    val checkout = Checkout(DefaultPricingStrategy(catalogue = catalogue, rules = RuleDefinitions.standard()))

    checkout.scan(ATV.sku)
            .scan(IPD.sku)
            .scan(IPD.sku)
            .scan(ATV.sku)
            .scan(IPD.sku)
            .scan(IPD.sku)
            .scan(IPD.sku)

    println(checkout.total())
}