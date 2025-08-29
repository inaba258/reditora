const express = require('express');
const { SecretManagerServiceClient } = require('@google-cloud/secret-manager');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const router = express.Router();

const secretClient = new SecretManagerServiceClient();
const PROJECT_ID = process.env.GOOGLE_CLOUD_PROJECT;

// JWT認証ミドルウェア
const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ 
      error: 'Access Token Required',
      message: 'アクセストークンが必要です'
    });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ 
        error: 'Invalid Token',
        message: '無効なトークンです'
      });
    }
    req.user = user;
    next();
  });
};

// APIキー取得エンドポイント
router.get('/reddit', authenticateToken, async (req, res) => {
  try {
    const secretName = `projects/${PROJECT_ID}/secrets/reddit-api-keys/versions/latest`;
    const [version] = await secretClient.accessSecretVersion({ name: secretName });
    
    const secretData = JSON.parse(version.payload.data.toString());
    
    res.json({
      success: true,
      data: {
        clientId: secretData.clientId,
        redirectUri: secretData.redirectUri,
        // クライアントシークレットは返さない（セキュリティ上の理由）
      }
    });
  } catch (error) {
    console.error('Reddit APIキー取得エラー:', error);
    res.status(500).json({
      error: 'Failed to retrieve API keys',
      message: 'APIキーの取得に失敗しました'
    });
  }
});

// 翻訳APIキー取得エンドポイント
router.get('/translation', authenticateToken, async (req, res) => {
  try {
    const secretName = `projects/${PROJECT_ID}/secrets/translation-api-keys/versions/latest`;
    const [version] = await secretClient.accessSecretVersion({ name: secretName });
    
    const secretData = JSON.parse(version.payload.data.toString());
    
    res.json({
      success: true,
      data: {
        apiKey: secretData.apiKey,
        apiUrl: secretData.apiUrl
      }
    });
  } catch (error) {
    console.error('翻訳APIキー取得エラー:', error);
    res.status(500).json({
      error: 'Failed to retrieve translation API keys',
      message: '翻訳APIキーの取得に失敗しました'
    });
  }
});

// Reddit OAuth認証用のクライアントシークレット取得（サーバーサイド専用）
router.post('/reddit/client-secret', authenticateToken, async (req, res) => {
  try {
    const { code, redirectUri } = req.body;
    
    if (!code || !redirectUri) {
      return res.status(400).json({
        error: 'Missing required parameters',
        message: 'codeとredirectUriが必要です'
      });
    }

    // Secret Managerから認証情報を取得
    const secretName = `projects/${PROJECT_ID}/secrets/reddit-api-keys/versions/latest`;
    const [version] = await secretClient.accessSecretVersion({ name: secretName });
    const secretData = JSON.parse(version.payload.data.toString());

    // Reddit APIにトークン交換リクエスト
    const axios = require('axios');
    const credentials = Buffer.from(`${secretData.clientId}:`).toString('base64');
    
    const tokenResponse = await axios.post('https://www.reddit.com/api/v1/access_token', 
      `grant_type=authorization_code&code=${code}&redirect_uri=${redirectUri}`, {
        headers: {
          'Authorization': `Basic ${credentials}`,
          'Content-Type': 'application/x-www-form-urlencoded',
          'User-Agent': 'RedditViewer/1.0.0'
        }
      }
    );

    res.json({
      success: true,
      data: tokenResponse.data
    });
    
  } catch (error) {
    console.error('Reddit OAuth認証エラー:', error);
    res.status(500).json({
      error: 'OAuth authentication failed',
      message: 'OAuth認証に失敗しました'
    });
  }
});

// APIキー更新エンドポイント（管理者専用）
router.put('/reddit', authenticateToken, async (req, res) => {
  try {
    // 管理者権限チェック
    if (!req.user.isAdmin) {
      return res.status(403).json({
        error: 'Admin access required',
        message: '管理者権限が必要です'
      });
    }

    const { clientId, clientSecret, redirectUri } = req.body;
    
    const secretData = {
      clientId,
      clientSecret,
      redirectUri,
      updatedAt: new Date().toISOString(),
      updatedBy: req.user.userId
    };

    // Secret Managerに新しいバージョンを作成
    const parent = `projects/${PROJECT_ID}/secrets/reddit-api-keys`;
    await secretClient.addSecretVersion({
      parent,
      payload: {
        data: Buffer.from(JSON.stringify(secretData))
      }
    });

    res.json({
      success: true,
      message: 'Reddit APIキーが更新されました'
    });

  } catch (error) {
    console.error('APIキー更新エラー:', error);
    res.status(500).json({
      error: 'Failed to update API keys',
      message: 'APIキーの更新に失敗しました'
    });
  }
});

// 翻訳APIキー更新エンドポイント（管理者専用）
router.put('/translation', authenticateToken, async (req, res) => {
  try {
    if (!req.user.isAdmin) {
      return res.status(403).json({
        error: 'Admin access required',
        message: '管理者権限が必要です'
      });
    }

    const { apiKey, apiUrl } = req.body;
    
    const secretData = {
      apiKey,
      apiUrl,
      updatedAt: new Date().toISOString(),
      updatedBy: req.user.userId
    };

    const parent = `projects/${PROJECT_ID}/secrets/translation-api-keys`;
    await secretClient.addSecretVersion({
      parent,
      payload: {
        data: Buffer.from(JSON.stringify(secretData))
      }
    });

    res.json({
      success: true,
      message: '翻訳APIキーが更新されました'
    });

  } catch (error) {
    console.error('翻訳APIキー更新エラー:', error);
    res.status(500).json({
      error: 'Failed to update translation API keys',
      message: '翻訳APIキーの更新に失敗しました'
    });
  }
});

module.exports = router; 