校园二手交易与闲置物品流转平台 - X64 交付说明
================================================

一、GitHub 仓库

仓库地址：https://github.com/kongbai-dev/web-
仓库可见性：Public（公开仓库），无需邀请 sunshinezxf@hotmail.com。

二、运行环境

1. CPU 架构：x86_64 / AMD64。
2. 操作系统：Windows、Linux 或 macOS（需支持 Linux 容器）。
3. 软件：Docker Engine 24+，Docker Compose v2。
4. 建议资源：2 核 CPU、4 GB 内存、至少 5 GB 可用磁盘空间。

三、目录内容

images/campus-market-x64-images.tar    Linux/AMD64 镜像离线包
docker-compose.yml                     一键编排文件
database/init/01-schema.sql            MySQL 8 数据库结构脚本
resources/uploads/                     用户上传图片持久化挂载目录
video/南小集校园二手交易平台-功能使用演示.mp4  功能使用演示录屏
docs/性能问题与竞态资源问题处理报告.docx  正式报告
docs/性能问题与竞态资源问题处理报告.md    报告源稿
SHA256SUMS.txt                          镜像包与关键文件校验值

四、Docker Compose 启动命令（推荐）

请在本 README.txt 所在目录打开终端并执行：

1. 导入离线镜像：
   docker load -i images/campus-market-x64-images.tar

2. 启动全部服务：
   docker compose up -d --pull never

3. 查看运行状态：
   docker compose ps

4. 查看日志：
   docker compose logs -f

5. 停止服务但保留数据：
   docker compose down

注意：不要使用 “docker compose down -v”。“-v” 会删除 MySQL 命名卷；误删
resources/uploads 也会造成上传资源丢失。

五、单镜像启动说明

本系统由 MySQL、Spring Boot 后端、React/Nginx 前端三个容器协作运行，推荐使用
Docker Compose。若需检查已导入镜像，可执行：

docker image inspect campus-market-backend:1.0.0-amd64 --format "{{.Architecture}}/{{.Os}}"
docker image inspect campus-market-frontend:1.0.0-amd64 --format "{{.Architecture}}/{{.Os}}"
docker image inspect mysql:8.4 --format "{{.Architecture}}/{{.Os}}"

三条命令均应输出 amd64/linux。

六、数据库与资源文件挂载说明

1. 数据库初始化脚本
   宿主机：./database/init
   容器内：/docker-entrypoint-initdb.d（只读）
   01-schema.sql 仅在 MySQL 数据目录第一次为空时执行。Spring Boot 会在首次启动时
   自动写入课程演示数据。

2. 数据库持久化卷
   Docker 卷：campus-market-x64_mysql-data（Compose 项目名变化时前缀会相应变化）
   容器内：/var/lib/mysql
   采用 Docker 命名卷是为了兼容 Windows/WSL：MySQL 需要修改证书文件权限，直接
   挂载 NTFS 目录可能因权限语义不同而初始化失败。查看卷可执行：
   docker volume ls
   逻辑备份可执行：
   docker compose exec -T mysql mysqldump -uroot -proot_2026_change_me --databases campus_market > database/campus_market_backup.sql

3. 上传资源目录
   宿主机：./resources/uploads
   容器内：/app/uploads
   商品图片保存在 resources/uploads/products 下。迁移时需与数据库备份一并复制。

4. 恢复 SQL 备份
   先启动 MySQL，再执行：
   docker compose exec -T mysql mysql -uroot -proot_2026_change_me < database/campus_market_backup.sql

5. 恢复到全新演示数据
   先备份现有数据，再执行 docker compose down -v，然后执行
   docker compose up -d --pull never。初始化脚本会重新执行，应用会重新写入演示数据。
   “down -v” 会永久删除当前数据库，请仅在确实需要重置时使用。

七、访问地址与演示账号

本地 Web 地址：http://localhost:8080
公网访问地址：暂无（本交付包为本地 Docker 部署版本）。

演示账号（密码均为 123456）：
管理员：admin
卖家：seller
学生用户：student

八、主要端口

8080：前端 Web 服务，也是浏览器唯一需要访问的端口。
MySQL 与后端端口仅在 Compose 内部网络开放，不直接暴露到宿主机，以减小攻击面。

九、安全提示

docker-compose.yml 中的数据库密码和 JWT 密钥仅供课程验收。部署到公网前必须替换，
并使用 HTTPS、限制上传文件类型与大小、配置定期备份。若 8080 端口被占用，可将
docker-compose.yml 中的 “8080:80” 改为 “其他端口:80”。

十、课程中印象最深刻的内容（可选）

印象最深刻的是从前端请求、后端事务到数据库约束的完整链路。一个看似简单的“购买”
按钮，实际涉及身份认证、状态机、事务边界、行锁与唯一约束；只有把这些层次协同起来，
系统才能在并发请求下保持数据正确。

十一、课程改进建议（可选）

建议增加一次面向真实部署的综合实践，覆盖 Docker Compose、数据库备份恢复、日志排查、
并发测试和基础安全配置。这样可以帮助学生把“功能可运行”进一步提升到“可部署、可验收、
可维护”。
