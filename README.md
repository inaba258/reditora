# Reddit翻訳ビューワー

Android向けのReddit投稿を自動で日本語に翻訳して表示するビューワーアプリです。

## 🚀 実装済み機能

- **OAuth2認証**: Redditアカウントでのセキュアなログイン ✅
- **投稿ビューワー**: Reddit投稿の閲覧と自動翻訳 ✅
- **コメントビューワー**: コメントのツリー表示と翻訳 ✅
- **サブレディット検索**: 特定のサブレディットの投稿閲覧 ✅
- **翻訳切り替え**: 原文/翻訳の即座切り替え ✅
- **ソート機能**: Hot/New/Top投稿のソート ✅
- **メディア表示**: 画像・動画の表示対応 ✅

## 🛠️ 技術スタック

- **言語**: Kotlin
- **UI**: Jetpack Compose + Material3
- **アーキテクチャ**: MVVM + Clean Architecture
- **依存性注入**: Hilt
- **ネットワーク**: Retrofit + OkHttp
- **非同期処理**: Coroutines + Flow
- **データ永続化**: DataStore Preferences
- **画像読み込み**: Coil

## 📋 セットアップ手順

### 1. Reddit APIキーの取得

1. [Reddit App Preferences](https://www.reddit.com/prefs/apps)にアクセス
2. "Create App"または"Create Another App"をクリック
3. アプリ情報を入力:
   - **name**: アプリ名
   - **App type**: "Installed app"を選択
   - **description**: アプリの説明
   - **about url**: (オプション)
   - **redirect uri**: `redditviewer://auth`
4. 作成後、Client IDをメモ

### 2. 翻訳APIキーの設定

Trymagic ArmL Translate APIのキーを取得してください。

### 3. local.propertiesの設定

プロジェクトルートに`local.properties`ファイルを作成し、以下を設定:

```properties
REDDIT_CLIENT_ID=your_reddit_client_id_here
REDDIT_CLIENT_SECRET=your_reddit_client_secret_here
REDDIT_REDIRECT_URI=redditviewer://auth
TRANSLATION_API_KEY=your_translation_api_key_here
TRANSLATION_API_URL=https://api.trymagic.com/translate
sdk.dir=C:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### 4. ビルドと実行

```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease
```

## 📱 アプリの使用方法

### 基本操作
1. **ログイン**: アプリを起動し、"Redditでログイン"ボタンをタップ
2. **認証**: ブラウザでRedditアカウントにログインし、アプリを承認
3. **投稿閲覧**: ホーム画面で投稿を閲覧（自動的に日本語に翻訳されます）

### 高度な機能
- **翻訳切り替え**: 翻訳アイコンをタップして原文/翻訳を切り替え
- **サブレディット検索**: 検索アイコンから特定のサブレディットを検索
- **投稿詳細**: 投稿をタップしてコメントを表示
- **ソート**: サブレディット内で人気/新着/トップでソート
- **コメントツリー**: ネストしたコメントを階層表示

## 🔒 セキュリティ

- OAuth2を使用したセキュアな認証
- APIキーは`local.properties`で管理（Gitにコミットされません）
- HTTPS通信のみ使用
- 認証トークンはDataStoreで暗号化保存

## 🏗️ アーキテクチャ

```
presentation/     # UI層 (Compose, ViewModel)
├── auth/        # 認証画面
├── home/        # ホーム画面
├── postdetail/  # 投稿詳細画面
├── components/  # 共通UIコンポーネント
└── theme/       # テーマ設定

domain/          # ドメイン層 (UseCase, Entity, Repository Interface)
├── model/       # エンティティ
└── repository/  # リポジトリインターフェース

data/            # データ層 (Repository実装, API, DB)
├── remote/      # API通信
├── local/       # ローカルデータ
├── repository/  # リポジトリ実装
└── mapper/      # データ変換

di/              # 依存性注入設定
```

## 📦 APK生成

### デバッグ版APK
```bash
./gradlew assembleDebug
```
生成されたAPK: `app/build/outputs/apk/debug/app-debug.apk`

### リリース版APK
```bash
./gradlew assembleRelease
```
生成されたAPK: `app/build/outputs/apk/release/app-release.apk`

### 署名付きAPK（本番用）
1. キーストアファイルを作成:
```bash
keytool -genkey -v -keystore your-release-key.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias your-key-alias
```

2. `app/build.gradle`の署名設定を有効化:
```gradle
signingConfigs {
    release {
        storeFile file('your-release-key.keystore')
        storePassword 'your-store-password'
        keyAlias 'your-key-alias'
        keyPassword 'your-key-password'
    }
}
```

3. 署名付きAPKを生成:
```bash
./gradlew assembleRelease
```

## 🎨 UI/UX特徴

- **Material3デザイン**: 最新のマテリアルデザインガイドラインに準拠
- **ダークテーマ対応**: システム設定に応じた自動切り替え
- **日本語最適化**: 完全日本語対応のUI
- **レスポンシブレイアウト**: 様々な画面サイズに対応
- **滑らかなアニメーション**: 画面遷移とUI要素のアニメーション

## 🔧 カスタマイズ

### 翻訳API変更
`TranslationRepositoryImpl.kt`を編集して他の翻訳サービスに対応可能

### UI テーマ変更
`presentation/theme/`内のファイルでカラーとタイポグラフィをカスタマイズ

### 追加機能
- 投稿の保存/ブックマーク機能
- コメントへの返信機能
- オフライン閲覧機能

## 🤝 コントリビューション

1. このリポジトリをフォーク
2. 機能ブランチを作成 (`git checkout -b feature/AmazingFeature`)
3. 変更をコミット (`git commit -m 'Add some AmazingFeature'`)
4. ブランチにプッシュ (`git push origin feature/AmazingFeature`)
5. プルリクエストを作成

## 📄 ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## ⚠️ 注意事項

- Reddit APIの利用規約を遵守してください
- APIリクエストのレート制限に注意してください
- 個人情報の取り扱いに注意してください
- 翻訳APIの利用料金にご注意ください

## 📞 サポート

問題や質問がある場合は、GitHubのIssuesページでお知らせください。

---

**🎉 Reddit翻訳ビューワーをお楽しみください！** 