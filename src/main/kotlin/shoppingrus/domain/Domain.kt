package shoppingrus.domain

import shoppingrus.catalogue.Catalogue

enum class SkuConstants(val sku: Sku) {
    IPD(Sku("ipd")),
    MBP(Sku("mbp")),
    ATV(Sku("atv")),
    VGA(Sku("vga")),
}

inline class Sku(val value: String)

data class Product(val sku: Sku, val name: String, val price: Double)
data class LineItem(val product: Product, val qty: Int)

data class PricingContext(val lineItems: Map<Sku, LineItem>, val catalogue: Catalogue)