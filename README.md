# Keycloak 管理工具 | Keycloak Manager | Keycloak マネージャー

<p align="center">
  <a href="#中文">中文</a> | <a href="#english">English</a> | <a href="#日本語">日本語</a>
</p>

---

<a name="中文"></a>
## 🇨🇳 中文

一个基于 Java Swing 的 Keycloak 管理客户端，提供图形化界面来管理 Keycloak 的客户端和用户。

**运行方式**: `java -jar keycloak_manager_tool-1.0-SNAPSHOT.jar`

### 功能特性

#### 连接管理
- 支持保存多个 Keycloak 连接配置
- 启动时选择连接配置
- 支持跳过 SSL/TLS 证书验证（适用于自签名证书）
- 配置本地加密存储
- **支持 HTTP/HTTPS 代理配置**
- **支持用户名密码和客户端凭据两种登录方式**

<img width="500" height="400" alt="连接配置" src="https://github.com/user-attachments/assets/9ce9e19a-6c9f-48be-a7ac-e0366bf1b0ac" />

#### 客户端管理
- 查看客户端列表
- 新增客户端
- 编辑客户端属性
- 删除客户端
- 双击查看客户端详情（支持表格/文本两种显示模式）

<img width="1200" height="800" alt="客户端管理" src="https://github.com/user-attachments/assets/c7a9f44c-161e-45f7-b6ac-50f1b9b50163" />

#### 用户管理
- 用户列表分页显示
- **支持跳转到指定页**
- 多条件搜索（ID、用户名、邮箱、自定义属性）
- 支持精确匹配和模糊查询
- 新增/编辑/删除用户
- 管理用户自定义属性
- 双击查看用户详情

<img width="1184" height="804" alt="用户管理" src="https://github.com/user-attachments/assets/ad6af9d1-4e5e-4a7f-a5b3-a172f1c651c4" />

### 技术栈
- Java 11+
- Gradle 9.0
- Swing GUI
- Keycloak Admin Client 26.0.8

### 快速开始

```bash
# 构建项目
./gradlew clean jar

# 运行程序
java -jar build/libs/keycloak_manager_tool-1.0-SNAPSHOT.jar
```

### 使用说明
1. **首次启动**：创建新的 Keycloak 连接配置
2. **连接配置**：支持基本配置、认证配置、代理配置、SSL配置
3. **测试连接**：在连接前可以先测试连接是否成功
4. **管理客户端**：增删改查客户端
5. **管理用户**：搜索、分页、设置自定义属性

### 配置存储
连接配置存储在用户主目录下的 `.keycloak_manager` 文件夹中。

---

<a name="english"></a>
## 🇺🇸 English

A Java Swing-based Keycloak management client providing a graphical interface to manage Keycloak clients and users.

**Run**: `java -jar keycloak_manager_tool-1.0-SNAPSHOT.jar`

### Features

#### Connection Management
- Save multiple Keycloak connection configurations
- Select configuration at startup
- Skip SSL/TLS certificate validation (for self-signed certificates)
- Local encrypted storage for configurations
- **HTTP/HTTPS proxy support**
- **Username/password and client credentials authentication**

<img width="500" height="400" alt="connection" src="https://github.com/user-attachments/assets/9ce9e19a-6c9f-48be-a7ac-e0366bf1b0ac" />

#### Client Management
- View client list
- Create new clients
- Edit client properties
- Delete clients
- Double-click to view client details (table/text modes)

<img width="1200" height="800" alt="clients" src="https://github.com/user-attachments/assets/c7a9f44c-161e-45f7-b6ac-50f1b9b50163" />

#### User Management
- Paginated user list
- **Jump to specific page**
- Multi-condition search (ID, username, email, custom attributes)
- Exact match and fuzzy query support
- Create/Edit/Delete users
- Manage custom user attributes

<img width="1184" height="804" alt="users" src="https://github.com/user-attachments/assets/ad6af9d1-4e5e-4a7f-a5b3-a172f1c651c4" />

### Tech Stack
- Java 11+
- Gradle 9.0
- Swing GUI
- Keycloak Admin Client 26.0.8

### Quick Start

```bash
# Build project
./gradlew clean jar

# Run application
java -jar build/libs/keycloak_manager_tool-1.0-SNAPSHOT.jar
```

### Usage
1. **First Launch**: Create a new Keycloak connection configuration
2. **Connection Config**: Basic, Authentication, Proxy, SSL settings
3. **Test Connection**: Test connectivity before connecting
4. **Manage Clients**: CRUD operations for clients
5. **Manage Users**: Search, pagination, custom attributes

### Configuration Storage
Configurations are stored in `.keycloak_manager` folder in user's home directory.

---

<a name="日本語"></a>
## 🇯🇵 日本語

Java Swing ベースの Keycloak 管理クライアント。Keycloak のクライアントとユーザーを管理するためのグラフィカルインターフェースを提供します。

**実行方法**: `java -jar keycloak_manager_tool-1.0-SNAPSHOT.jar`

### 機能

#### 接続管理
- 複数の Keycloak 接続設定を保存
- 起動時に設定を選択
- SSL/TLS 証明書検証をスキップ（自己署名証明書用）
- 設定のローカル暗号化保存
- **HTTP/HTTPS プロキシ対応**
- **ユーザー名/パスワードとクライアント認証情報の両方に対応**

<img width="500" height="400" alt="接続設定" src="https://github.com/user-attachments/assets/9ce9e19a-6c9f-48be-a7ac-e0366bf1b0ac" />

#### クライアント管理
- クライアント一覧表示
- 新規クライアント作成
- クライアント属性の編集
- クライアントの削除
- ダブルクリックで詳細表示（テーブル/テキストモード）

<img width="1200" height="800" alt="クライアント管理" src="https://github.com/user-attachments/assets/c7a9f44c-161e-45f7-b6ac-50f1b9b50163" />

#### ユーザー管理
- ページ分割されたユーザーリスト
- **指定ページへのジャンプ**
- 複数条件検索（ID、ユーザー名、メール、カスタム属性）
- 完全一致とあいまい検索
- ユーザーの作成/編集/削除
- カスタムユーザー属性の管理

<img width="1184" height="804" alt="ユーザー管理" src="https://github.com/user-attachments/assets/ad6af9d1-4e5e-4a7f-a5b3-a172f1c651c4" />

### 技術スタック
- Java 11+
- Gradle 9.0
- Swing GUI
- Keycloak Admin Client 26.0.8

### クイックスタート

```bash
# プロジェクトのビルド
./gradlew clean jar

# アプリケーションの実行
java -jar build/libs/keycloak_manager_tool-1.0-SNAPSHOT.jar
```

### 使用方法
1. **初回起動**: 新しい Keycloak 接続設定を作成
2. **接続設定**: 基本設定、認証設定、プロキシ設定、SSL設定
3. **接続テスト**: 接続前に接続性をテスト
4. **クライアント管理**: クライアントの CRUD 操作
5. **ユーザー管理**: 検索、ページ分割、カスタム属性

### 設定の保存場所
設定はユーザーのホームディレクトリの `.keycloak_manager` フォルダに保存されます。

---

## GitHub

https://github.com/Rozenone/keycloak_manager_tool

## 作者 | Author | 作者

Rozenone

邮箱 | Email | メール: rozenone@foxmail.com

## 许可证 | License | ライセンス

MIT License
