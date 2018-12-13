package samples

import org.jetbrains.kotlin.contracts.contextual.callsIn
import org.jetbrains.kotlin.contracts.contextual.exceptions.CatchesException
import org.jetbrains.kotlin.contracts.contextual.requires
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/*
 * Declaration of function that throws an exception
 */
@ExperimentalContracts
fun throwsFileNotFoundException() {
    contract {
        requires(CatchesException<FileNotFoundException>())
    }
    throw FileNotFoundException()
}

/*
 * Declaration of function that catches Exception
 * Note: there is must be `callsInPlace` contract
 *   on lambda that run in `try` block must
 * Note: contracts with generics are not supported yet
 *   but most likely will be in future
 */
@ExperimentalContracts
inline fun myCatchIOException(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        callsIn(block, CatchesException<IOException>())
    }
    try {
        block()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

/*
 * Example of usage functions with exception contracts
 */
@ExperimentalContracts
fun foo() {
    throwsFileNotFoundException() // Warning

    myCatchIOException {
        throwsFileNotFoundException() // OK
    }
}


@ExperimentalContracts
fun alsoThrowsException() {
    contract {
        requires(CatchesException<FileNotFoundException>())
    }
    throwsFileNotFoundException()
}