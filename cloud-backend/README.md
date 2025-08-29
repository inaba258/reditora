# Reddit翻訳ビューワー バックエンドAPI

Google Cloud Run上で動作するバックエンドAPIサーバーです。APIキー管理、プッシュ通知、ユーザープロフィール機能を提供します。

## 🏗️ アーキテクチャ

- **プラットフォーム**: Google Cloud Run
- **ランタイム**: Node.js 18
- **データベース**: Firestore
- **シークレット管理**: Google Secret Manager
- **プッシュ通知**: Firebase Cloud Messaging

## 📋 セットアップ手順

### 1. Google Cloudプロジェクトの設定

```bash
# Google Cloud SDKをインストール
# https://cloud.google.com/sdk/docs/install

# プロジェクトを作成
gcloud projects create reddit-viewer-backend --name="Reddit Viewer Backend"

# プロジェクトを選択
gcloud config set project reddit-viewer-backend

# 必要なAPIを有効化
gcloud services enable run.googleapis.com
gcloud services enable secretmanager.googleapis.com
gcloud services enable firestore.googleapis.com
gcloud services enable firebase.googleapis.com
```

### 2. Firebase プロジェクトの設定

1. [Firebase Console](https://console.firebase.google.com/)でプロジェクトを作成
2. Firestore Databaseを有効化
3. Firebase Cloud Messagingを設定
4. サービスアカウントキーをダウンロード

### 3. Secret Managerにシークレットを保存

```bash
# Reddit APIキーを保存
gcloud secrets create reddit-api-keys --data-file=reddit-secrets.json

# 翻訳APIキーを保存  
gcloud secrets create translation-api-keys --data-file=translation-secrets.json

# JWTシークレットを保存
echo -n "your-jwt-secret-key" | gcloud secrets create jwt-secret --data-file=-
```

**reddit-secrets.json の例:**
```json
{
  "clientId": "your-reddit-client-id",
  "clientSecret": "your-reddit-client-secret",
  "redirectUri": "redditviewer://auth"
}
```

**translation-secrets.json の例:**
```json
{
  "apiKey": "your-translation-api-key",
  "apiUrl": "https://api.trymagic.com/translate"
}
```

### 4. 環境変数の設定

`.env` ファイルを作成:

```env
# Google Cloud
GOOGLE_CLOUD_PROJECT=your-project-id
NODE_ENV=production

# JWT
JWT_SECRET=your-jwt-secret

# CORS
ALLOWED_ORIGINS=https://your-app-domain.com,http://localhost:3000

# Firebase
FIREBASE_PROJECT_ID=your-firebase-project-id
```

### 5. デプロイメント

```bash
# 依存関係をインストール
npm install

# Cloud Runにデプロイ
gcloud run deploy reddit-viewer-api \
  --source . \
  --platform managed \
  --region asia-northeast1 \
  --allow-unauthenticated \
  --set-env-vars GOOGLE_CLOUD_PROJECT=your-project-id \
  --set-env-vars NODE_ENV=production \
  --memory 1Gi \
  --cpu 1 \
  --concurrency 100 \
  --timeout 300s \
  --max-instances 10
```

## 🔧 ローカル開発

### 開発環境のセットアップ

```bash
# 依存関係をインストール
npm install

# Google Cloud認証
gcloud auth application-default login

# 開発サーバーを起動
npm run dev
```

### 環境変数 (.env.local)

```env
GOOGLE_CLOUD_PROJECT=your-project-id
NODE_ENV=development
JWT_SECRET=your-local-jwt-secret
ALLOWED_ORIGINS=http://localhost:3000
```

## 📡 API エンドポイント

### 認証
- `POST /api/auth/login` - ユーザーログイン
- `POST /api/auth/refresh` - トークン更新
- `DELETE /api/auth/logout` - ログアウト

### APIキー管理
- `GET /api/keys/reddit` - Reddit APIキー取得
- `GET /api/keys/translation` - 翻訳APIキー取得
- `POST /api/keys/reddit/client-secret` - OAuth認証処理

### プッシュ通知
- `POST /api/notifications/register` - FCMトークン登録
- `GET /api/notifications/settings` - 通知設定取得
- `PUT /api/notifications/settings` - 通知設定更新
- `POST /api/notifications/send` - 通知送信
- `GET /api/notifications/history` - 通知履歴

### ユーザー管理
- `GET /api/users/profile` - プロフィール取得
- `PUT /api/users/profile` - プロフィール更新
- `DELETE /api/users/data` - ユーザーデータ削除

### システム
- `GET /health` - ヘルスチェック
- `GET /` - API情報

## 🔐 セキュリティ

### 認証・認可
- JWT トークンベース認証
- リフレッシュトークン対応
- 管理者権限チェック

### レート制限
- IP ベースのレート制限 (100リクエスト/分)
- API キー別制限
- DDoS 保護

### データ保護
- HTTPS 必須
- CORS 設定
- Helmet.js セキュリティヘッダー
- 入力値検証

## 📊 監視とロギング

### Cloud Logging
```bash
# ログを確認
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=reddit-viewer-api" --limit 50
```

### Cloud Monitoring
- CPU 使用率
- メモリ使用量
- リクエスト数
- エラー率

## 🚀 CI/CD

### GitHub Actions ワークフロー

`.github/workflows/deploy.yml`:
```yaml
name: Deploy to Cloud Run

on:
  push:
    branches: [main]
    paths: [cloud-backend/**]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - id: 'auth'
        uses: 'google-github-actions/auth@v1'
        with:
          credentials_json: '${{ secrets.GCP_SA_KEY }}'
      
      - name: 'Set up Cloud SDK'
        uses: 'google-github-actions/setup-gcloud@v1'
      
      - name: 'Deploy to Cloud Run'
        run: |
          cd cloud-backend
          gcloud run deploy reddit-viewer-api \
            --source . \
            --platform managed \
            --region asia-northeast1 \
            --allow-unauthenticated
```

## 🧪 テスト

```bash
# 単体テスト
npm test

# 統合テスト
npm run test:integration

# カバレッジレポート
npm run test:coverage
```

## 📈 パフォーマンス最適化

### Cloud Run設定
- **CPU**: 1 vCPU
- **メモリ**: 1 GiB
- **同時実行数**: 100
- **タイムアウト**: 300秒
- **最大インスタンス**: 10

### データベース最適化
- Firestore インデックス最適化
- クエリ結果キャッシュ
- バッチ処理の活用

## 🔄 バックアップとリストア

### Firestore バックアップ
```bash
# 自動バックアップを設定
gcloud firestore databases create --type=firestore-native --location=asia-northeast1

# 手動バックアップ
gcloud firestore export gs://your-backup-bucket/firestore-backup
```

## 📞 サポート

問題や質問がある場合は、以下の方法でお問い合わせください：

- GitHub Issues
- メール: support@reddit-viewer.com
- Slack: #reddit-viewer-support

## 📄 ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。 