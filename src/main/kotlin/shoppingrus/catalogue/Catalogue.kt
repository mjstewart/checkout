package shoppingrus.catalogue

import shoppingrus.domain.Product
import shoppingrus.domain.Sku

interface Catalogue {
    fun add(sku: Sku, product: Product): Product?
    fun get(sku: Sku): Product?
    fun getAll(): Sequence<Product>
}