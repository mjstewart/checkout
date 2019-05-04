package shoppingrus.pricing

import shoppingrus.domain.Sku

interface PricingStrategy {
    fun calculate(skus: List<Sku>): Double
}

