const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const rateLimit = require('rate-limiter-flexible');
const winston = require('winston');
require('dotenv').config();

const authRoutes = require('./routes/auth');
const apiKeyRoutes = require('./routes/apiKeys');
const translationRoutes = require('./routes/translation');
const notificationRoutes = require('./routes/notifications');
const userRoutes = require('./routes/users');

const app = express();
const PORT = process.env.PORT || 8080;

// ロガー設定
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.Console(),
    new winston.transports.File({ filename: 'error.log', level: 'error' }),
    new winston.transports.File({ filename: 'combined.log' })
  ]
});

// レート制限設定
const rateLimiter = new rateLimit.RateLimiterMemory({
  keyGenerator: (req) => req.ip,
  points: 100, // リクエスト数
  duration: 60, // 60秒間
});

const rateLimiterMiddleware = async (req, res, next) => {
  try {
    await rateLimiter.consume(req.ip);
    next();
  } catch (rejRes) {
    res.status(429).json({
      error: 'Too Many Requests',
      message: 'レート制限に達しました。しばらく待ってから再試行してください。'
    });
  }
};

// ミドルウェア設定
app.use(helmet());
app.use(cors({
  origin: process.env.ALLOWED_ORIGINS?.split(',') || ['http://localhost:3000'],
  credentials: true
}));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));
app.use(rateLimiterMiddleware);

// ログミドルウェア
app.use((req, res, next) => {
  logger.info(`${req.method} ${req.path} - IP: ${req.ip}`);
  next();
});

// ルート設定
app.use('/api/auth', authRoutes);
app.use('/api/keys', apiKeyRoutes);
app.use('/api/translation', translationRoutes);
app.use('/api/notifications', notificationRoutes);
app.use('/api/users', userRoutes);

// ヘルスチェック
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    timestamp: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0'
  });
});

// ルートエンドポイント
app.get('/', (req, res) => {
  res.json({
    message: 'Reddit翻訳ビューワー API Server',
    version: '1.0.0',
    endpoints: {
      auth: '/api/auth',
      apiKeys: '/api/keys',
      translation: '/api/translation',
      notifications: '/api/notifications',
      users: '/api/users',
      health: '/health'
    }
  });
});

// エラーハンドリング
app.use((err, req, res, next) => {
  logger.error('Unhandled error:', err);
  res.status(500).json({
    error: 'Internal Server Error',
    message: '内部サーバーエラーが発生しました'
  });
});

// 404ハンドラー
app.use('*', (req, res) => {
  res.status(404).json({
    error: 'Not Found',
    message: 'エンドポイントが見つかりません'
  });
});

// サーバー起動
app.listen(PORT, () => {
  logger.info(`Server is running on port ${PORT}`);
  console.log(`🚀 Reddit翻訳ビューワー API Server が起動しました`);
  console.log(`📡 Port: ${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV || 'development'}`);
});

module.exports = app; 