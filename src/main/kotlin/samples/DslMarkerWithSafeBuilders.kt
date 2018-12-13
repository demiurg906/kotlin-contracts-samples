package samples

import org.jetbrains.kotlin.contracts.contextual.callsIn

import org.jetbrains.kotlin.contracts.contextual.dslmarker.DslMarkers
import org.jetbrains.kotlin.contracts.contextual.expectsTo
import org.jetbrains.kotlin.contracts.contextual.provides
import org.jetbrains.kotlin.contracts.contextual.requires
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallKind
import org.jetbrains.kotlin.contracts.contextual.safebuilders.Calls
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.contracts.receiverOf

data class Fooz(val x: Int, val bar: Baz)

data class Baz(val y : Int)

@ExperimentalContracts
class BazBuilder {
    private var y_: Int? = null
    fun setY(value: Int = 0) {
        contract {
            requires(DslMarkers(this@BazBuilder))
            provides(Calls(::setY, this@BazBuilder))
        }
        y_ = value
    }

    fun create(): Baz = Baz(y_!!)
}

@ExperimentalContracts
class FoozBuilder {
    private var x_: Int? = null
    private var baz_: Baz? = null
    fun setX(value: Int = 0) {
        contract {
            requires(DslMarkers(this@FoozBuilder))
            provides(Calls(::setX, this@FoozBuilder))
        }
        x_ = value
    }

    fun buildBaz(init: BazBuilder.() -> Unit) {
        contract {
            callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
            callsIn(init, DslMarkers(receiverOf(init)))
            expectsTo(init, CallKind(BazBuilder::setY, InvocationKind.EXACTLY_ONCE, receiverOf(init)))
            requires(DslMarkers(this@FoozBuilder))
            provides(Calls(::buildBaz, this@FoozBuilder))
        }
        val builder = BazBuilder()
        builder.init()
        baz_ = builder.create()
    }

    fun create() = Fooz(x_!!, baz_!!)
}

@ExperimentalContracts
fun buildFooz(init: FoozBuilder.() -> Unit): Fooz {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        callsIn(init, DslMarkers(receiverOf(init)))
        expectsTo(init, CallKind(FoozBuilder::setX, InvocationKind.EXACTLY_ONCE, receiverOf(init)))
        expectsTo(init, CallKind(FoozBuilder::buildBaz, InvocationKind.EXACTLY_ONCE, receiverOf(init)))
    }
    val builder = FoozBuilder()
    builder.init()
    return builder.create()
}

// ---------------- TESTS ----------------

@ExperimentalContracts
fun SBDslMarkerTest1(): Fooz =
    buildFooz { // OK
        setX()
        buildBaz {
            setY()
        }
    }