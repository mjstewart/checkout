package shoppingrus.pricing

import shoppingrus.domain.LineItem
import shoppingrus.domain.PricingContext
import shoppingrus.domain.Sku

typealias PriceMapper = (LineItem, PricingContext) -> Double
typealias RuleMappings = Map<Sku, Rule>

data class Rule(val description: String, val mapper: PriceMapper) {
    operator fun invoke(lineItem: LineItem, context: PricingContext): Double = mapper(lineItem, context)
}

object RuleDefinitions {

    fun withoutDiscount() = Rule(
            description = "No discount is applied - product price multiplied by qty",
            mapper = { (product, qty), _ -> product.price * qty }
    )

    /**
     * Associates a Sku to a rule which handles the discounting policy.
     *
     * For example, if the 'ipd' rule is triggered, the mapper function receives the 'ipd' LineItem and
     * PricingContext. The PricingContext contains all other LineItems in current checkout as well as the
     * inventory catalogue.
     */
    fun standard(): RuleMappings {
        val rules = mutableMapOf<Sku, Rule>()

        rules[Sku("ipd")] = Rule(
                description = "Buy more than 4 iPads and drop the unit price to $499.99 each",
                mapper = { (product, qty), _ ->
                    if (qty > 4) {
                        499.99 * qty
                    } else {
                        product.price * qty
                    }
                })

        rules[Sku("atv")] = Rule(
                description = "Buy 3 Apple TV's and only pay for 2",
                mapper = { (product, qty), _ ->
                    if (qty == 3) {
                        product.price * 2
                    } else {
                        product.price * qty
                    }
                })

        rules[Sku("vga")] = Rule(
                description = "Each MacBook Pro sold receives a free VGA adapter",
                mapper = { vgaLineItem, context ->
                    context.lineItems[Sku("mbp")]?.let { mbpLineItem ->
                        if (vgaLineItem.qty > mbpLineItem.qty) {
                            // pay for the additional adapters
                            vgaLineItem.product.price * (vgaLineItem.qty - mbpLineItem.qty)
                        } else {
                            0.0
                        }
                    } ?: vgaLineItem.product.price * vgaLineItem.qty
                })

        return rules
    }
}

