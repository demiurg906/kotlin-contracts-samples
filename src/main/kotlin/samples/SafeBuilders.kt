package samples

import org.jetbrains.kotlin.contracts.contextual.expectsTo
import org.jetbrains.kotlin.contracts.contextual.provides
import org.jetbrains.kotlin.contracts.contextual.safebuilders.CallKind
import org.jetbrains.kotlin.contracts.contextual.safebuilders.Calls
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.contracts.receiverOf

/*
 * Workflow of Safe builders contracts:
 *  There is a builder class with some setter functions and
 *   corresponding build function that takes initializer (lambda
 *   with receiver), applies it to builder and returns built object.
 *  With contracts you can check, how many times each function
 *   has been called in initializer.
 *  On setter functions you must write contract that says
 *   'call of that function provides effect that this function
 *    has been called once'
 *  In build function you must write contracts on init lambda
 *   that says 'I expected that `function` in init lambda will
 *   be called `InvocationKind` times.
 *  Note: init lambda in builder function must has contract
 *   `callsInPlace`
 *
 * Note: at this times that contracts don't work with builder
 *  instances (see [builderInstance] and Readme)
 *
 * InvocationKind: InvocationKind is enumeration that used to
 *  describe contract system how many times function should be called
 *  Available values:
 *   - EXACTLY_ONCE -- exactly one time
 *   - AT_LEAST_ONCE -- one or more times
 *   - AT_MOST_ONCE -- zero or one time
 */


data class XYZ(val x: Int, var y: Int, val z: Int = 0)

/*
 * Builder class with contracts on setters
 * Note: contracts on getters and setters are not supported yet
 */
@ExperimentalContracts
class XYZBuilder {
    private var x: Int? = null
    private var y: Int? = null
    private var z: Int? = null

    fun setValX(value: Int) {
        contract {
            provides(Calls(::setValX, this@XYZBuilder))
        }
        this.x = value
    }

    fun setVarY(value: Int) {
        contract {
            provides(Calls(::setVarY, this@XYZBuilder))
        }
        y = value
    }

    fun setDefaultValZ(value: Int) {
        contract {
            provides(Calls(::setDefaultValZ, this@XYZBuilder))
        }
        z = value
    }

    fun create(): XYZ {
        return if (z == null) {
            XYZ(x!!, y!!)
        } else {
            XYZ(x!!, y!!, z!!)
        }
    }
}

/*
 * Build function with contract
 */
@ExperimentalContracts
fun build(init : XYZBuilder.() -> Unit): XYZ {
    contract {
        callsInPlace(init, InvocationKind.EXACTLY_ONCE)
        expectsTo(init, CallKind(XYZBuilder::setValX, InvocationKind.EXACTLY_ONCE, receiverOf(init)))
        expectsTo(init, CallKind(XYZBuilder::setVarY, InvocationKind.AT_LEAST_ONCE, receiverOf(init)))
        expectsTo(init, CallKind(XYZBuilder::setDefaultValZ, InvocationKind.AT_MOST_ONCE, receiverOf(init)))
    }

    val xyzBuilder = XYZBuilder()
    xyzBuilder.init()
    return xyzBuilder.create()
}

/*
 * Samples of usage of build functions
 */
@ExperimentalContracts
fun buildersTest1(): XYZ =
    build { // OK
        setValX(10)
        setVarY(11)
        setDefaultValZ(12)
    }

@ExperimentalContracts
fun buildersTest2(): XYZ =
    build { // OK
        setValX(10)
        setVarY(11)
        setVarY(12)
    }

@ExperimentalContracts
fun buildersTest3(): XYZ =
    build { // Warning
        setDefaultValZ(11)
        setDefaultValZ(12)
    }

/**
 * Not supported yet
 */
@ExperimentalContracts
fun builderInstance(): XYZ {
    val builder = XYZBuilder()
    builder.setValX(10)
    builder.setDefaultValZ(11)
    builder.setDefaultValZ(12)
    return builder.create()
}