package shoppingrus.catalogue

import shoppingrus.domain.LineItem
import shoppingrus.domain.PricingContext
import shoppingrus.domain.Product
import shoppingrus.domain.Sku

class InMemoryCatalogue(private val catalogue: MutableMap<Sku, Product> = mutableMapOf()) : Catalogue {
    override fun add(sku: Sku, product: Product): Product? = catalogue.put(sku, product)

    override fun get(sku: Sku): Product? = catalogue[sku]

    override fun getAll(): Sequence<Product> = catalogue.values.asSequence()
}

fun InMemoryCatalogue.purchase(purchases: List<Pair<Sku, Int>> = emptyList()): PricingContext =
        purchases.filterNot { get(it.first) == null }
                .associateBy({ it.first }, { LineItem(get(it.first)!!, it.second) })
                .let { PricingContext(it, this) }

fun catalogueOf(products: List<Product> = emptyList()): InMemoryCatalogue =
        InMemoryCatalogue().also { catalogue ->
            products.forEach { catalogue.add(it.sku, it) }
        }