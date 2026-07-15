# MOP 运维管理平台（后端）

基于 Spring Boot 4.x 的企业级后台管理系统，提供用户权限管理、系统监控、定时任务、代码生成及 AI 智能对话等功能。

---

## 技术栈

| 分类         | 技术                                                  | 版本     |
|------------|-----------------------------------------------------|--------|
| **运行环境**   | JDK                                                 | 17+    |
| **核心框架**   | Spring Boot                                         | 4.0.3  |
| **安全框架**   | Spring Security + JWT                               | 0.12.6 |
| **ORM**    | MyBatis Spring Boot Starter                         | 4.0.1  |
| **数据库**    | SQL Server（Microsoft JDBC Driver）                   | 12.8.1 |
| **连接池**    | Druid                                               | 1.2.28 |
| **多数据源**   | dynamic-datasource（baomidou）                        | 4.5.0  |
| **缓存**     | Redis（Lettuce 客户端）                                  | —      |
| **分页**     | PageHelper                                          | 2.1.1  |
| **定时任务**   | Quartz                                              | —      |
| **JSON**   | Fastjson2                                           | 2.0.61 |
| **API 文档** | SpringDoc OpenAPI 3.0.2 + Knife4j 4.5.0             | —      |
| **代码生成**   | Apache Velocity                                     | 2.3    |
| **Excel**  | Apache POI                                          | 5.3.0  |
| **系统监控**   | OSHI                                                | 6.10.0 |
| **验证码**    | Kaptcha                                             | 2.3.3  |
| **AI 对话**  | LangChain4j（OpenAI / DashScope / DeepSeek / Ollama） | 0.36.2 |
| **用户代理解析** | Yauaa                                               | 8.1.0  |
| **构建工具**   | Maven                                               | 3.6+   |

---

## 项目结构

```
MOP/
├── mop-admin/          # 入口模块：启动类、Controller、配置文件
├── mop-framework/      # 框架核心：安全、AOP、过滤器、全局异常处理
├── mop-system/         # 系统业务：用户、角色、菜单、部门、字典、通知等
├── mop-common/         # 通用工具：注解、枚举、异常、工具类、Redis
├── mop-quartz/         # 定时任务：Quartz 动态任务管理
├── mop-generator/      # 代码生成：Velocity 模板 + 数据库反向工程
├── mop-ai/             # AI 模块：大模型对话（SSE 流式返回），多厂商切换
├── sql/                # 数据库初始化脚本
├── bin/                # 启动/打包/清理脚本
├── logs/               # 日志目录
├── uploadPath/         # 文件上传目录
└── pom.xml             # 根 POM（聚合）
```

### 模块依赖关系

```
mop-admin
  ├── mop-framework
  │     └── mop-system
  │           └── mop-common
  ├── mop-quartz ──→ mop-common
  ├── mop-generator ──→ mop-common
  └── mop-ai ──→ mop-common
```

---

## 内置功能

| 功能        | 说明                           |
|-----------|------------------------------|
| **用户管理**  | 用户是系统操作者，完成用户配置              |
| **部门管理**  | 配置系统组织架构（公司、部门、小组），树形结构      |
| **岗位管理**  | 配置系统用户所属职务                   |
| **菜单管理**  | 配置系统菜单、操作权限、按钮权限标识           |
| **角色管理**  | 角色菜单权限分配、数据权限设置              |
| **字典管理**  | 维护系统中固定不变的数据字典               |
| **参数管理**  | 动态配置系统参数                     |
| **通知公告**  | 发布及维护系统通知公告                  |
| **操作日志**  | 记录系统操作日志及异常信息                |
| **登录日志**  | 记录用户登录日志（含登录 IP、浏览器等）        |
| **在线用户**  | 查看当前在线用户，支持强制下线              |
| **定时任务**  | 在线添加、修改、删除、暂停/恢复 Quartz 定时任务 |
| **代码生成**  | 数据库反向生成前后端 CRUD 代码           |
| **系统监控**  | 实时监控服务器 CPU、内存、磁盘、JVM 等指标    |
| **缓存监控**  | 查看 Redis 缓存信息及键值管理           |
| **AI 对话** | 集成大模型对话，SSE 流式输出，支持多厂商切换     |

---

## 环境要求

- JDK 17+
- Maven 3.6+
- Redis（默认端口 6379）
- SQL Server（默认端口 1433）

---

## 数据库初始化

1. 创建数据库 `mes_ops_platform`
2. 执行 `sql/` 目录下的 SQL 脚本：
    - `mop_initial.sql`（完整建库脚本，含 CREATE DATABASE + 所有表结构 + 初始数据）
    - `mysql_conversion_sqlserver_initial_sql.sql`（仅表结构 + 初始数据，不包含 CREATE DATABASE）
3. 修改 `mop-admin/src/main/resources/application-dev.yml`（开发）或 `application-prod.yml`（生产）中的数据库连接信息

> **注意**：`mop_initial.sql` 中 CREATE DATABASE 的文件路径为本地路径，部署到其他机器时需修改。

---

## 运行与构建

### 方式一：使用 bin/ 脚本（推荐）

| 脚本                 | 用途             | 等价命令                                                    |
|--------------------|----------------|---------------------------------------------------------|
| `bin/package.bat`  | 清理并打包项目（跳过测试）  | `mvn clean package -Dmaven.test.skip=true`              |
| `bin/clean.bat`    | 清理 target 构建目录 | `mvn clean`                                             |
| `bin/run.bat`      | 启动应用（**开发环境**） | `java -jar mop-admin.jar --spring.profiles.active=dev`  |
| `bin/run-prod.bat` | 启动应用（**生产环境**） | `java -jar mop-admin.jar --spring.profiles.active=prod` |

### 方式二：使用 Maven 命令

```bash
# 打包
mvn clean package -Dmaven.test.skip=true

# 启动（开发环境，Swagger 开启）
java -jar mop-admin/target/mop-admin.jar --spring.profiles.active=dev

# 启动（生产环境，Swagger 关闭）
java -jar mop-admin/target/mop-admin.jar --spring.profiles.active=prod
```

---

## 环境切换

通过 `--spring.profiles.active` 参数切换：

| 参数值    | 环境   | Swagger | 日志级别  | 防盗链 |
|--------|------|---------|-------|-----|
| `dev`  | 开发环境 | 开启      | DEBUG | 关闭  |
| `prod` | 生产环境 | 关闭      | WARN  | 开启  |

---

## JVM 参数

`run.bat` 和 `run-prod.bat` 中配置了以下 JVM 参数：

| 参数                     | 默认值   | 说明      |
|------------------------|-------|---------|
| `-Xms`                 | 256m  | 初始堆内存   |
| `-Xmx`                 | 1024m | 最大堆内存   |
| `-XX:MetaspaceSize`    | 128m  | 元空间初始大小 |
| `-XX:MaxMetaspaceSize` | 512m  | 元空间最大大小 |

可根据服务器配置适当调整。

---

## 后端访问地址

| 地址                               | 说明                  |
|----------------------------------|---------------------|
| `http://localhost:8080`          | 应用地址                |
| `http://localhost:8080/doc.html` | Knife4j 接口文档（仅 dev） |
| `http://localhost:8080/druid`    | Druid 监控面板          |

### 默认管理员账号

- 用户名：`admin`
- 密码：`admin123`

---

## AI 对话配置

AI 对话模块支持多厂商切换，配置文件中的 `ai.model` 节点分散在两个层中：

**application.yml（AI 模型公共参数）**：

```yaml
ai:
  model:
    max-tokens: 1024
    temperature: 0.7
    max-history-messages: 5
    system-prompt: "你的名字是「牛牛哥」..."
```

**application-dev.yml / application-prod.yml（环境独有，厂商+密钥）**：

```yaml
ai:
  model:
    provider: openai        # dashscope | openai | deepseek | ollama
    api-key: your-api-key
    model-name: your-model
    base-url: https://api.example.com/v1
```

> Spring Boot 会将两层 `ai.model` 键值自动合并为完整配置。

| 厂商        | provider    | 说明                                |
|-----------|-------------|-----------------------------------|
| 阿里百炼      | `dashscope` | 通义千问系列                            |
| OpenAI 兼容 | `openai`    | GPT-4o、火山引擎 Ark 等兼容 API           |
| DeepSeek  | `deepseek`  | deepseek-chat / deepseek-reasoner |
| Ollama 本地 | `ollama`    | 完全免费，需本地运行 Ollama                 |

---

## 安全特性

| 特性         | 说明                          |
|------------|-----------------------------|
| **认证**     | Spring Security + JWT 无状态认证 |
| **权限**     | RBAC 角色-菜单权限模型，支持按钮级权限控制    |
| **数据权限**   | 基于部门的数据范围过滤                 |
| **XSS 防护** | 请求参数 XSS 过滤                 |
| **防盗链**    | 可配置 Referer 白名单             |
| **密码策略**   | 密码错误次数限制 + 锁定时间             |
| **验证码**    | 支持数学计算 / 字符验证码              |

---

## 国际化

后端支持中英文国际化，资源文件位于 `mop-admin/src/main/resources/i18n/`：

| 文件                          | 语言     |
|-----------------------------|--------|
| `messages.properties`       | 默认（中文） |
| `messages_en_US.properties` | 英文     |

通过 Cookie `language` 或 URL 参数 `?lang=en_US` 切换语言。

---

## 配置文件说明

| 文件                           | 说明                        |
|------------------------------|---------------------------|
| `application.yml`            | 公共配置：框架选型、多数据源路由、Druid 监控 |
| `application-dev.yml`        | 开发环境完整配置（明文，开箱即用）         |
| `application-prod.yml`       | 生产环境完整配置（明文，按需填写）         |
| `logback.xml`                | 日志配置                      |
| `mybatis/mybatis-config.xml` | MyBatis 全局配置              |

## 多数据源配置

项目基于 [baomidou/dynamic-datasource](https://github.com/baomidou/dynamic-datasource) 实现多数据源路由。

### 数据源列表

| 数据源名       | 说明         | 控制方式          |
|------------|------------|---------------|
| `master`   | MOP 主数据源   | 永远启用          |
| `server_a` | A服务器-产线MES | yml 配置 + 字典开关 |
| `server_b` | B服务器-仓储WMS | yml 配置 + 字典开关 |
| `server_c` | C服务器-质量QMS | yml 配置 + 字典开关 |
| `server_d` | D服务器-设备EAM | yml 配置 + 字典开关 |
| `server_e` | E服务器-报表BI  | yml 配置 + 字典开关 |

### 使用方式

```java

@DS("server_a")
public List<Order> getOrders() {
    // 跨库查询：库名.dbo.表名
    return jdbcTemplate.queryForList(
            "SELECT * FROM mes_production.dbo.work_order");
}
```

### 外部数据源开关

通过字典管理 > `datasource_switch` 控制外部数据源启用/停用：

- `dictValue='Y'`：启用
- `dictValue='N'`：停用

修改后调用 `POST /monitor/datasource/reload` 使配置立即生效，无需重启。
