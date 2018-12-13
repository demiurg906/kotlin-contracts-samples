package samples

import org.jetbrains.kotlin.contracts.contextual.callsIn

import org.jetbrains.kotlin.contracts.contextual.dslmarker.DslMarkers
import org.jetbrains.kotlin.contracts.contextual.requires
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.contracts.receiverOf

/*
 * DslMarker contracts is equivalent of @DSLMarker annotation.
 * (see https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-dsl-marker/index.html)
 * Usage of DslMarker contracts:
 *  - member functions of classes used in dsl must has contract
 *   `requires(DslMarkers([reference to this]))`
 *  - dsl functions with lambda must has contract
 *    `callsIn(lambda, DslMarkers(receiverOf(lambda)))`
 *    and also it must be marked as `callsInPlace`
 */

data class Foo(val x: Int, val bar: Bar)

data class Bar(val y : Int)

@ExperimentalContracts
class BarBuilder {
    private var y_: Int? = null
    fun setY(value: Int = 0) {
        contract {
            requires(DslMarkers(this@BarBuilder))
        }
        y_ = value
    }

    fun create(): Bar = Bar(y_!!)
}

@ExperimentalContracts
class FooBuilder {
    private var x_: Int? = null
    private var bar_: Bar? = null
    fun setX(value: Int = 0) {
        contract {
            requires(DslMarkers(this@FooBuilder))
        }
        x_ = value
    }

    fun buildBar(init: BarBuilder.() -> Unit) {
        contract {
            callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
            callsIn(init, DslMarkers(receiverOf(init)))
            requires(DslMarkers(this@FooBuilder))
        }
        val builder = BarBuilder()
        builder.init()
        bar_ = builder.create()
    }

    fun create() = Foo(x_!!, bar_!!)
}

@ExperimentalContracts
fun buildFoo(init: FooBuilder.() -> Unit): Foo {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        callsIn(init, DslMarkers(receiverOf(init)))
    }
    val builder = FooBuilder()
    builder.init()
    return builder.create()
}

// ---------------- TESTS ----------------

@ExperimentalContracts
fun DslMarkerTest1(): Foo =
    buildFoo { // OK
        setX()
        buildBar {
            setY()
        }
    }

@ExperimentalContracts
fun test_2(): Foo =
    buildFoo {
        buildBar {
            setX() // Warning
            setY()
        }
    }
