package shoppingrus.pricing

import shoppingrus.catalogue.Catalogue
import shoppingrus.domain.LineItem
import shoppingrus.domain.PricingContext
import shoppingrus.domain.Sku

/**
 * The DefaultPricingStrategy uses an inventory catalogue to retrieve product details to be consumed by
 * pricing rules to calculate the total order cost.
 */
class DefaultPricingStrategy(
        private val catalogue: Catalogue,
        private val rules: RuleMappings) : PricingStrategy {

    /**
     * Apply the rule for the given sku otherwise no discounts are applied
     */
    private fun resolve(lineItem: LineItem, context: PricingContext) =
            rules.getOrDefault(lineItem.product.sku, RuleDefinitions.withoutDiscount())(lineItem, context)

    override fun calculate(skus: List<Sku>): Double {
        /*
         * Sku as the key enables rules to lookup products in the current checkout in O(1).
         * Mapping the value to a LineItem simplifies rules from needing to re-lookup the product from the catalogue.
         */
        val lineItems: Map<Sku, LineItem> = skus.mapNotNull { catalogue.get(it) }
                .groupingBy { it }.eachCount()
                .toList()
                .associateBy({ it.first.sku }, { LineItem(it.first, it.second) })

        // Rules may need to query both the ordered items and the inventory catalogue
        val context = PricingContext(lineItems, catalogue)

        return lineItems.values.fold(0.0, { acc, lineItem ->
            acc + resolve(lineItem, context)
        })
    }
}