package com.github.yuk1ty.contextParametersExample

interface Raise<E : Exception> {
    // 今回はエラーを単に送出するだけにする
    fun raise(error: E): Nothing = throw error
}

// AppError用のコンテクストをハンドリングする関数を定義する。
inline fun <T> handle(block: context(Raise<AppError>) () -> T): T {
    return try {
        val raiseImpl = object : Raise<AppError> {}
        with(raiseImpl) { block() }
    } catch (e: Exception) {
        // 何かしらのハンドリングをする。今回は便宜のために単に呼び出し元に伝播する。
        throw e
    }
}

sealed class AppError : Exception() {
    object NegativeNumber : AppError()
}

context(raiseCtxt: Raise<AppError>)
fun validateNumber(x: Int): Int {
    if (x < 0) {
        raiseCtxt.raise(AppError.NegativeNumber)
    } else {
        return x
    }
}

fun callRaiseDSL() {
    // こちらは通常のバリデーションが通り、値が返ってくる。
    val result = handle { validateNumber(1) }
    // バリデーションの結果AppError.NegativeNumberが送出される。
    val error = handle { validateNumber(-1) }
}