# 青禾集 · 校园二手交易与闲置物品流转平台

本项目依据《校园二手交易与闲置物品流转平台_完整设计方案》实现，采用 Spring Boot + React 前后端分离架构。默认使用 H2 文件数据库，克隆后无需安装数据库即可演示；也提供 MySQL profile。

## 已实现功能

- 用户注册、JWT 登录、BCrypt 密码加密、个人资料与信用评分
- 商品发布、编辑、下架、重新上架、分类、关键词/价格筛选、排序、本地图片上传
- 商品收藏、浏览历史、我的发布
- 订单并发锁与完整状态机：创建 → 卖家确认 → 模拟支付 → 交付 → 收货 → 完成
- 买卖双方权限校验、取消订单、完成后评价
- 商品举报、我的举报
- 管理后台：统计、用户封禁、商品下架、订单查看、举报处理、分类管理
- H2 / MySQL 双数据库配置、统一 401/403 与异常响应、核心参数校验、响应式 React 页面

## 快速启动

环境要求：JDK 21+、Maven 3.9+、Node.js 20+。

后端：

```powershell
cd backend
mvn package -DskipTests
java -jar target/campus-market-1.0.0.jar
```

> 当前项目位于中文目录。部分 Windows 终端中 `mvn spring-boot:run` 的派生 Java 进程可能无法正确解析中文 classpath，因此推荐使用上面的可执行 Jar 启动方式。

前端（另开终端）：

```powershell
cd frontend
npm install
npm run dev
```

访问 <http://localhost:5173>。API 位于 `http://localhost:8080/api`，H2 控制台位于 `http://localhost:8080/h2-console`，JDBC URL 为 `jdbc:h2:file:./data/campus-market`。

## 演示账号

| 角色 | 用户名 | 密码 | 用途 |
|---|---|---|---|
| 买家 | student | 123456 | 浏览、收藏、购买、举报 |
| 卖家 | seller | 123456 | 发布商品、确认订单、交付 |
| 管理员 | admin | 123456 | 后台治理与统计 |

推荐答辩时分别在普通窗口与无痕窗口登录 `student` 和 `seller`，完整演示订单状态流转；随后用 `admin` 处理预置举报。

## 使用 MySQL

先创建或允许自动创建 `campus_market` 数据库，然后修改 `backend/src/main/resources/application.yml` 中 mysql profile 的账号密码：

```powershell
cd backend
mvn package -DskipTests
java -jar target/campus-market-1.0.0.jar --spring.profiles.active=mysql
```

## 验证命令

```powershell
cd backend
mvn test

cd ../frontend
npm run build
```

后端集成测试包含 3 个场景：以买家、卖家两个身份跑通完整订单状态机并验证商品最终变为已售出；验证未登录接口的统一响应；验证非法价格拦截和本地图片上传。

本地上传图片保存在 `backend/uploads/products`，单张最大 5MB，支持 JPG、PNG、WebP 和 GIF。该运行时目录已加入 `.gitignore`。
