package samples

import org.jetbrains.kotlin.contracts.contextual.closes
import org.jetbrains.kotlin.contracts.contextual.requires
import org.jetbrains.kotlin.contracts.contextual.starts
import org.jetbrains.kotlin.contracts.contextual.transactions.OpenedTransaction
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/*
 * Transaction contracts workflow:
 *   there is class with methods that:
 *    - opens transaction
 *    - does some work with transaction
 *    - closes transactions
 *   Rules that contracts checks:
 *    - you can do actions only with opened transaction
 *    - you can close only opened transaction
 *    - you can not open already opened transaction
 *  Note: after closing transaction you can reuse it again
 *  Note: aliasing is not supported yet (see [aliasingTest])
 *  Note: using contracts on transaction dsl (like builders)
 *    will be released soon (see Readme)
 *  IMPORTANT: don't use transactions contract with extension lambdas,
 *    it crashes compiler (will be fixed very soon)
 *    (see [transactionWith] and Readme)
 */
@ExperimentalContracts
class Transaction {
    fun start() {
        contract {
            starts(OpenedTransaction(this@Transaction))
        }
        // open transaction
    }

    fun setData() {
        contract {
            requires(OpenedTransaction(this@Transaction))
        }
        // something useful
    }

    fun commit() {
        contract {
            closes(OpenedTransaction(this@Transaction))
        }
        // commit transaction
    }
}

// ---------------- TESTS ----------------

@ExperimentalContracts
fun transactionTest1() {
    val transaction = Transaction()

    // OK
    transaction.start()
    transaction.setData()
    transaction.commit()
}

@ExperimentalContracts
fun transactionTest2() {
    val transaction = Transaction()

    transaction.setData() // Warning: transaction is not opened
    transaction.commit() // Warning: transaction is not opened
}

@ExperimentalContracts
fun transactionTest3() {
    val transaction = Transaction()

    transaction.start()
    transaction.commit()
    transaction.setData() // Warning: transaction is not opened
}

@ExperimentalContracts
fun transactionTest4() {
    val transaction = Transaction()
    transaction.commit() // Warning: transaction is not opened
}

@ExperimentalContracts
fun transactionTest5() {
    val transaction = Transaction() // Warning: transaction transaction must be closed
    transaction.start()
}

@ExperimentalContracts
fun transactionTest6() {
    val transaction = Transaction()

    transaction.start()
    transaction.start() // Warning: transaction transaction already started
    transaction.commit()
}

@ExperimentalContracts
fun reuseTransactionTest() {
    val transaction = Transaction()

    transaction.start()
    transaction.commit()
    // OK
    transaction.start()
    transaction.commit()
}

@ExperimentalContracts
fun aliasingTest() {
    /*
     * Contracts can analyze only effects connected
     *   to direct variable
     */
    val transaction1 = Transaction()
    transaction1.start()
    val transaction2 = transaction1
    transaction2.commit() // Warning
}

@ExperimentalContracts
fun transactionWith() {
    val transaction = Transaction()

    transaction.start()
    // DON'T DO THAT!
//    with (transaction) {
//        commit()
//    }
    transaction.commit()
}