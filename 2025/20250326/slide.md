---
marp: true
theme: rose-pine-custom
paginate: true
---

<!-- _paginate: skip -->

# 2025 年の Rust はどこに向かっているのか？

2025/03/26
Rust 開発最前線 - yuki さんと 2025 年の最新トレンドを学ぶ @ Findy

---

## 目次

1. 自己紹介
2. 昨今の Rust の動き
3. Project Goals とは何か？
4. 2025H1 の Flagship Goals を知る
5. その他気になった Project の紹介

---

## 自己紹介

- Yuki Toyoda (@helloyuki\_)
- Rust.Tokyo Organizer
- 某 SaaS 系企業にてソフトウェアアーキテクトを務めている。
- 執筆
  - 『Rust による Web アプリケーション開発』
  - 『実践 Rust プログラミング入門』

---

## 昨今の Rust の動き

- Rust 1.85.0 がリリースされた。現在の Rust は 2024 Edition となった。
- Rust の開発は 2025H1 の Project Goals にしたがって動いている。

---

## 昨今の Rust の動き

### 2024 Edition

- いくつかの便利な機能が入った。

---

## Project Goals とは何か？

---

## Project Goals とは何か？

- 日本語での解説は全く見ないが、近年の Rust のプロジェクトマネジメントでは「Project Goals」という仕組みが導入・運用されている。
- Project Goals は半年ごとの開発目標を定めたもの。
- 2024 年ごろからはじまった仕組みで、現在は 2025H1 が実行されている。

---

## Project Goals とは何か？

### Project Goals のメリット

- Project Goals に認定されると、オーナーと Rust チームとの関係性が確約される。
- オーナーは Rust チームから下記の支援を受けることができる。
  - Project Goals のためのリソースの確保が行われる。
  - 大規模な影響が出そうな機能の実装時に、Rust チームに意見を求めることができる。
  - Rust チームからのレビューやディスカッションなどのサポートを受けられる。

---

## Project Goals とは何か？

### Flagship Goals

- 半年に定められる目標にはいくつか種類がある。
  - Flagship Goals: 主要な目標と呼ばれるもので、Project Goals の中で特に強調される。主要な目標として制定されるためには、次の要素を持っていなければならないとされる。
    1. 何が良くなるのか具体的にわかりやすいこと。
    2. Rust ユーザーの大多数に対して大きな影響を与えること。
    3. 時間がかかるものであること。
  - それ以外: もちろん Flagship Goals 以外にもさまざまなゴールが設定されている。

---

## Project Goals とは何か？

### 2024H2 の目標について

2024H2 の目標については、次のように定められていた。

- Rust をよりもっとシンプルにすること。Rust 2024 Edition のリリース。
- Async Rust のユーザー体験をもっと改善すること。
- 低レベルなシステム向けに安全な抽象機構を提供すること。

---

## Project Goals とは何か？

### Project Goals はどのように収集されているのか？

- 毎回アンケートが実施されており、その中から Rust チームの助けが必要そうなものがピックアップされる。
- Inside Rust というアナウンスメントの中で募集があるので、H2 に何か提出したい方はお忘れなきよう。
  - "Rust Project Goals Submission Period": https://blog.rust-lang.org/inside-rust/2024/05/07/announcing-project-goals.html

---

## Project Goals とは何か？

### ここまでのまとめ

- Project Goals が定められ始めたことにより、Rust のコアチームが進んでいる先や、やっていることを外部からも確認できるようになった。
- Project Goals は半年単位で制定される。
- Project Goals はボトムアップな仕組みであり、我々であっても参加できる。採択されるかどうかは、そのテーマが「Rust 全体にとって重要か」に左右される。

---

## Project Goals とは何か？

### ここまでの参考資料

- 「Nicholas Matsakis (Co-Lead, Rust Design Team): "Rust Roadmap 2.0” | KEYNOTE | RustConf 2024」: https://youtu.be/7YjomcXNvTk?si=1ol4RvHw42OJRj5t
- Motivation: https://rust-lang.github.io/rust-project-goals/about/motivation.html
- Flagship Goals: https://rust-lang.github.io/rust-project-goals/about/flagship_goals.html
- 「Rust Project Goals Submission Period」: https://blog.rust-lang.org/inside-rust/2024/05/07/announcing-project-goals.html

---

## 2025H1 の Flagship Goals を知る

---

## 2025H1 の Flagship Goals を知る

現在走っている「2025H1」の Project Goals のうち、Flagship Goals を紹介する。下記 3 つが挙げられている。

- ネットワーキングシステムで Rust をもっと簡単に使えるようにする: Async Rust の改善
- 低レイヤーのプロジェクトでもっとサポートする: Rust for Linux への取り組み
- Rust メンテナ同士の対面交流の不足に対応する: Rust の 10 周年パーティー

https://rust-lang.github.io/rust-project-goals/2025h1/index.html

---

## 2025H1 の Flagship Goals を知る

### Async Rust の改善

- Rust は Web バックエンド開発で急速に人気を伸ばしている。
- しかし、Async Rust は「ハードモードだ」と評される。
- こうした課題について、いくつかの面から対処する。

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### Async Rust の課題感

- 同期 Rust とは違う振る舞いをする機能が残ってしまっている。ただし、2024 Edition である程度改善されている。
  - ✅ トレイト周り
  - ✅ クロージャー周り
  - ❌ Drop 処理

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### Async Rust の課題感

- ユーザーが非同期ランタイムを選ぶのが大変。
  - async-std で動かしていて reqwest を入れたら「tokio を入れて」とコンパイラに怒られる。
  - しかし、Rust 初級者のうちは「tokio って何？」「そもそもなんで動かないの？」となる。
  - どの非同期ランタイムを選んだらいいか、ユーザーを迷わせている。

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### デバッグ困難性

- ユーザーががんばってミスの原因を予想して突き止めなければならない類のバグが発生することがある。
  - Future のキャンセル安全性
  - デッドロックに関連するもの
  - まだまだある。

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### Status quo stories

- ユーザーが具体的にどのような課題を抱えているかを、実体験ベースで収集する「Status quo stories」というページがある。
  - status quo = 現状
  - ここに、Async Rust を使っていてどういう辛い体験をしたかがレポートされている。
  - 実体験ベースで名前だけ少し改変されて書かれている。
  - 読んでいてハマりどころが多いことがよくわかる。

https://rust-lang.github.io/wg-async/vision/submitted_stories/status_quo.html

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### 2025H1 で取り組まれること

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### ここまでのまとめ

- Async Rust に課題が多く、それを解消すると多くのユーザーが喜ぶため、Project Goals として設定されている。
- 主には同期 Rust に使い心地を揃えることを目指しつつ、一部機能の難しさやランタイム乱立問題などに今後取り組んでいくものと思われる。

---

## 2025H1 の Flagship Goals を知る > Async Rust の改善

### ここまでの参考資料

- 「Bring the Async Rust experience closer to parity with sync Rust」: https://rust-lang.github.io/rust-project-goals/2025h1/async.html
- Status quo stories: https://rust-lang.github.io/wg-async/vision/submitted_stories/status_quo.html
- Async Drop 問題: https://rust-lang.github.io/wg-async/vision/submitted_stories/status_quo/alan_finds_database_drops_hard.html
- キャンセル安全性問題: https://rust-lang.github.io/wg-async/vision/submitted_stories/status_quo/barbara_gets_burned_by_select.html

---

## 2025H1 の Flagship Goals を知る

### Rust for Linux

- Rust for Linux (RFL) は最も注目の集まるプロジェクトと言っても過言ではない。
- ただし、最近も既存の開発者との反りが合わず推進者が辞めるなど、既存の開発チームとの折り合いに苦労しているように見受けられる。
- Misreading Chat で最近このプロジェクトを調査した論文が紹介されており、RFL の現状にキャッチアップできるのでおすすめ。

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux の課題感

- 現在 Nightly Rust でしかビルドできない。が、実験状態を抜け出すためには Stable Rust でビルドできなければならない。
- 安定化されていない (unstable) コンパイラフラグなどを多数使用しているため。
- 安定化されていない数々の機能を安定化する (stable) 必要がある。
  - 何を安定化しなければならないかはここにまとまっている: https://github.com/Rust-for-Linux/linux/issues/2

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux のモチベーション

- Rust で書いていくことで、カーネル開発を「人間工学的に良い状態 (ergonomic)」にしたい。
- Rust でカーネルを書くと、いくつものバグを未然に防ぐことができる。安全性に貢献できる。
- (あとでちゃんと調べて書く)

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- ABI modifier コンパイルフラグの安定化
- build-std の安定化
- doctest 関係の機能の安定化
- clippy の整備

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- 押さえておきたい前提: RFL では cargo を使用していない。
  - パッケージ（依存）管理は kbuild という Linux 開発用のツールで行なっているため。
  - cargo を使う意味があまりない…なので、rustc のコマンドを直接叩いてビルドしている。
  - rustc のコマンドを直接叩く際にいくつかの安定化されていないコンパイラフラグを使う。
  - これが nightly を要求する最大のブロッカーとなっている。

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- ABI modifier コンパイラフラグの安定化

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- build-std の安定化
  - rustc には build-std というフラグがあるが、これが安定化されていない。
  - std, core, alloc を再ビルドする際に使えるフラグ。
  - たとえば標準ライブラリの features=backtrace をオフにしたいなどのケースで使える。
  - Linux 開発ではこうしたビルドパラメータのカスタマイズがいくつか必要。

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- doctests 関連の機能の安定化
  - 現状は doctests の結果の生成に下記のコマンドで実行しているが、`-Zunstable-options`の利用が求められる。
  ```
  $ rustdoc +nightly --no-run --test-builder[path] -Zunstable-options
  ```
  - Rust 1.86.0 で`--output-format=doctest`が投入され、解決される。
  - (ちょっとまだ調査不足かも)

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### Rust for Linux に向けた開発

- clippy の整備
  - `.clippy.toml`がまだほとんどセットアップされていない状態なので整備を進める。
  - そもそも設定ファイルが追加されたのが 2024 年 10 月で、極めて最近のこと。

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### ここまでのまとめ

- RFL では、安定化されていない機能をかなり利用しているため、現状 nightly でしかビルドできない。
- そうした機能を安定化させて、stable でビルドできることを目指している。
- 同時に開発者体験向上にも取り組んでいる。

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### ここまでの参考資料

- 「Stabilize tooling needed by Rust for Linux」: https://rust-lang.github.io/rust-project-goals/2025h1/rfl.html
- RFL で使いたいがまだ安定化されていない機能がまとまった Issue: https://github.com/Rust-for-Linux/linux/issues/2
- build-std の現状: https://hackmd.io/@adamgemmell/rybJRFvdJe
- doctests 関連の議事録: https://hackmd.io/vcnuZEpqQaaVNVZZmTFyIA?view#rustdoc-a-way-to-extract-doctests
- 「Miguel Ojeda (Rust for Linux): KEYNOTE | RustConf 2024」: https://youtu.be/FRMJzNYut4g?si=WRmx-lnvoA3LKUW3

---

## 2025H1 の Flagship Goals を知る > Rust for Linux

### ここまでの参考資料

- 「Rust for Linux を手元で試す」: https://zenn.dev/garasubo/articles/rust-for-linux
- Misreading Chat RFL 回: https://misreading.chat/2024/12/03/142-an-empirical-study-of-rust-for-linux-the-success-dissatisfaction-and-compromise/

---

## 2025H1 の Flagship Goals を知る

### Rust All Hands 2025

- Rust は今年の 5 月で v1.0 がリリースされてから 10 年を迎える。
- これに伴い、今年 Rust Week by RustNL がオランダで開催される。
  - その中で、Rust All Hands 2025 というイベントが開催される。
- 基本的に Rust 本体に関わった人なら参加できるものらしい。
  - 基本的には Rust へのコントリビュータやコミッタのオフ会という位置づけっぽい。
    ーコミュニティ関連の人も参加できる…かも。

---

## 2025H1 の Flagship Goals を知る

### Rust All Hands 2025

- 「Organize Rust All-Hands 2025」: https://rust-lang.github.io/rust-project-goals/2025h1/all-hands.html
- RustWeek 2025: https://rustweek.org/
- こちらのお知らせから参加登録ができる: https://blog.rust-lang.org/inside-rust/2024/09/02/all-hands.html

---

## その他気になった Project の紹介

---

## その他気になった Project の紹介

- cargo-semver-check の本体入り
- StableMIR を作る
- clippy の高速化
- 次世代トレイトリゾルバー

---

## その他気になった Project の紹介

### cargo-semver-check の本体入り

- cargo-semver-check は、rustdoc を解析して互換性のない破壊的変更を検知するツール。
- これを本体入りさせようというもの。
  - cargo-publish のパイプラインの中に組み込むというアイディアもあるらしい。

---

## その他気になった Project の紹介

### cargo-semver-check の本体入り

- SemVer 違反の現状
  - Rust のエコシステムでは結構見る。書籍を書いていても出会った。
  - 実は何が SemVer 互換にあたるかは、定義が存在する。しかし、必ずしも絶対守られているとも言えない: https://doc.rust-lang.org/cargo/reference/semver.html
- 主要な約 1000 個のクレートに対する調査で、31 回のリリースに 1 回は SemVer 違反が含まれるという結果もあった。
  - ルールを全部覚えておいて実装するのは無理に近い。ツールの力に頼るべき。
  - 「Semver violations are common, better tooling is the answer」: https://predr.ag/blog/semver-violations-are-common-better-tooling-is-the-answer/
  - 個人的には、ツールの本体入りと cargo-publish 入りはぜひやってほしい。
