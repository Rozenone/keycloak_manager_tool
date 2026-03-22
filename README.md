# Keycloak 管理工具

一个基于 Java Swing 的 Keycloak 管理客户端，提供图形化界面来管理 Keycloak 的客户端和用户。
打开方式: java -jar keycloak_manager_tool-1.0-SNAPSHOT.jar

## 功能特性

### 连接管理
- 支持保存多个 Keycloak 连接配置
- 启动时选择连接配置
- 支持跳过 SSL/TLS 证书验证（适用于自签名证书）
- 配置本地加密存储

### 客户端管理
- 查看客户端列表
- 新增客户端
- 编辑客户端属性
- 删除客户端
- 双击查看客户端详情（支持表格/文本两种显示模式）

### 用户管理
- 用户列表分页显示
- 多条件搜索（ID、用户名、邮箱、自定义属性）
- 支持精确匹配和模糊查询
- 新增/编辑/删除用户
- 管理用户自定义属性
- 双击查看用户详情（支持表格/文本两种显示模式）
- 在首页列表显示用户表示名（displayName）

### 界面特性
- 错误弹窗文字可复制
- 详情页支持表格/文本两种显示模式
- 文本模式下可自由复制内容

## 技术栈

- Java 17+
- Gradle 9.0
- Swing GUI
- Keycloak Admin Client 26.0.8

## 快速开始

### 构建项目

```bash
./gradlew clean jar
```

### 运行程序

```bash
java -jar build/libs/keycloak_manager_tool-1.0-SNAPSHOT.jar
```

### 使用说明

1. **首次启动**：
   - 程序会弹出配置选择对话框
   - 点击"新增配置"创建新的 Keycloak 连接
   - 输入服务器地址、Realm、用户名和密码
   - 如果使用的是自签名证书，勾选"跳过 SSL 验证"

2. **连接 Keycloak**：
   - 选择已保存的配置
   - 点击"连接"按钮

3. **管理客户端**：
   - 切换到"客户端管理"标签页
   - 可以进行增删改查操作

4. **管理用户**：
   - 切换到"用户管理"标签页
   - 使用搜索框查找用户
   - 支持分页浏览
   - 可以设置用户的自定义属性（如 displayName）

## 配置存储

连接配置存储在用户主目录下的 `.keycloak_manager` 文件夹中，采用 Base64 编码保护密码等敏感信息。

存储位置：
- Windows: `C:\Users\<用户名>\.keycloak_manager\`
- Linux/Mac: `~/.keycloak_manager/`

## 注意事项

1. **SSL 证书**：如果 Keycloak 服务器使用自签名证书或 IP 地址访问，请勾选"跳过 SSL 验证"选项

2. **用户属性**：
   - 自定义属性以键值对形式存储
   - 支持多值属性（用逗号分隔）
   - displayName 属性会在首页用户列表中显示

3. **分页设置**：用户列表支持每页 10/20/50/100 条记录切换

## 常见问题

### 无法连接到 Keycloak
- 检查服务器地址是否正确（包含 http:// 或 https://）
- 确认 Realm 名称正确
- 检查用户名和密码
- 如果是自签名证书，勾选"跳过 SSL 验证"

### 用户属性设置后无法显示
- 确保属性值不为空
- 重新查询用户列表刷新数据
- 检查 Keycloak 版本兼容性

## GitHub

https://github.com/Rozenone/keycloak_manager_tool

## 作者

Rozenone

邮箱：rozenone@foxmail.com

## 许可证

MIT License
