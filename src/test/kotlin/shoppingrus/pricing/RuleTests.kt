package shoppingrus.pricing

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import shoppingrus.domain.SkuConstants.*
import shoppingrus.domain.Product
import shoppingrus.domain.Sku
import shoppingrus.utils.pricingContext

class RuleTests {
    private val rules: Map<Sku, Rule> = RuleDefinitions.standard()

    @Test
    fun `Given without discount, then apply full price`() {
        val missingSkuA = Sku("a")
        val missingSkuB = Sku("b")

        val context = pricingContext(
                products = listOf(
                        Product(missingSkuA, "product a", 549.99),
                        Product(missingSkuB, "product b", 1399.99)),
                purchases = listOf(
                        missingSkuA to 1,
                        missingSkuB to 3
                )
        )

        val rule = RuleDefinitions.withoutDiscount()

        assertThat(rule(context.lineItems.getValue(missingSkuA), context)).isEqualTo(549.99)
        assertThat(rule(context.lineItems.getValue(missingSkuB), context)).isEqualTo(1399.99 * 3)
    }

    @DisplayName("given Macbook Pro")
    @Nested
    inner class GivenMacBookPro {

        @Test
        fun `then no custom rule should exist`() {
            assertThat(rules.containsKey(MBP.sku)).isFalse()
        }
    }

    @DisplayName("given iPad")
    @Nested
    inner class GivenIPad {

        @Test
        fun `then rule should exist`() {
            assertThat(rules.containsKey(IPD.sku)).isTrue()
        }

        private fun assertRule(qty: Int, price: Double = 549.99) {
            val context = pricingContext(
                    products = listOf(Product(IPD.sku, "Super iPad test", price)),
                    purchases = listOf(IPD.sku to qty)
            )
            val iPad = context.lineItems.getValue(IPD.sku)
            val rule = rules.getValue(IPD.sku)
            assertThat(rule(iPad, context)).isEqualTo(iPad.product.price * iPad.qty)
        }

        @Test
        fun `when less than 4 purchased, then no discount`() {
            assertRule(qty = 3)
        }

        @Test
        fun `when exactly 4 purchased, then no discount`() {
            assertRule(qty = 4)
        }

        @Test
        fun `when more than 4 purchased, then apply bulk discount`() {
            assertRule(qty = 5, price = 499.99)
        }
    }

    @DisplayName("given apple tv")
    @Nested
    inner class GivenAppleTV {

        @Test
        fun `then rule should exist`() {
            assertThat(rules.containsKey(ATV.sku)).isTrue()
        }

        @Test
        fun `when exactly 3 purchased, then only charge for the cost of 2`() {
            val context = pricingContext(
                    products = listOf(Product(ATV.sku, "Apple TV test", 109.50)),
                    purchases = listOf(ATV.sku to 3)
            )
            val tv = context.lineItems.getValue(ATV.sku)
            val rule = rules.getValue(tv.product.sku)
            assertThat(rule(tv, context)).isEqualTo(tv.product.price * 2)
        }

        @Test
        fun `when 3 items are not purchased, then no discounts`() {
            val context = pricingContext(
                    products = listOf(Product(ATV.sku, "Apple TV test", 109.50)),
                    purchases = listOf(ATV.sku to 2)
            )
            val tv = context.lineItems.getValue(ATV.sku)
            val rule = rules.getValue(tv.product.sku)
            assertThat(rule(tv, context)).isEqualTo(tv.product.price * tv.qty)
        }
    }

    @DisplayName("given VGA adapter")
    @Nested
    inner class GivenVGAAdapter {

        @Test
        fun `then rule should exist`() {
            assertThat(rules.containsKey(VGA.sku)).isTrue()
        }

        @Test
        fun `when macbook is not in order, then charge for all adapters`() {
            val context = pricingContext(
                    products = listOf(Product(VGA.sku, "VGA adapter test", 30.00)),
                    purchases = listOf(VGA.sku to 3)
            )
            val vga = context.lineItems.getValue(VGA.sku)
            val rule = rules.getValue(vga.product.sku)
            assertThat(rule(vga, context)).isEqualTo(vga.product.price * vga.qty)
        }

        @Test
        fun `when many macbooks exist in order with equal vga count, then adapter charge is free`() {
            val qty = 3
            val context = pricingContext(
                    products = listOf(
                            Product(VGA.sku, "VGA adapter test", 30.00),
                            Product(MBP.sku, "MacBook Pro test", 1399.99)
                    ),
                    purchases = listOf(VGA.sku to qty, MBP.sku to qty)
            )

            val vga = context.lineItems.getValue(VGA.sku)
            val mbp = context.lineItems.getValue(MBP.sku)

            val rule = rules.getValue(vga.product.sku)
            assertThat(vga.qty == qty && mbp.qty == qty).isTrue()
            assertThat(rule(vga, context)).isEqualTo(0.0)
        }

        @Test
        fun `when less adapters than macbooks in order, then adapter charge is free`() {
            val context = pricingContext(
                    products = listOf(
                            Product(VGA.sku, "VGA adapter test", 30.00),
                            Product(MBP.sku, "MacBook Pro test", 1399.99)
                    ),
                    purchases = listOf(VGA.sku to 2, MBP.sku to 3)
            )

            val vga = context.lineItems.getValue(VGA.sku)
            val mbp = context.lineItems.getValue(MBP.sku)
            val rule = rules.getValue(vga.product.sku)

            assertThat(vga.qty < mbp.qty).isTrue()
            assertThat(rule(vga, context)).isEqualTo(0.0)
        }

        @Test
        fun `when more adapters than macbooks in order, then only pay for the additional adapters`() {
            val context = pricingContext(
                    products = listOf(
                            Product(VGA.sku, "VGA adapter test", 30.00),
                            Product(MBP.sku, "MacBook Pro test", 1399.99)
                    ),
                    purchases = listOf(VGA.sku to 5, MBP.sku to 3)
            )

            val vga = context.lineItems.getValue(VGA.sku)
            val mbp = context.lineItems.getValue(MBP.sku)
            val rule = rules.getValue(vga.product.sku)

            val additionalAdapters = vga.qty - mbp.qty
            val expectedAdditionalAdapterCost = additionalAdapters * vga.product.price

            assertThat(vga.qty > mbp.qty).isTrue()
            assertThat(rule(vga, context)).isEqualTo(expectedAdditionalAdapterCost)
        }
    }
}