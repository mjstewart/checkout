Shopping cart in kotlin

# Usage

```$bash
git clone https://github.com/mjstewart/checkout.git
cd checkout
mvn clean test
```

maven project structure 

# Overview

The core of the implementation centers around 2 classes which are commented

- [DefaultPricingStrategy](https://github.com/mjstewart/checkout/blob/master/src/main/kotlin/shoppingrus/pricing/DefaultPricingStrategy.kt)
- [Rules](https://github.com/mjstewart/checkout/blob/master/src/main/kotlin/shoppingrus/pricing/Rules.kt)

# Examples 
- See tests for example usage - [Tests](https://github.com/mjstewart/checkout/tree/master/src/test/kotlin/shoppingrus)
- Alternatively, there is a main entry point - [Main.kt](https://github.com/mjstewart/checkout/blob/master/src/main/kotlin/shoppingrus/Main.kt)
 
# Requirements

kotlin 1.3+

# Testing libraries used

- junit5
- assertj