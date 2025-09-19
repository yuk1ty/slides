---
marp: true
theme: rose-pine
paginate: true
---

<!-- _paginate: skip -->

# Kotlinの「コンテクスト指向プログラミング（Context Oriented Programming）」

2025/09/19
Nextbeat Tech Bar：第七回関数型プログラミング（仮）の会

---

## 目次

- 自己紹介
- Kotlinにおける高階関数と拡張関数
- Kotlinの高階関数はコンテクストを作る
- Context Oriented Programming (COP)
- Context ParametersによるCOPの拡張
- 関数型プログラミングからの影響
- まとめ

---

## 自己紹介

- yuki (X: @helloyuki\_)
- 所属: ソフトウェアアーキテクト @ Sansan株式会社
- rust-lang/rustのコントリビュータやRust.Tokyoなど
- 以前はScalaエンジニアで、今はKotlinエンジニアをしている

---

## 免責事項

- 5分で細かく解説はできません！ごめんなさい。
- 代わりに細かい解説を含む記事を書きました。この資料を手がかりにしつつも、気になる点があれば記事をご覧ください。
  - Kotlinの「コンテクスト指向プログラミング」とは何か？: https://blog-dry.com/entry/2025/09/19/132623

---

## Kotlinにおける高階関数と拡張関数

Kotlinには高階関数がある。

```kotlin
fun foo(action: Actinable -> Unit): Unit
```

---

## Kotlinにおける高階関数と拡張関数

また、拡張関数もある。特定の型に対して実装をあとから継ぎ足しできるイメージの機能。`<レシーバ>.関数名()`で実装する。

```kotlin
// 拡張関数
fun Extendable.foo(): Unit

// メンバー内拡張関数
interface SomeScope {
    fun Extendable.foo(): Unit
}
```

---

## Kotlinの高階関数はコンテクストを作る

高階関数はブロックとして記述することができる。これが「コンテクスト」を作る。

```kotlin
class DatabaseContext {
    fun startTransaction(block: (Transaction) -> Unit): Unit = TODO()
}

class Transaction

fun callScopeFunctions() {
    // applyはKotlinのスコープ関数と呼ばれる関数のひとつ
    // fun <T> T.apply(block: T.() -> Unit): T
    DatabaseContext().apply {
        // DatabaseContextのコンテクスト
        startTransaction { tx ->
            // Transactionのコンテクスト
        }
    }
}
```

---

## Context Oriented Programming

- 高階関数の作るコンテクストと拡張関数とを組み合わせると、コンテクストに応じて利用できる関数が切り替わる実装をできる。
- KotlinではこれをContext Oriented Programmingと呼ぶ。
  - An introduction to context-oriented programming in Kotlin: https://proandroiddev.com/an-introduction-context-oriented-programming-in-kotlin-2e79d316b0a2
- Kotlinのコルーチンはこれを使ってよくデザインされた代表例だと思う。

---

## CoroutineScope

- kotlinx.coroutinesというライブラリに入っている機能。
- `coroutineScope`という関数が`CoroutineScope`のコンテクストを生成し、その中であればたとえば`async`などの関数を呼び出せる。
- この実装にはまさに、高階関数と拡張関数が利用されている。

---

## CoroutineScope

`coroutineScope`関数はブロックとしてCoroutineScopeのレシーバ付き関数リテラルを受け取る。`async`関数は`CoroutineScope`の拡張関数になっている。

```kotlin
// coroutineScope関数
suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R

// async関数
fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T
): Deferred<T>
```

---

## CoroutineScope

これらを組み合わせると、`coroutineScope`のコンテクスト内で`async`関数を呼び出せる。

```kotlin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

suspend fun callCoroutine() {
    coroutineScope {
        // async関数はCoroutinesScopeのコンテクスト内でのみ呼び出せる。
        val firstTask = async { highLoadFunction() }
        val secondTask = async { highLoadFunction() }
        // Continues...
    }
}
```

---

## Context ParametersによるCOPの拡張

- さらにKotlin 2.2.0になって、コンテクスト情報を伝播させられるContext Parametersという実験的な機能が実装された。
- Scalaのimplicitsによく似ている。

---

## Context ParametersによるCOPの拡張

関数に`context(ctx: Context)`を追加すると、裏で自動でコンテクストを伝播してくれる。一方で、コンテクストがきちんと切り出されていない箇所で呼び出されるとコンパイルエラーで検知される。

```kotlin
context(ctx: Context)
fun delegateContext() = ctx.call()

fun main() {
    with(Context) {
        // これは問題ない
        delegateContext()
    }

    // コンテクストの切り出されていない箇所で呼び出すとコンパイルエラーになる
    delegateContext()
}
```

---

## Context ParametersによるCOPの拡張

- これを利用すると、たとえば下記を実装できる。
  - いわゆるモノイド。
  - 型つきエラーハンドリング（Raise DSL）。
- Raise DSLを今回は紹介する。

---

## 型つきエラーハンドリング（Raise DSL）

```kotlin
// 今回のコンテクスト
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
```

---

## 型つきエラーハンドリング（Raise DSL）

```kotlin
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
```

---

## 関数型プログラミングからの影響

- Context ParametersのDesign Docでも言及があるが、いわゆる型クラスに近い概念と言える？
- また、コンパイル時にコンテクストが十分かどうかを検査することから、Coeffects的だと言える。
- Raise DSLはAlgebraic Effectに似ている。

など。

---

## まとめ

- KotlinにはContext Oriented Programmingという考え方がある。
- これは結構関数型プログラミングで議論されるコンセプトの影響を受けている。

---

## 参考文献

- KEEP-0259: https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0259-context-receivers.md
- KEEP-0367: https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0367-context-parameters.md
- Functional Error Handling in Kotlin: Part 3 - The Raise DSL: https://rockthejvm.com/articles/functional-error-handling-in-kotlin-part-3-the-raise-dsl
