const express = require('express');
const admin = require('firebase-admin');
const { Firestore } = require('@google-cloud/firestore');
const jwt = require('jsonwebtoken');
const router = express.Router();

// Firebase Admin初期化
if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.applicationDefault(),
    projectId: process.env.GOOGLE_CLOUD_PROJECT
  });
}

const firestore = new Firestore();
const messaging = admin.messaging();

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

// FCMトークン登録
router.post('/register', authenticateToken, async (req, res) => {
  try {
    const { fcmToken, deviceInfo } = req.body;
    const userId = req.user.userId;

    if (!fcmToken) {
      return res.status(400).json({
        error: 'FCM token required',
        message: 'FCMトークンが必要です'
      });
    }

    // Firestoreにトークンを保存
    await firestore.collection('user_tokens').doc(userId).set({
      fcmToken,
      deviceInfo: deviceInfo || {},
      registeredAt: admin.firestore.FieldValue.serverTimestamp(),
      lastActive: admin.firestore.FieldValue.serverTimestamp(),
      isActive: true
    }, { merge: true });

    res.json({
      success: true,
      message: 'FCMトークンが登録されました'
    });

  } catch (error) {
    console.error('FCMトークン登録エラー:', error);
    res.status(500).json({
      error: 'Failed to register FCM token',
      message: 'FCMトークンの登録に失敗しました'
    });
  }
});

// 通知設定取得
router.get('/settings', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;
    
    const doc = await firestore.collection('notification_settings').doc(userId).get();
    
    if (!doc.exists) {
      // デフォルト設定を返す
      const defaultSettings = {
        newPosts: true,
        newComments: true,
        mentions: true,
        directMessages: true,
        upvotes: false,
        quietHours: {
          enabled: false,
          startTime: '22:00',
          endTime: '08:00'
        },
        frequency: 'immediate' // immediate, hourly, daily
      };
      
      return res.json({
        success: true,
        data: defaultSettings
      });
    }

    res.json({
      success: true,
      data: doc.data()
    });

  } catch (error) {
    console.error('通知設定取得エラー:', error);
    res.status(500).json({
      error: 'Failed to get notification settings',
      message: '通知設定の取得に失敗しました'
    });
  }
});

// 通知設定更新
router.put('/settings', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;
    const settings = req.body;

    await firestore.collection('notification_settings').doc(userId).set({
      ...settings,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    res.json({
      success: true,
      message: '通知設定が更新されました'
    });

  } catch (error) {
    console.error('通知設定更新エラー:', error);
    res.status(500).json({
      error: 'Failed to update notification settings',
      message: '通知設定の更新に失敗しました'
    });
  }
});

// 個別通知送信
router.post('/send', authenticateToken, async (req, res) => {
  try {
    const { targetUserId, title, body, data } = req.body;

    if (!targetUserId || !title || !body) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'targetUserId, title, bodyが必要です'
      });
    }

    // ターゲットユーザーのFCMトークンを取得
    const tokenDoc = await firestore.collection('user_tokens').doc(targetUserId).get();
    
    if (!tokenDoc.exists) {
      return res.status(404).json({
        error: 'User token not found',
        message: 'ユーザーのトークンが見つかりません'
      });
    }

    const { fcmToken } = tokenDoc.data();

    // 通知設定をチェック
    const settingsDoc = await firestore.collection('notification_settings').doc(targetUserId).get();
    const settings = settingsDoc.exists ? settingsDoc.data() : { newPosts: true };

    // クワイエット時間のチェック
    if (settings.quietHours?.enabled) {
      const now = new Date();
      const currentTime = now.getHours() * 100 + now.getMinutes();
      const startTime = parseInt(settings.quietHours.startTime.replace(':', ''));
      const endTime = parseInt(settings.quietHours.endTime.replace(':', ''));

      if (startTime <= endTime) {
        if (currentTime >= startTime && currentTime <= endTime) {
          return res.json({
            success: true,
            message: 'クワイエット時間のため通知をスキップしました'
          });
        }
      } else {
        if (currentTime >= startTime || currentTime <= endTime) {
          return res.json({
            success: true,
            message: 'クワイエット時間のため通知をスキップしました'
          });
        }
      }
    }

    // FCM通知を送信
    const message = {
      token: fcmToken,
      notification: {
        title,
        body
      },
      data: {
        ...data,
        timestamp: Date.now().toString()
      },
      android: {
        notification: {
          icon: 'ic_notification',
          color: '#FF4500',
          sound: 'default'
        }
      }
    };

    const response = await messaging.send(message);

    // 通知履歴を保存
    await firestore.collection('notification_history').add({
      targetUserId,
      title,
      body,
      data: data || {},
      sentAt: admin.firestore.FieldValue.serverTimestamp(),
      messageId: response,
      status: 'sent'
    });

    res.json({
      success: true,
      messageId: response,
      message: '通知が送信されました'
    });

  } catch (error) {
    console.error('通知送信エラー:', error);
    res.status(500).json({
      error: 'Failed to send notification',
      message: '通知の送信に失敗しました'
    });
  }
});

// 一括通知送信
router.post('/broadcast', authenticateToken, async (req, res) => {
  try {
    // 管理者権限チェック
    if (!req.user.isAdmin) {
      return res.status(403).json({
        error: 'Admin access required',
        message: '管理者権限が必要です'
      });
    }

    const { title, body, data, targetCondition } = req.body;

    if (!title || !body) {
      return res.status(400).json({
        error: 'Missing required fields',
        message: 'title, bodyが必要です'
      });
    }

    // 条件に基づいてトークンを取得
    let tokensQuery = firestore.collection('user_tokens').where('isActive', '==', true);
    
    if (targetCondition?.lastActiveAfter) {
      const afterDate = new Date(targetCondition.lastActiveAfter);
      tokensQuery = tokensQuery.where('lastActive', '>=', afterDate);
    }

    const tokensSnapshot = await tokensQuery.get();
    const tokens = tokensSnapshot.docs.map(doc => doc.data().fcmToken);

    if (tokens.length === 0) {
      return res.json({
        success: true,
        message: '送信対象のトークンがありません'
      });
    }

    // 一括送信メッセージを作成
    const message = {
      notification: {
        title,
        body
      },
      data: {
        ...data,
        timestamp: Date.now().toString()
      },
      android: {
        notification: {
          icon: 'ic_notification',
          color: '#FF4500',
          sound: 'default'
        }
      },
      tokens
    };

    const response = await messaging.sendMulticast(message);

    // 結果を記録
    await firestore.collection('broadcast_history').add({
      title,
      body,
      data: data || {},
      targetCondition: targetCondition || {},
      sentAt: admin.firestore.FieldValue.serverTimestamp(),
      successCount: response.successCount,
      failureCount: response.failureCount,
      totalTokens: tokens.length
    });

    res.json({
      success: true,
      successCount: response.successCount,
      failureCount: response.failureCount,
      totalTokens: tokens.length,
      message: '一括通知が送信されました'
    });

  } catch (error) {
    console.error('一括通知送信エラー:', error);
    res.status(500).json({
      error: 'Failed to send broadcast notification',
      message: '一括通知の送信に失敗しました'
    });
  }
});

// 通知履歴取得
router.get('/history', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;
    const limit = parseInt(req.query.limit) || 50;
    const offset = parseInt(req.query.offset) || 0;

    const snapshot = await firestore
      .collection('notification_history')
      .where('targetUserId', '==', userId)
      .orderBy('sentAt', 'desc')
      .limit(limit)
      .offset(offset)
      .get();

    const notifications = snapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data(),
      sentAt: doc.data().sentAt?.toDate()
    }));

    res.json({
      success: true,
      data: notifications,
      hasMore: snapshot.size === limit
    });

  } catch (error) {
    console.error('通知履歴取得エラー:', error);
    res.status(500).json({
      error: 'Failed to get notification history',
      message: '通知履歴の取得に失敗しました'
    });
  }
});

// FCMトークンの無効化
router.delete('/token', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.userId;

    await firestore.collection('user_tokens').doc(userId).update({
      isActive: false,
      deactivatedAt: admin.firestore.FieldValue.serverTimestamp()
    });

    res.json({
      success: true,
      message: 'FCMトークンが無効化されました'
    });

  } catch (error) {
    console.error('FCMトークン無効化エラー:', error);
    res.status(500).json({
      error: 'Failed to deactivate FCM token',
      message: 'FCMトークンの無効化に失敗しました'
    });
  }
});

module.exports = router; 