南小集 · 校园二手交易与闲置物品流转平台
Linux/AMD64 课程项目交付说明
========================================

一、项目与访问地址

项目名称：南小集 · 校园二手交易与闲置物品流转平台
GitHub 仓库：https://github.com/kongbai-dev/web-
仓库可见性：Public
功能演示录屏：video/南小集校园二手交易平台-功能使用演示.mp4
本地 Web 地址：http://localhost:8080
公网访问地址：暂无

二、运行环境

CPU 架构：x86_64 / AMD64
容器平台：Linux/AMD64
运行软件：Docker Engine 24+、Docker Compose v2
适用系统：支持 Linux 容器的 Windows、Linux 或 macOS
运行资源：2 核 CPU、4 GB 内存、5 GB 可用磁盘空间

三、交付目录

images/campus-market-x64-images.tar
  MySQL、Spring Boot 后端和 React/Nginx 前端的 Linux/AMD64 离线镜像包。

docker-compose.yml
  三项服务的一键编排文件。

.env.example
  邮箱验证码 SMTP 配置模板。

database/init/01-schema.sql
  MySQL 8 数据库结构和索引初始化脚本。

resources/uploads/
  用户上传图片的宿主机持久化目录。

video/南小集校园二手交易平台-功能使用演示.mp4
  项目功能使用演示录屏。

docs/性能问题与竞态资源问题处理报告.docx
  性能问题与竞态资源问题处理正式报告。

docs/性能问题与竞态资源问题处理报告.md
  报告 Markdown 源稿。

IMAGE_INFO.txt
  镜像 ID、架构与大小信息。

SHA256SUMS.txt
  镜像包、数据库脚本、录屏及关键文档的 SHA-256 校验值。

四、邮箱验证码服务配置

复制配置模板并填写实际发件邮箱和 SMTP 授权码：

Windows PowerShell：Copy-Item .env.example .env
Linux/macOS：cp .env.example .env

必填项：MAIL_USERNAME、MAIL_PASSWORD、MAIL_FROM。
QQ 邮箱默认使用 smtp.qq.com:465 和 SSL；使用其他邮箱服务时，同时修改主机、端口和加密参数。
.env 保存敏感凭据，不纳入 Git 仓库或公开提交材料。
验证码有效期为 10 分钟，同一邮箱的发送间隔为 60 秒。

五、Docker Compose 启动命令

在本 README.txt 所在目录执行：

1. 导入离线镜像

docker load -i images/campus-market-x64-images.tar

2. 启动全部服务

docker compose up -d --pull never

3. 查看服务状态

docker compose ps

4. 访问 Web 服务

http://localhost:8080

5. 查看运行日志

docker compose logs -f

6. 停止服务并保留数据

docker compose down

六、镜像信息

campus-market-backend:1.0.0-amd64
campus-market-frontend:1.0.0-amd64
mysql:8.4

架构检查命令：

docker image inspect campus-market-backend:1.0.0-amd64 --format "{{.Architecture}}/{{.Os}}"
docker image inspect campus-market-frontend:1.0.0-amd64 --format "{{.Architecture}}/{{.Os}}"
docker image inspect mysql:8.4 --format "{{.Architecture}}/{{.Os}}"

三张镜像的目标平台均为 amd64/linux。

七、数据库与资源文件挂载

1. 数据库初始化脚本

宿主机路径：./database/init
容器路径：/docker-entrypoint-initdb.d
挂载权限：只读

MySQL 数据目录为空时执行 01-schema.sql，Spring Boot 随后写入课程演示数据。

2. 数据库持久化卷

Docker 卷：campus-market-x64_mysql-data
容器路径：/var/lib/mysql

执行 docker compose down 后数据库卷继续保留。

3. 上传资源目录

宿主机路径：./resources/uploads
容器路径：/app/uploads

商品上传图片保存在 resources/uploads/products，数据库迁移时与该目录共同迁移。

八、演示账号

以下账号密码均为 123456：

管理员：admin
卖家：seller
学生用户：student

九、课程中印象最深刻的内容

印象最深刻的是前端请求、后端事务与数据库约束组成的完整业务链路。一个商品交易操作同时涉及身份认证、订单状态机、事务边界、数据库行锁与唯一约束，各层协同才能保证并发场景下的数据一致性。

十、课程改进建议

增加一次覆盖 Docker Compose 部署、数据库备份恢复、日志排查、并发测试和基础安全配置的综合实践，使课程项目从功能实现进一步延伸到部署、验收和维护。
