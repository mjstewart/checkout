package shoppingrus.domain

import shoppingrus.catalogue.Catalogue

inline class Sku(val value: String)

data class Product(val sku: Sku, val name: String, val price: Double)
data class LineItem(val product: Product, val qty: Int)

data class PricingContext(val lineItems: Map<Sku, LineItem>, val catalogue: Catalogue)