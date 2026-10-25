# 智辨歧黄 - 基于大模型的中医智能问诊平台

> **智辨歧黄** 是一个基于 Spring Boot + MySQL + 大模型技术构建的中医智能问诊平台，旨在通过 AI 技术辅助中医辨证论治，提供个性化的中医健康咨询服务。

---

## 📋 项目简介

本平台集成了中医理论知识库与大语言模型，支持用户录入症状、体征等信息，自动进行证型判定、体质辨识、健康风险评估，并生成个性化的中医调理建议（饮食、运动、情志、穴位、中成药推荐等）。

### 核心功能

- 👤 **用户管理**：注册、登录、个人信息维护（身高、体重、血型、遗传病史、BMI 等）
- 🩺 **智能问诊**：症状采集 → 证型判定 → 体质特征分析 → 健康风险预警 → 个性化调理建议生成
- 📜 **问诊记录**：历史问诊记录查询、详情查看、数据隔离（用户仅可见自己的记录）
- 🤖 **大模型集成**：支持接入主流大模型 API（如 DeepSeek、OpenAI 兼容接口）进行中医推理

---

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 4.0.5 (Spring 7.x) |
| **ORM** | Spring Data JPA / Hibernate |
| **数据库** | MySQL 8.0+ |
| **构建工具** | Maven 3.9+ |
| **JDK** | Java 17+ |
| **前端** | 原生 HTML + CSS + JS (静态资源托管于 Spring Boot) |
| **大模型接入** | RESTful API (OpenAI 兼容格式) |
| **其他** | Lombok, Maven Wrapper |

---

## 📁 项目结构

```
TcmProject
├── src
│   ├── main
│   │   ├── java/com/example/tcmproject
│   │   │   ├── controller/        # REST 控制器
│   │   │   │   └── TcmController.java
│   │   │   ├── entity/            # JPA 实体类
│   │   │   │   ├── User.java
│   │   │   │   ├── ConsultationRecord.java
│   │   │   │   └── ConsultRecord.java
│   │   │   ├── repository/        # 数据访问层
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RecordRepository.java
│   │   │   │   └── Repository.java
│   │   │   └── TcmProjectApplication.java  # 启动类
│   │   └── resources
│   │       ├── application.properties      # 配置文件
│   │       └── static/index.html           # 前端页面
│   └── test
│       └── java/.../TcmProjectApplicationTests.java
├── sql
│   └── init_database.sql        # 数据库初始化脚本
├── .gitignore
├── pom.xml
├── mvnw / mvnw.cmd
└── README.md
```

---

## 🚀 快速开始

### 1. 环境准备

| 软件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | 推荐 Temurin / Oracle JDK |
| Maven | 3.9+ | 项目自带 `mvnw` 可免安装 |
| MySQL | 8.0+ | 需预先创建数据库 |
| Git | 任意 | 克隆代码用 |

### 2. 数据库初始化

#### 方式一：使用初始化脚本（推荐）

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本（会创建数据库、表、基础数据）
source /path/to/TcmProject/sql/init_database.sql;
```

#### 方式二：手动创建（JPA 自动建表）

```sql
-- 仅创建数据库，表由 JPA 自动生成 (spring.jpa.hibernate.ddl-auto=update)
CREATE DATABASE IF NOT EXISTS tcm_platform 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

> ⚠️ **注意**：生产环境建议使用 `init_database.sql` 显式管理表结构，关闭 JPA 自动建表（改为 `validate` 或 `none`）。

### 3. 修改配置文件

编辑 `src/main/resources/application.properties`，修改数据库连接信息：

```properties
# 数据库连接设置
spring.datasource.url=jdbc:mysql://localhost:3306/tcm_platform?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8
spring.datasource.username=root
spring.datasource.password=你的数据库密码

# JPA 配置
spring.jpa.hibernate.ddl-auto=update   # 开发环境用 update，生产建议 validate/none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

### 4. 启动项目

#### 使用 Maven Wrapper（无需安装 Maven）

```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

#### 或使用已安装的 Maven

```bash
mvn spring-boot:run
```

#### 或打包为 JAR 运行

```bash
./mvnw clean package -DskipTests
java -jar target/TcmProject-0.0.1-SNAPSHOT.jar
```

### 5. 访问应用

启动成功后，浏览器访问：

- **前端页面**：<http://localhost:8080>
- **API 基础路径**：<http://localhost:8080/api>

---

## 📚 API 接口文档

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/users/register` | POST | 用户注册 |
| `/api/users/login` | POST | 用户登录 |
| `/api/users/{id}` | GET | 获取用户信息 |
| `/api/users/{id}` | PUT | 更新用户信息 |
| `/api/consult` | POST | 智能问诊（核心接口） |
| `/api/records/user/{userId}` | GET | 查询用户问诊记录列表 |
| `/api/records/{id}` | GET | 查询问诊记录详情 |

> 详细请求/响应示例请参考 `TcmController.java` 源码。

---

## 🗄 数据库设计

### 核心表结构

#### `users` - 用户表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(100) | 密码（建议加密存储） |
| nickname | VARCHAR(50) | 昵称 |
| height | DOUBLE | 身高(cm) |
| weight | DOUBLE | 体重(kg) |
| blood_type | VARCHAR(10) | 血型 |
| genetic_history | VARCHAR(500) | 遗传病史 |
| bmi | DOUBLE | BMI指数 |

#### `consultation_records` - 问诊记录表 (JPA 关联版)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 外键关联 users.id |
| symptoms | VARCHAR(1000) | 主诉症状 |
| diagnosis_type | VARCHAR(100) | 证型判定 |
| features | VARCHAR(500) | 体质特征 |
| risks | VARCHAR(500) | 健康风险 |
| advice | TEXT | 调理建议 |
| consult_time | DATETIME | 问诊时间 |

#### `consult_records` - 问诊记录表 (轻量版)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键，自增 |
| user_id | BIGINT | 关联用户ID |
| consult_time | VARCHAR(50) | 问诊时间字符串 |
| diag_type | VARCHAR(100) | 证型判定 |
| feature | VARCHAR(500) | 体质特征 |
| risk | VARCHAR(500) | 健康风险 |
| advice | TEXT | 调理建议 |

---

## ⚙️ 大模型配置

项目通过调用大模型 API 实现中医推理。需在 `TcmController.java` 或配置文件中设置：

```java
// 示例：DeepSeek API 配置
String apiKey = "your-api-key";
String baseUrl = "https://api.deepseek.com/v1/chat/completions";
// 或兼容 OpenAI 格式的其他服务
```

> 建议将 API Key 通过环境变量或配置中心注入，避免硬编码。

---

## 🧪 测试

```bash
# 运行单元测试
./mvnw test

# 运行测试并生成报告
./mvnw test jacoco:report
```

---

## 📦 打包部署

```bash
# 打包生成可执行 JAR
./mvnw clean package -DskipTests

# 运行
java -jar target/TcmProject-0.0.1-SNAPSHOT.jar \
  --spring.datasource.url=jdbc:mysql://prod-db:3306/tcm_platform \
  --spring.datasource.username=prod_user \
  --spring.datasource.password=prod_pass \
  --spring.jpa.hibernate.ddl-auto=validate
```

### Docker 部署 (可选)

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/TcmProject-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
```

```bash
docker build -t tcm-project .
docker run -d -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/tcm_platform \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=1234 \
  tcm-project
```

---

## 🔧 常见问题

| 问题 | 解决方案 |
|------|----------|
| 启动报 `Communications link failure` | 检查 MySQL 是否启动、端口 3306 是否开放、防火墙设置 |
| 表未自动创建 | 确认 `spring.jpa.hibernate.ddl-auto=update`，且数据库用户有 CREATE 权限 |
| 中文乱码 | 确保 MySQL 使用 `utf8mb4`，连接 URL 包含 `characterEncoding=utf-8` |
| 端口 8080 被占用 | 修改 `server.port=8081` 或杀掉占用进程 |

---

## 📄 许可证

本项目采用 MIT License 开源，详见 [LICENSE](LICENSE) 文件。

---

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -m 'Add your feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 提交 Pull Request

---

## 📞 联系方式

- **项目主页**：<https://github.com/waitingr/TcmProject>
- **问题反馈**：[Issues](https://github.com/waitingr/TcmProject/issues)

---

> **免责声明**：本平台提供的中医建议仅供参考，不能替代专业医生诊断。如有不适，请及时就医。