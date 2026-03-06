# れでぃとらバックエンド

## 構成

- **プラットフォーム**: Google Cloud Run
- **ランタイム**: Node.js 18
- **データベース**: Firestore
- **シークレット管理**: Google Secret Manager
- **プッシュ通知**: Firebase Cloud Messaging(まだ)

## セットアップ手順

### 1.Google Cloudプロジェクトの設定

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

## ローカル

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

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。 