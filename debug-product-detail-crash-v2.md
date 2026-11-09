# Debug Session: product-detail-crash-v2

## Session Info
- **Session ID**: product-detail-crash-v2
- **Start Time**: 2026-06-21
- **Status**: [OPEN]

## Symptom
- 点击商品进入详情页后页面空白，然后app强制退出
- Manifest已添加但问题仍存在

## Hypotheses
1. **缓存问题** - 需要卸载旧应用清除缓存
2. **Glide初始化问题** - 在非UI线程初始化
3. **数据库表创建失败** - onCreate时表未正确创建
4. **布局文件引用错误** - R.id引用问题
5. **Intent传递问题** - productId为0或无效

## Instrument Points
- 添加try-catch包裹关键代码块
- 打印详细的崩溃日志

## Progress Log
