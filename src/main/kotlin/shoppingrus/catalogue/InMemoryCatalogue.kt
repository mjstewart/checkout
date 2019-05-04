package shoppingrus.catalogue

import shoppingrus.domain.Product
import shoppingrus.domain.Sku

enum class SkuConstants(val sku: Sku) {
    IPD(Sku("ipd")),
    MBP(Sku("mbp")),
    ATV(Sku("atv")),
    VGA(Sku("vga")),
}

class InMemoryCatalogue(private val catalogue: MutableMap<Sku, Product> = mutableMapOf()): Catalogue {
    override fun add(sku: Sku, product: Product): Product? = catalogue.put(sku, product)

    override fun get(sku: Sku): Product? = catalogue[sku]
}