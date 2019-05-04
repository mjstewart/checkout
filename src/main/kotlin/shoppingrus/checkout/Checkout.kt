package shoppingrus.checkout

import shoppingrus.catalogue.Catalogue
import shoppingrus.catalogue.InMemoryCatalogue
import shoppingrus.catalogue.SkuConstants.*
import shoppingrus.domain.Product
import shoppingrus.domain.Sku
import shoppingrus.pricing.DefaultPricingStrategy
import shoppingrus.pricing.PricingStrategy
import shoppingrus.pricing.RuleDefinitions


class Checkout(private val pricingStrategy: PricingStrategy) {
    private val purchases: MutableList<Sku> = mutableListOf()

    fun scan(sku: Sku, qty: Int = 1) {
        (0..qty).forEach { _ ->
            purchases.add(sku)
        }
    }

    fun total(): Double = pricingStrategy.calculate(purchases)
}

/**
 * test cases
 *
 * no products = $0.00
 * edge case around many VGA with Macbook pro, only 1 VGA should be deducted
 */

fun main() {
    val catalogue: Catalogue = sequenceOf(
            Product(IPD.sku, "Super iPad", 549.99),
            Product(MBP.sku, "MacBook Pro", 1399.99),
            Product(ATV.sku, "Apple TV", 109.50),
            Product(VGA.sku, "VGA adaptor", 30.00)
    ).associateByTo(mutableMapOf(), { it.sku }).let { InMemoryCatalogue(it) }

    val checkout = Checkout(DefaultPricingStrategy(catalogue = catalogue, rules = RuleDefinitions.standard()))

    catalogue.get(ATV.sku)?.also { checkout.scan(it.sku, qty = 2) }
    catalogue.get(VGA.sku)?.also { checkout.scan(it.sku) }
    catalogue.get(IPD.sku)?.also { checkout.scan(it.sku, qty = 5) }

    println(checkout.total())


}