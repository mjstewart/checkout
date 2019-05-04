package shoppingrus.checkout

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import shoppingrus.domain.SkuConstants.*
import shoppingrus.domain.Product
import shoppingrus.domain.Sku
import shoppingrus.pricing.DefaultPricingStrategy
import shoppingrus.pricing.Rule
import shoppingrus.pricing.RuleDefinitions
import shoppingrus.catalogue.catalogueOf

class CheckoutTest {

    private val rules: Map<Sku, Rule> = RuleDefinitions.standard()

    @Test
    fun `given 0 scanned, then total should be 0`() {
        val products = listOf(
                Product(IPD.sku, "Super iPad", 549.99),
                Product(MBP.sku, "MacBook Pro", 1399.99),
                Product(ATV.sku, "Apple TV", 109.50),
                Product(VGA.sku, "VGA adaptor", 30.00)
        )

        val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products), rules = rules)

        val total = Checkout(strategy).total()
        assertThat(total).isEqualTo(0.0)
    }

    @Test
    fun `given scanned sku is not in catalogue, then it should be ignored in total`() {
        val products = listOf(
                Product(IPD.sku, "Super iPad", 549.99),
                Product(MBP.sku, "MacBook Pro", 1399.99),
                Product(ATV.sku, "Apple TV", 109.50),
                Product(VGA.sku, "VGA adaptor", 30.00)
        ).associateBy { it.sku }

        val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)

        val total = Checkout(strategy)
                .scan(IPD.sku)
                .scan(Sku("missing"))
                .total()

        assertThat(total).isEqualTo(products.getValue(IPD.sku).price)
    }

    @Test
    fun `given empty catalogue, then total should be 0 regardless of scanned items`() {
        val strategy = DefaultPricingStrategy(catalogue = catalogueOf(), rules = rules)

        val total = Checkout(strategy)
                .scan(IPD.sku)
                .scan(IPD.sku)
                .scan(MBP.sku)
                .scan(Sku("missing"))
                .total()

        assertThat(total).isEqualTo(0.0)
    }

    @Test
    fun `given every item is involved in a discount, scan order should not matter`() {
        val products = listOf(
                Product(IPD.sku, "Super iPad", 549.99),
                Product(MBP.sku, "MacBook Pro", 1399.99),
                Product(ATV.sku, "Apple TV", 109.50),
                Product(VGA.sku, "VGA adaptor", 30.00)
        ).associateBy { it.sku }

        val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)

        val totalOrderingA = Checkout(strategy)
                .scan(ATV.sku, qty = 3)
                .scan(IPD.sku, qty = 5)
                .scan(MBP.sku, qty = 4)
                .scan(VGA.sku, qty = 4)
                .total()

        val totalOrderingB = Checkout(strategy)
                .scan(MBP.sku, qty = 4)
                .scan(VGA.sku, qty = 4)
                .scan(IPD.sku, qty = 5)
                .scan(ATV.sku, qty = 3)
                .total()

        // 3 tvs for the price of 2
        val expectedTvPrice = products.getValue(ATV.sku).price * 2

        // more than 4 iPads get a bulk discount of dropping the unit price
        val expectedIPadPrice = 499.99 * 5

        // scanned 4 mac books which get no discount
        val expectedMBPPrice = products.getValue(MBP.sku).price * 4

        // 4 VGA's are in the order with 4 macs resulting in all adapters being bundled in for free
        val expectedVGAPrice = 0.0

        val expectedTotal = expectedTvPrice + expectedIPadPrice + expectedMBPPrice + expectedVGAPrice

        assertThat(totalOrderingA).isEqualTo(expectedTotal)
        assertThat(totalOrderingB).isEqualTo(expectedTotal)
    }

    @DisplayName("given apple tv scenarios")
    @Nested
    inner class GivenAppleTVScenarios {

        @Test
        fun `when 3 for 2 deal with other non discounted items, then apply tv discount only`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .scan(VGA.sku)
                    .total()

            // 3 tv's for the price of 2
            val expectedTVWithDiscount = products.getValue(ATV.sku).price * 2
            val expectedVGACost = products.getValue(VGA.sku).price
            val expectedTotal = expectedTVWithDiscount + expectedVGACost

            assertThat(total).isEqualTo(expectedTotal)
        }

        @Test
        fun `when not 3 for 2 deal with other non discounted items, then no discount`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .scan(VGA.sku)
                    .total()

            // 4 tv's results in missing out on the 3 for 2 deal
            val expectedTVCost = products.getValue(ATV.sku).price * 4
            val expectedVGACost = products.getValue(VGA.sku).price
            val expectedTotal = expectedTVCost + expectedVGACost

            assertThat(total).isEqualTo(expectedTotal)
        }
    }

    @DisplayName("given super iPad scenarios")
    @Nested
    inner class GivenIPadScenarios {

        @Test
        fun `when more than 4 purchased with other non discounted items, then apply iPad discount only`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .total()

            // Purchase 5 iPads and receive a bulk discount by dropping the unit price to 499.99
            val expectedIPadWithDiscount = 499.99 * 5
            val expectedTVPrice = products.getValue(ATV.sku).price * 2
            val expectedTotal = expectedIPadWithDiscount + expectedTVPrice

            assertThat(total).isEqualTo(expectedTotal)
        }

        @Test
        fun `when 4 or less purchased with other non discounted items, then no discount`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(IPD.sku)
                    .scan(ATV.sku)
                    .scan(ATV.sku)
                    .total()

            // Purchase 4 iPads which misses out on the bulk discount by 1
            val expectedIPadWithDiscount = products.getValue(IPD.sku).price * 4
            val expectedTVPrice = products.getValue(ATV.sku).price * 2
            val expectedTotal = expectedIPadWithDiscount + expectedTVPrice

            assertThat(total).isEqualTo(expectedTotal)
        }
    }

    @DisplayName("given vga adapter scenarios")
    @Nested
    inner class GivenVGAScenarios {

        @Test
        fun `when purchased with a mac, bundle the adapter for free`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(MBP.sku)
                    .scan(IPD.sku)
                    .scan(VGA.sku)
                    .total()

            // Purchase 1 macbook pro with 1 VGA which results in VGA being bundled in for free
            val expectedVGAPrice = 0.0
            val expectedMacPrice = products.getValue(MBP.sku).price
            val expectedIPadPrice = products.getValue(IPD.sku).price

            val expectedTotal = expectedVGAPrice + expectedMacPrice + expectedIPadPrice

            assertThat(total).isEqualTo(expectedTotal)
        }

        @Test
        fun `when not purchased with a mac, the adapter receives no discount`() {
            val products = listOf(
                    Product(IPD.sku, "Super iPad", 549.99),
                    Product(MBP.sku, "MacBook Pro", 1399.99),
                    Product(ATV.sku, "Apple TV", 109.50),
                    Product(VGA.sku, "VGA adaptor", 30.00)
            ).associateBy { it.sku }

            val strategy = DefaultPricingStrategy(catalogue = catalogueOf(products.values.toList()), rules = rules)
            val total = Checkout(strategy)
                    .scan(IPD.sku)
                    .scan(VGA.sku)
                    .total()

            val expectedVGAPrice = products.getValue(VGA.sku).price
            val expectedIPadPrice = products.getValue(IPD.sku).price

            val expectedTotal = expectedVGAPrice + expectedIPadPrice

            assertThat(total).isEqualTo(expectedTotal)
        }

    }
}