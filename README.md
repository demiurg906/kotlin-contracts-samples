# kotlin-contracts-samples

This repository provides samples of using prototype of new feature of kotlin contracts system -- contextual contracts (in develop). On [wiki](https://github.com/demiurg906/kotlin-contracts-samples/wiki) you can examine detailed description of that system (in russian).

This is not final implementation and there is no guarantees that this feature will be supported in released Koltin compiler.

## Installation

For enabling contracts in IDE you should install some IDEA plugins:
1. Forked version of Kotlin plugin
    - [IDEA 2018.2](https://teamcity.jetbrains.com/repository/download/Kotlin_dev_CompilerAllPlugins/1833129:id/kotlin-plugin-1.3.30-dev-245-IJ2018.2-1.zip)
    - [IDEA 2018.3](https://teamcity.jetbrains.com/repository/download/Kotlin_dev_CompilerAllPlugins/1833129:id/kotlin-plugin-1.3.30-dev-245-IJ2018.3-1.zip)
2. Core contracts [plugin](https://teamcity.jetbrains.com/repository/download/Kotlin_dev_CompilerAllPlugins/1833129:id/kotlin-plugin-1.3.30-dev-245-IJ2018.2-1.zip!/Kotlin/lib/kotlin-contracts-plugin.jar)
3. Contracts subplugins [plugin](https://teamcity.jetbrains.com/repository/download/Kotlin_dev_CompilerAllPlugins/1833129:id/kotlin-compiler-1.3.30-dev-245.zip!/kotlinc/lib/kotlin-contracts-compiler-subplugins.jar)

For enabling contracts in compiler, you should configure your _build.gradle_ and _settings.gradle_ files as follows in sample([build.gradle](build.gradle), [settings.gradle](settings.gradle)).

## Implemented contracts

By now there is four different types of contracts are implemented:
- Checked exceptions ([wiki](https://github.com/demiurg906/kotlin/wiki/05.Implemented_cases#checked-exceptions), [samples](src/main/kotlin/samples/CheckedExceptions.kt))
- Safe builders ([wiki](https://github.com/demiurg906/kotlin/wiki/05.Implemented_cases#transactions), [samples](src/main/kotlin/samples/SafeBuilders.kt))
- Transactions ([wiki](https://github.com/demiurg906/kotlin/wiki/05.Implemented_cases#dsl-marker), [samples](src/main/kotlin/samples/Transactions.kt))
- Dsl Marker ([wiki](https://github.com/demiurg906/kotlin/wiki/05.Implemented_cases#safe-builders), [samples](src/main/kotlin/samples/DslMarker.kt))

## Actual restrictions

Contextual contracts is a prototype and it contains some restrictions (they are not described in wiki):

#### Generics

Using generic parameters in contracts is forbidden now, so you can not write contracts like that:
```kotlin
fun runCatching<T : Throwable>(block: () -> Unit) {
    contract {
        callsIn(block, CatchesException<T>())
    }
    ...
}
```

Most likely this feature will be able in near future.

#### DSL and Instance duality

Some contracts cases (_transactions_ and _safe builders_) could be used in two ways:

**DSL style:**
```kotlin
fun build(init: Builder.() -> Unit): Foo {
    val builder = Builder()
    builder.init()
    return builder.build()
}

fun foo(): Foo =
    build {
         setX(1)
         setY(2)
    }
```

**Instance style:**
```kotlin
fun foo(): Foo {
    val builder = Builder()
    builder.setX(1)
    builder.setY(2)
    return builder.build()
}
```

At this moment is allowed:
- _DSL style_ for _safe builders_ contracts
- _Instance style_ for _transactions_ contracts

In near future both styles will be allowed for both types of contracts, but with one restriction: you can use only one style and can not mix them.

#### Usage of functions with extension lambdas

Usage of functions with extension lambdas (like `with`) in _Instance style_ is not supported, so for correct work do not use that functions.

**Example of bad code:**
```kotlin
fun foo() {
    val transaction = Transaction()
    transaction.open()
    with (transaction) {
        commit()
    }
}
```
