![メインアイコン画像](icon.png)

Reddit の投稿・コメントを、日本語に翻訳して表示するAndroidアプリ

---

## 概要

Reddit APIとGoogle CloudTranslationAPIを組み合わせ、投稿・コメントを最初から日本語で表示するAndroidアプリです。
自身のRedditアカウントにログインもできます。
apiキーは含んでおらず、また現在は私のサーバーの方もapi料金節約のため停止しています。そのため動作させる為には環境構築を行う必要があります。(ご連絡いただければこちらで建てることも可能です！)

## なぜつくったか

Redditを利用する上での不満を解消するため。
海外サッカーや物理学の情報収集に使っていたが、物理系のスレッドは専門用語が多く、英語のまま読むのに時間がかかることがありました。
また、Reddit公式の翻訳機能は一部の投稿にしか対応していないこともあり、不便を感じていたため制作しました。

このアプリは開発においてCursor(Claude Sonnet 3.5)を利用しました。

### 任せた部分
- Express のルーティング・ミドルウェアの雛形
- helmet / cors などセキュリティ設定
- エラーハンドリングの定型処理
など

### 自分で直した部分
- クリーンアーキテクチャ（data / domain / presentation）
- AppModule / NetworkModule / RepositoryModuleの分離
- KotlinクライアントとNode.jsサーバー間の503エラー等の修正
- 投稿カード・リストのレイアウトとアイコンのUI調整（AI生成のデザインが使いにくかったため）
など

## 結論、反省
・デザイン等は割と自分で実装する必要があると感じた。
・Firebase等の実装時、かなりエラーが多発した。その辺は公式リファレンスを読み、確実に実装することの重要性を学んだ。。
・一日数時間の利用×1週間でも、1000円程度TranslateAPIの料金がかかってしまった。
・開発段階の時コミットをほぼしなかったこと。何故しなかったのか今になっては思い出せない。

## 技術構成

| 
| カテゴリ | 使用技術 
|---|---|
| モバイル（Android） | Kotlin, Jetpack Compose |
| 認証 | OAuth2, JWT, DataStore |
| 通信 | Retrofit2 |
| DI | Hilt |
| バックエンド | Node.js / Express |
| 外部API | Reddit API / Google TranslateAPI |
| APIキー管理 | Google Cloud SecretManager |
| DB | Google Cloud Firestore |

## スクリーンショット