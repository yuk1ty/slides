package com.github.yuk1ty.contextParametersExample

interface Monoid<A> {
    fun op(lhs: A, rhs: A): A
    fun unit(): A
}

object IntMonoid : Monoid<Int> {
    override fun op(lhs: Int, rhs: Int): Int = lhs + rhs
    override fun unit(): Int = 0
}

object StringMonoid : Monoid<String> {
    override fun op(lhs: String, rhs: String): String = lhs + rhs
    override fun unit(): String = ""
}

context(monoid: Monoid<A>)
fun <A> sum(list: List<A>): A = list.fold(monoid.unit(), monoid::op)

fun callMonoid() {
    // IntMonoidのコンテクストを生成する
    with(IntMonoid) {
        val intList = listOf(1, 2, 3)
        // IntMonoidがcontextに渡される
        val intSum = sum(intList)
        println("intSum = $intSum") // 6
    }

    // StringMonoidのコンテクストを生成する
    with(StringMonoid) {
        val stringList = listOf("a", "b", "c")
        // StringMonoidがcontextに渡される
        val stringSum = sum(stringList)
        println("stringSum = $stringSum") // "abc"
    }
}