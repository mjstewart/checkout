package shoppingrus.utils

import shoppingrus.catalogue.catalogueOf
import shoppingrus.catalogue.purchase
import shoppingrus.domain.Product
import shoppingrus.domain.Sku

fun pricingContext(products: List<Product>, purchases: List<Pair<Sku, Int>> = emptyList()) =
        catalogueOf(products).purchase(purchases)