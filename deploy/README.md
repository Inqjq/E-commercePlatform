# 中间件与后端部署指南（4核4G 云服务器）

本目录提供 MySQL 8 + Redis 7 的 Docker Compose 配置，以及后端服务在云服务器上的部署步骤。
部署拓扑：**单台 4c4g 服务器**，MySQL/Redis 以 Docker 容器运行（仅绑定 127.0.0.1），后端 jar 同机运行，前端静态文件由 Nginx 托管。

> **联调拓扑：中间件在云服务器、应用在本地**——后端暂不上服务器时，只需服务器上的 MySQL/Redis
> 能被本地连上：MySQL 复用已有实例（已开放 3306）建库建号即可；Redis 用
> `docker run -p 6379:6379` 发布端口，**必须在云安全组把 6379 限制为只放行你本地的出口 IP**，
> 且 requirepass 必须是强密码；更稳妥的方式是 SSH 隧道（Redis 保持只绑 127.0.0.1）：
> `ssh -N -L 16379:127.0.0.1:6379 user@服务器IP`，本地配 `REDIS_HOST=127.0.0.1 REDIS_PORT=16379`。
> 本地连接参数写入 `backend/start-local.sh`（已被 gitignore），模板见 `backend/start-local.sh.example`。

> **服务器上已有 MySQL 时（如 dcloud-mysql），不要再用本目录的 compose 起第二个 MySQL**：
> 4G 内存扛不住两个 mysqld，3306 端口也会冲突。直接走下方「方案 A：复用已有 MySQL」，
> 只需新增一个 Redis 容器即可。

## 一、部署中间件

### 方案 A：复用服务器上已有的 MySQL（推荐，4G 内存机器首选）

**1. 在现有 MySQL 里建库和专用账号**（不要用 root 连业务）：

```bash
docker exec -it dcloud-mysql mysql -uroot -p
```

```sql
CREATE DATABASE dufeng DEFAULT CHARSET utf8mb4;
CREATE USER 'dufeng'@'%' IDENTIFIED BY '换成强密码';
GRANT ALL PRIVILEGES ON dufeng.* TO 'dufeng'@'%';
FLUSH PRIVILEGES;
```

**2. 新增 Redis 容器**（先确认 6379 未被占用：`docker ps | grep redis`）：

```bash
docker run -d --name dufeng-redis --restart unless-stopped \
  -p 127.0.0.1:6379:6379 \
  -v dufeng-redis-data:/data \
  redis:7 redis-server \
  --requirepass '换成强密码' \
  --appendonly yes \
  --maxmemory 256mb --maxmemory-policy volatile-ttl
```

若 6379 已被其他项目占用，改用 `-p 127.0.0.1:6380:6379`，后端环境变量 `REDIS_PORT=6380` 即可。

**3. 后端连接参数**（见第二节 start.sh）：`DB_HOST=127.0.0.1`、`DB_PORT=3306`、`DB_USERNAME=dufeng`、`DB_PASSWORD=上面设置的密码`、`DB_NAME=dufeng`、`REDIS_HOST=127.0.0.1`、`REDIS_PASSWORD=上面设置的密码`。

> ⚠️ **顺带安全提醒**：`docker ps` 显示你的 MySQL 绑定在 `0.0.0.0:3306`、Neo4j 在 `0.0.0.0:7474/7687`，
> 即公网可直连。公网暴露的 MySQL/Neo4j 是扫描器和勒索挖矿的重点目标。建议到云控制台安全组检查：
> 3306、7474、7687、Jenkins 的 8080 等端口如果对 0.0.0.0/0 开放，改为只放行你自己的常用 IP；
> 若 dcloud 项目也是同机访问，可以直接把容器端口绑定改成 `127.0.0.1:3306:3306`（改绑定前确认没有外部程序在远程连它，
> 可用 `SELECT host, user FROM mysql.user;` 和连接列表判断）。

### 方案 B：独立部署 MySQL + Redis（服务器上没有可用 MySQL 时）

### 1. 安装 Docker（Ubuntu/Debian）

```bash
curl -fsSL https://get.docker.com | sh
sudo systemctl enable --now docker
docker compose version   # 确认 compose 插件可用
```

CentOS/openEuler 同理，或使用系统源 `dnf install docker-ce docker-compose-plugin`。

### 2. 准备密码配置

在 `deploy/` 目录创建 `.env`（**不要提交到 git**，.gitignore 建议加入 `deploy/.env`）：

```bash
cd deploy
cat > .env <<'EOF'
MYSQL_ROOT_PASSWORD=换成强密码
MYSQL_DATABASE=dufeng
REDIS_PASSWORD=换成强密码
EOF
chmod 600 .env
```

### 3. 启动

```bash
docker compose up -d
docker compose ps          # 两个容器应为 healthy
docker compose logs mysql  # 排查问题时看日志
```

安全要点：
- 3306/6379 已绑定 `127.0.0.1`，公网无法直连；**云安全组也无需放行这两个端口**。
- Redis 设置了 requirepass 且开启 AOF 持久化。
- 数据落在 named volume（`mysql-data` / `redis-data`），可用 `docker volume ls` 查看。

### 4. 常用运维命令

```bash
docker compose down            # 停止（数据保留）
docker compose up -d           # 启动
docker exec -it dufeng-mysql mysql -uroot -p   # 进入 MySQL
docker exec -it dufeng-redis redis-cli -a '你的Redis密码'   # 进入 Redis
```

## 二、部署后端

### 1. 准备密钥

```bash
# JWT 签名密钥（Base64，至少 48 字节随机）
openssl rand -base64 48
# 支付回调签名密钥
openssl rand -hex 32
```

### 2. 打包上传

本地执行：

```bash
cd backend
mvn clean package -DskipTests
scp target/dufeng-ecommerce-backend.jar user@服务器IP:/opt/dufeng/
```

### 3. 启动脚本 `/opt/dufeng/start.sh`

```bash
#!/bin/bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=127.0.0.1
export DB_PORT=3306
export DB_NAME=dufeng
export DB_USERNAME=root
export DB_PASSWORD='你的MySQL密码'
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
export REDIS_PASSWORD='你的Redis密码'
export JWT_SECRET='上面生成的JWT密钥'
export PAY_SECRET='上面生成的支付密钥'
export PAY_MOCK_CALLBACK=false
export CORS_ALLOWED_ORIGINS=''   # 前后端同域部署（Nginx 反代 /api）时留空即可
export SERVER_PORT=8080

exec java -Xms512m -Xmx1g -jar /opt/dufeng/dufeng-ecommerce-backend.jar
```

```bash
chmod +x /opt/dufeng/start.sh
```

> 生产 profile 下 Swagger 已关闭；所有凭证只从环境变量读取，缺一个都会**启动即失败**（快速失败优于弱默认值），这是有意设计。

### 4. systemd 托管 `/etc/systemd/system/dufeng.service`

```ini
[Unit]
Description=Dufeng E-commerce Backend
After=network.target docker.service

[Service]
User=www
ExecStart=/opt/dufeng/start.sh
Restart=always
RestartSec=5
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now dufeng
sudo systemctl status dufeng
journalctl -u dufeng -f   # 看日志
```

首次启动 Flyway 会自动执行 `V1__init.sql`、`V2__audit_log.sql` 建表，无需手工导入。

### 5. 创建生产管理员账号

演示账号（admin/admin123）**只在 dev 环境自动创建**。生产环境用以下方式生成首个管理员：

```bash
# 生成 BCrypt 密码哈希（用 htpasswd，或本地 dev 启动一次后从 t_user 表复制）
sudo apt install apache2-utils
htpasswd -bnBC 10 "" '你的管理员密码' | tr -d ':\n'
# 插入管理员（注意把哈希中的 $ 转义后再进 shell，或用客户端工具执行）
docker exec -it dufeng-mysql mysql -uroot -p dufeng -e \
  "INSERT INTO t_user(id, username, password_hash, nickname, gender, status) VALUES (1, 'admin', '<上面的哈希>', '平台管理员', 0, 1);"
```

## 三、部署前端（Nginx）

本地 `frontend/` 下 `npm run build`，把 `dist/` 上传到服务器 `/opt/dufeng/dist`，Nginx 配置：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /opt/dufeng/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;   # SPA history 路由
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

前后端同域走 Nginx 反代，浏览器不跨域，后端 CORS 白名单留空即可。

## 四、4c4g 资源分配参考

| 组件 | 内存 | 说明 |
|---|---|---|
| MySQL | ~700M | buffer pool 512M（compose 已配置） |
| Redis | ~280M | maxmemory 256mb（compose 已配置） |
| 后端 JVM | 1g | -Xms512m -Xmx1g |
| 系统/其他 | 其余 | 建议开启 2G swap 兜底：`fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile` |

## 五、上线前检查清单

- [ ] `deploy/.env`、`/opt/dufeng/start.sh` 权限 600，不入 git
- [ ] 安全组仅放行 80/443（和 SSH），3306/6379 未放行
- [ ] `JWT_SECRET`、`PAY_SECRET`、DB/Redis 密码均为随机强密钥
- [ ] `SPRING_PROFILES_ACTIVE=prod`、`PAY_MOCK_CALLBACK=false`
- [ ] `https://your-domain/v3/api-docs` 返回 404（Swagger 已关）
- [ ] 未登录请求 `/api/portal/user/me` 返回 401；普通用户请求 `/api/admin/**` 返回 403
- [ ] 首个管理员账号密码非弱口令
