---
marp: true
theme: rose-pine-custom
paginate: true
---

<!-- _paginate: skip -->

# カジュアルコントリビュータと学ぶRustコンパイラ

2025/07/29
Rustの現場に学ぶ 〜Webアプリの裏側からOS、人工衛星まで〜

---

## 目次

- 前提の整理
- カジュアルコントリビュータ？
- Rustコンパイラの構成と特徴
- Rustコンパイラ開発の個人的体験

---

## 前提の整理

- コンパイラに関するある程度の知識は既知とさせてください🙇🏻‍♀️ 説明しているとさすがに15分〜20分では扱いきれないためです。
  - 字句解析（レキサ）、構文解析（パーサー）、抽象構文木あたりのイメージがつけば大丈夫…だと思う。
  - 昔記事を書いたらしい（6年前って書いてあってびっくりしてる）。
    - Rust LT#2 で話をしました & その話の詳細な解説: https://blog-dry.com/entry/2018/08/03/184806
    
---

## 前提の整理

- 尺の都合で実装レベルの話までは踏み込めません。
- 懇親会でお話ししましょう…！私も初心者ですが。
    
---

## カジュアルコントリビュータ？
### 定義

- 私が勝手にそう呼んでいるだけで、一般的な定義はない。
- 時折現れてIssueを解決していく人。
- ガチ勢ではない（ので、「カジュアル」）が、多少はコンパイラに詳しいだろうということで今日話をする。

---

## カジュアルコントリビュータ？
### 動機

- 転職してRustエンジニアではなくなったので、Rustを触る機会が激減した。
- 前からやってみたかったRust本体へのコントリビューションをしてみることに。
  - 後述するようにAIの力のおかげで、かなりコントリビュートしやすくなった。

---

## Rustコンパイラの構成と特徴

- **全体的な構成**: よくあるコンパイラの構成。ASTを構築後、中間表現に変換し、コード生成を経てバイナリが作られる。
- **特徴的な設計**: クエリベースのコンパイルが走る。丁寧なエラーメッセージ、トレイトリゾルバあたりはRust特有かも。

---

## Rustコンパイラの構成と特徴
### 全体的な構成

Rustコンパイラ内では、大まかには次のように「中間表現（IR）」が変換されていく。

- AST
- HIR
- MIR
- LLVM IR (Codegen)

上のフェーズから下のフェーズに中間表現が直されていくことを「**lowering**」と呼ぶ。

---

## Rustコンパイラの構成と特徴
### 全体的な構成: AST

- ソースコードを解析（字句、構文）し、抽象構文木（Abstract Syntax Tree; AST）に直すフェーズ。
- ちなみにRustは、再帰下降構文解析で、パーサーを手で実装している。

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

- HIR (High-level Intermediate Representation) と呼ばれる中間表現がまずは生成される。その後THIR (Typed High-level Intermediate Representation)が生成され、次のフェーズ（MIR）に渡される。
- 主には脱糖（desugar）を担う。たとえば、Rustの文法上現れる下記の文法機能は、コンパイラ内部では違う形で扱われる。
  - for式やwhile式は、すべてmatch + loopに変換される。
  - asyncやawaitは、コンパイラ内部表現としてコルーチンに変換される。

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

- そのほか、型に関するさまざまな解析がかけられる。
  - 型推論、トレイトと実装の紐付け、型チェックはHIRへの変換のタイミングで行われる。
  - パターンマッチ時に現れる網羅性チェックは、THIRへの変換のタイミングで行われる。

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

（コードからHIRを出力してみた例を出す）

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

HIRは、次のcargoコマンドで確認できる。

```
cargo rustc -- -Z unpretty=hir-tree
cargo rustc -- -Z unpretty=hir
cargo rustc -- -Z unpretty=thir-tree
```

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

試しに下記のようなコードをHIRとして出力させてみる。

```rust
fn main() {
    let nums = vec![1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    let mut ans = 0;
    for num in nums {
        ans += num;
    }
    assert_eq!(ans, 55);
}
```

---

## Rustコンパイラの構成と特徴
### 全体的な構成: HIR

本来のHIR自体はこのようにASTに近しい形だが、少し読みにくいのでRustコードに近い形式で出力させる。

```
DefId(0:0 ~ for_hir[8e24]) => OwnerNodes {
    node: ParentedNode {
        parent: 4294967040,
        node: Crate(
            Mod {
                spans: ModSpans {
                    inner_span: src/bin/for_hir.rs:1:1: 8:2 (#0),
                    inject_use_span: no-location (#0),
                },
                item_ids: [
                    ItemId {
                        owner_id: DefId(0:1 ~ for_hir[8e24]::{use#0}),
                    },
                    ItemId {
                        owner_id: DefId(0:2 ~ for_hir[8e24]::std),
                    },

```

---

すると、for式がmatch, loop, matchの形式で変換されていることを確認できる。

```rust
// ...
fn main() {
    let nums =
        <[_]>::into_vec(::alloc::boxed::box_new([1, 2, 3, 4, 5, 6, 7, 8, 9,
                        10]));
    let mut ans = 0;
    {
        let _t =
            match #[lang = "into_iter"](nums) {
                mut iter =>
                    loop {
                        match #[lang = "next"](&mut iter) {
                            #[lang = "None"] {} => break,
                            #[lang = "Some"] {  0: num } => { ans += num; }
                        }
                    },
            };
        _t
    };
    match (&ans, &55) {
        (left_val, right_val) => {
            if !(*left_val == *right_val) {
                let kind = ::core::panicking::AssertKind::Eq;
                ::core::panicking::assert_failed(kind, &*left_val,
                    &*right_val, ::core::option::Option::None);
            }
        }
    };
}
```

---

## Rustコンパイラの構成と特徴
### 全体的な構成: MIR

- THIRからMIR（Mid-level Intermediate Representation）が生成される。
- MIRはRustコンパイラの根幹をなす中間表現である。主には制御フローグラフに基づいた中間表現になっており、型付けもなされた状態である。
- たとえば下記が含まれる。
  - ボローチェック（Borrow Check）。
  - 単相化（Monomorphization）。

---

## Rustコンパイラの構成と特徴
### 全体的な構成: MIR

MIRは、次のcargoコマンドで出力できる。

```
cargo rustc -- -Z unpretty=mir 
```

---

## Rustコンパイラの構成と特徴
### 全体的な構成: MIR


---

## Rustコンパイラの構成と特徴
### 全体的な構成: MIR

HIRで使用したのと同じコードを使ってMIRを出力させてみると、次のような出力を見ることができる。

```
fn main() -> () {
    let mut _0: ();
    let _1: std::vec::Vec<i32>;
// ...
    bb1: {
        _6 = ShallowInitBox(move _5, [i32; 10]);
        _26 = copy ((_6.0: std::ptr::Unique<[i32; 10]>).0: std::ptr::NonNull<[i32; 10]>) as *const [i32; 10] (Transmute);
        _27 = copy _26 as *const () (PtrToPtr);
        _28 = copy _27 as usize (Transmute);
        _29 = AlignOf([i32; 10]);
        _30 = Sub(copy _29, const 1_usize);
        _31 = BitAnd(copy _28, copy _30);
        _32 = Eq(copy _31, const 0_usize);
        assert(copy _32, "misaligned pointer dereference: address must be a multiple of {} but is {}", copy _29, copy _28) -> [success: bb15, unwind unreachable];
    }
```

---

## Rustコンパイラの構成と特徴
### 全体的な構成: MIR

ポイントは下記の通り。

- `bbX`, `scope X`のような記述
- `move`や`copy`、`drop`など、ボローチェッカーに関連する記述を確認できる。
- `return`や`goto`、`unreachable`など、制御フローに関係しそうなものを確認できる。

---

## Rustコンパイラの構成と特徴
### bbX, scope X

- `scope`: 軸解析後のソースコードのスコープ構造を表現する。

```
            scope 3 {
                debug iter => _9;
                let _13: i32;
                scope 4 {
                    debug num => _13;
                }
            }

```

- `bb`: Basic Blocksの略で、制御フローグラフの一単位を示す。（`_3`などは、MIRの一番上で定義されている変数情報を指している）

```
    bb0: {
        _3 = SizeOf([i32; 10]);
        _4 = AlignOf([i32; 10]);
        _5 = alloc::alloc::exchange_malloc(move _3, move _4) -> [return: bb1, unwind continue];
    }
```

---

## Rustコンパイラの構成と特徴
### move, copy, drop

- その値がmoveするのか、copyするのか、そこでdropするのかを示している。

```
    bb3: {
        _9 = move _8;
        goto -> bb4;
    }
```

---

## Rustコンパイラの構成と特徴
### 実際の中間表現を少し読んでみる

（時間があれば。なければ飛ばす予定です。）

---

## Rustコンパイラの構成と特徴
### 全体的な構成: LLVM IR（Codegen）

- MIRからLLVM IRが生成される。バイナリが生成される直前のフェーズであると言える。
- バイナリの生成までにはさらにいくつかのステップを経るが、詳しくは下記の記事を参考にできる。
  - Resolving Rust Symbols: https://blog.shrirambalaji.com/posts/resolving-rust-symbols/
  - ちなみに、この記事の著者は昨年Rust.Tokyoに登壇してくれた。

---
