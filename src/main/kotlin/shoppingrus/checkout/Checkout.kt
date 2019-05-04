package shoppingrus.checkout

import shoppingrus.domain.Sku
import shoppingrus.pricing.PricingStrategy

class Checkout(private val pricingStrategy: PricingStrategy) {
    private val purchases: MutableList<Sku> = mutableListOf()

    fun scan(sku: Sku, qty: Int = 1): Checkout = (0 until qty).forEach { _ ->
        purchases.add(sku)
    }.let { this }

    fun total(): Double = pricingStrategy.calculate(purchases)
}