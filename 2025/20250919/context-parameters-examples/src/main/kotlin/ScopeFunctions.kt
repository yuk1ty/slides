package com.github.yuk1ty.contextParametersExample

class DatabaseContext {
    fun startTransaction(block: (Transaction) -> Unit): Unit = TODO()
}

class Transaction

fun callScopeFunctions() {
    DatabaseContext().apply {
        // DatabaseContextのコンテクスト
        startTransaction { tx ->
            // Transactionのコンテクスト
        }
    }
}
