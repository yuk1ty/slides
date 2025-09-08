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

- 自己紹介
- 前提の整理
- カジュアルコントリビュータ？
- Rustコンパイラの構成と特徴
- Rustコンパイラ開発の個人的体験

---

## 自己紹介

- yuki (@helloyuki\_)
- Sansan株式会社のBill Oneというプロダクトでアーキテクト。
- 普段は海外チームと働いています。
- Rust.Tokyoのオーガナイザー。

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

### 全体的な構成: LLVM IR（Codegen）

- MIRからLLVM IRが生成される。バイナリが生成される直前のフェーズであると言える。
- バイナリの生成までにはさらにいくつかのステップを経るが、詳しくは下記の記事を参考にできる。
  - Resolving Rust Symbols: https://blog.shrirambalaji.com/posts/resolving-rust-symbols/
  - ちなみに、この記事の著者は昨年Rust.Tokyoに登壇してくれた。

---

## Rustコンパイラの構成と特徴

### 特徴的な設計

- クエリシステム（Query）
- エラーメッセージ（Diagnostics; 診断）

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > クエリシステム: クエリシステムとは

- 一義的には、あるキーに対するクエリがあった際に、その結果を返す。結果はキャッシュされ、2回目以降の問い合わせではキャッシュの結果を使用する、というもの。
- アナロジー的には、Rustコンパイラの各モジュールの知識はいわゆるデータベースのようになっていて、そのデータベースに対してクエリをかけ、結果を取得する。
  - "データベース"なので、コンパイル結果の多くはディスク上に保存されている。もう一度コンパイラを立ち上げた際は、ディスクからデータを読み取って活用する。
  - クエリは別のクエリを呼び出すこともある。これがDAGを構築している。一度通った経路がキャッシュされるイメージ。
  - これがRustにおけるインクリメンタルコンパイルを実現している。
- コンパイラを開発してるとわりとどこでも見る。私も使ったことがある。

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > クエリシステム: `TyCtxt<'tcx>`

- （TODO: 結果がここに集約されるはず？）

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > クエリシステム: クエリの定義

内部的には `rustc_queries!` というマクロで定義されている。

```rust
rustc_queries! {
    /// Records the type of every item.
    query type_of(key: DefId) -> Ty<'tcx> {
        cache_on_disk_if { key.is_local() }
        desc { |tcx| "computing the type of `{}`", tcx.def_path_str(key) }
    }
    ...
}
```

あとは先ほどの`TypCtxt<'tcx>`経由で呼び出せる。

```rust
fn foo(tcx: TyCtxt<'_>, def_id: DefId) {
  let ident = tcx.type_of(def_id).instantiate_identity();
}
```

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > エラーメッセージ

- Rustのエラーメッセージは丁寧と話題だが、その裏側は意外と泥臭い。
- 何か魔法があるわけではなく、さまざまなケースできちんとエラーメッセージを実装して出力している。
- 条件分岐等々を網羅しながら書いているので、意外にエラーメッセージに不具合や誤り、細かい実装ミスが発見される。

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > エラーメッセージ: `DiagCtxt`

- クエリシステムと似たように、コンパイル時のエラー（診断; Diagnosticsという）については`rust_errors::DiagCtxt`というコンテクストに保有される。
  - 実装中は`rust_errors::DiagCtxtHandle<'a>`を経由して呼び出すと思う。
- 実装時はこれに診断内容を送るイメージ。

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > エラーメッセージの管理

- エラーメッセージの管理自体は、コンパイラの各クレートに存在する`message.ftl`というファイルで行われている。キーと値が詰まった形式になっている。
- たとえばstatic mutable referenceを作ることはできない、というコンパイルエラーに関するメッセージの定義。

````
lint_static_mut_refs_lint = creating a {$shared_label}reference to mutable static
    .label = {$shared_label}reference to mutable static
    .suggestion = use `&raw const` instead to create a raw pointer
    .suggestion_mut = use `&raw mut` instead to create a raw pointer```
````

---

## Rustコンパイラの構成と特徴

### 特徴的な設計 > エラーメッセージの管理

- 呼び出しはアトリビュートにどのエラーメッセージを呼び出すかの情報を記述して行われる。

```rust
#[derive(LintDiagnostic)]
#[diag(lint_static_mut_refs_lint)]
pub(crate) struct RefOfMutStatic<'a> {
    #[label]
    pub span: Span,
    #[subdiagnostic]
    pub sugg: Option<MutRefSugg>,
    pub shared_label: &'a str,
    #[note(lint_shared_note)]
    pub shared_note: bool,
    #[note(lint_mut_note)]
    pub mut_note: bool,
}
```

---

## Rustコンパイラ開発の個人的体験

- 前提として必要そうな知識など
- 楽しい点と苦労している点
- AIの活用
- プロジェクトへの印象など
- 将来への展望

---

## Rustコンパイラ開発の個人的体験

### 前提として必要そうな知識など

- Rustそれ自体への理解は必要。Issueを見て、「ああ、あの機能ね」とあたりがつくのが大事。コントリビューションを開始する前にまずRustを学ぶのは必須だと思う。
- コンパイラへの基礎的な理解は必要。コンパイラの教科書を一冊軽く読んでおくのをおすすめはする。ただ、公開されている開発者向けガイドで結構追いつけるといえば追いつける。
- 英語でのコミュニケーション。今はLLMを使ってなんとかなるので、最悪大丈夫。でも、そもそも英語を使えれば心理的障壁なく、臆することなく普段のPull Request上でのノリで会話できるので、できるようになっておくにこしたことはないと思っている。

---

## Rustコンパイラ開発の個人的体験

### 楽しい点と苦労している点

- 楽しい点
  - Rustに貢献できる点。自分の名前がコントリビュータリストに載る。
  - なんとなく使っていた文法機能を深く理解できることが多い。
  - Rustを書ける。

---

## Rustコンパイラ開発の個人的体験

### 楽しい点と苦労している点

- 苦労している点
  - 子持ちかつ休日もワンオペが多いタイプの人間の場合、単に時間の捻出がかなり難しい。気合いで時間を見つけてる。
  - コードベースが巨大すぎて普段使ってるツールだと固まる。NeovimでがんばってるがさすがにVSCodeの方が動作が速い😂
  - CIは長い。1回睡眠が挟まるレベル。

---

## Rustコンパイラ開発の個人的体験

### AIの活用

- どちらかというと読む方にたくさん使っている。DeepWikiはとくに便利かも。
- 書かせる方はなんか難しい。コードベースが巨大すぎるのか？で、わりと頓珍漢な回答が返ってくることも多い。実装のアイデアの相談には使える。
- AIがなかったら人に聞かないといけなかったので、聞くのが億劫で正直コントリビュートできてなかったかも。

---

## Rustコンパイラ開発の個人的体験

### プロジェクトへの印象など

- コードが巨大かつ、めちゃくちゃ綺麗に整理されているかというとそうでもない。
- 強いリーダーシップやイニシアチブが求められていそうなフェーズに見える。
  - コンパイルスピードの改善とかは、正直解決できないことはないと思うけど、全体を把握している人が少ない＆いろんなところへの影響を考慮しないといけないなどなどで重い腰が上がらないのはわかる。
  - 誰かが強い意志を持って主導しないと解決しない問題だと思う。

---

## Rustコンパイラ開発の個人的体験

### 将来への展望

- 引き続き時間があるときにやるスタンスで行きたい。
- 将来的にはasync/awaitの改善周りのプロジェクトに参加したい。
- Rustをどうこうしたいという情熱を保ち続けるのが大事そう❤️‍🔥
