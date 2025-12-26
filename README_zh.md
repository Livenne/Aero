# Aero 框架

Aero 是一个轻量级、快速启动的 Java Web 框架，提供了类似 Spring Boot 的开发体验，但具有更小的体积和更快的启动速度。该框架采用模块化架构设计，内置了完整的依赖注入、MVC 和 ORM 功能。

## 🌐 语言

- [English README](README.md)
- [中文说明](README_zh.md)

## 🚀 核心特性

- **轻量级架构**：快速启动，资源占用少
- **模块化设计**：基于模块系统，可灵活扩展功能
- **自动依赖注入**：基于注解的自动装配机制
- **MVC 架构支持**：控制器、服务层、数据访问层完整实现
- **内置 ORM**：支持数据库操作，自动生成表结构
- **全局异常处理**：统一异常处理机制
- **拦截器支持**：可自定义请求拦截处理
- **嵌入式 Tomcat**：内置 Web 服务器，无需外部部署
- **可扩展模块系统**：框架和应用级模块可以创建并自动加载

## 📦 项目架构

Aero 框架采用模块化架构设计，主要包含以下模块：

### 1. 核心模块
- [Application](./aero-core/src/main/java/io/github/livenne/Application.java)：应用程序入口点
- [ApplicationContext](./aero-core/src/main/java/io/github/livenne/ApplicationContext.java)：应用上下文管理
- [BeanFactory](./aero-core/src/main/java/io/github/livenne/BeanFactory.java)：依赖注入容器

### 2. Servlet 模块
- [ServletModule](./aero-core/src/main/java/io/github/livenne/module/servlet/ServletModule.java)：处理 HTTP 请求和路由
- [ServiceServlet](./aero-core/src/main/java/io/github/livenne/module/servlet/ServiceServlet.java)：核心 Servlet 实现
- [RouterMapping](./aero-core/src/main/java/io/github/livenne/module/servlet/RouterMapping.java)：路由映射管理
- [TomcatServer](./aero-core/src/main/java/io/github/livenne/module/servlet/TomcatServer.java)：嵌入式 Tomcat 服务器

### 3. ORM 模块
- [ORMModule](./aero-core/src/main/java/io/github/livenne/module/orm/ORMModule.java)：ORM 功能管理
- [SQLMethodProxy](./aero-core/src/main/java/io/github/livenne/module/orm/SQLMethodProxy.java)：SQL 方法代理
- [SQLUtils](./aero-core/src/main/java/io/github/livenne/utils/SQLUtils.java)：SQL 工具类

### 4. 模块系统
模块系统允许框架级和应用级模块在初始化期间自动加载。框架模块如 [ServletModule](./aero-core/src/main/java/io/github/livenne/module/servlet/ServletModule.java) 和 [ORMModule](./aero-core/src/main/java/io/github/livenne/module/orm/ORMModule.java) 会自动加载。开发人员也可以通过实现 [Module](./aero-core/src/main/java/io/github/livenne/Module.java) 接口创建自定义模块。

## 🛠️ 快速开始

### 1. 添加依赖

在您的 `build.gradle.kts` 文件中：

```kotlin
dependencies {
    implementation("io.github.livenne:aero:1.0.4")
    // 或使用 Bill of Materials (BOM)
    implementation(platform("io.github.livenne:aero-bom:1.0.4"))
    implementation("io.github.livenne:aero")
}
```

或者在 Maven 的 `pom.xml` 中：

```xml
<dependency>
    <groupId>io.github.livenne</groupId>
    <artifactId>aero</artifactId>
    <version>1.0.4</version>
</dependency>

<!-- 或使用 Bill of Materials (BOM) -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.livenne</groupId>
            <artifactId>aero-bom</artifactId>
            <version>1.0.4</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.livenne</groupId>
        <artifactId>aero</artifactId>
    </dependency>
</dependencies>
```

### 2. 创建启动类

```java
import io.github.livenne.Application;

public class Main {
    public static void main(String[] args) {
        Application.run(Main.class);
    }
}
```

### 3. 配置应用属性 (可选)

在 `resources` 目录下创建 `application.properties`：

```properties
database.url=jdbc:mysql://localhost:3306/wanmall
database.driver=com.mysql.cj.jdbc.Driver
database.username=root
database.password=root
server.port=8080
```

## 🔧 核心功能详解

### 控制器

使用 `@Controller` 注解定义控制器，支持多种请求映射：

```java
import io.github.livenne.ResponseEntity;
import io.github.livenne.annotation.servlet.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller("/test")
public class TestController {
    @GetMapping("/hello")
    public ResponseEntity hello(){
        return ResponseEntity.ok("Hello Aero");
    }

    @PostMapping("/hello/{n}")
    public ResponseEntity helloDemo(@PathVariable("n") Integer n){
        return ResponseEntity.ok(String.format("Hello x%d",n));
    }

    @GetMapping("/parm")
    public ResponseEntity helloParm(@RequestParm("name") String name){
        return ResponseEntity.ok(String.format("Hello %s", name));
    }

    @PostMapping("/form")
    public ResponseEntity helloForm(@RequestBody Map<String, String> map) {
        return ResponseEntity.ok(map);
    }

    @PostMapping("/attribute")
    public ResponseEntity helloA(@Attribute("userId") Long userId) {
        return ResponseEntity.ok(String.format("Hello user: %d", userId));
    }

    @GetMapping("/serve")
    public ResponseEntity helloB(@Request HttpServletRequest req, @Response HttpServletResponse res) {
        log.info(req.getRequestURI());
        res.addCookie(new Cookie("Name","Tom"));
        return ResponseEntity.ok();
    }
    
    @PostConstruct
    public void postConstruct(){
        log.info("Test Controller init ok");
    }

    @PreDestroy
    public void preDestroy() {
        log.info("Test Controller destroy");
    }
}
```

### 服务层

使用 `@Service` 注解标记服务类：

```java
import io.github.livenne.annotation.context.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // 实现登录逻辑
    }

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        // 实现注册逻辑
    }
}
```

### 数据访问层

Repository 必须为接口并继承 [BaseMapper](./aero-core/src/main/java/io/github/livenne/BaseMapper.java)，使用 ORM 注解进行数据库操作：

```java
import io.github.livenne.annotation.orm.*;
import java.util.List;

@Repository
public interface UserRepository extends BaseMapper<User> {
    @Insert
    Long save(UserSaveDTO userSaveDTO);

    @Query
    User getById(@Cond("id") Long id);

    @Query
    User getByUsername(@Cond("username") String username);

    @Query
    User getByEmail(@Cond("email") String email);

    @Update
    void updateScore(@Cond("id") Long id, @Column("score") Long score);

    @Update
    void updateNickname(@Cond("id") Long id, @Column("nickname") String nickname);

    @Update
    void updatePassword(@Cond("id") Long id, @Column("password") String password);

    @Update
    void updateAvatar(@Cond("id") Long id, @Column("avatar") String url);

    @Update
    void updateDefAddr(@Cond("id") Long id, @Column("addr") Long addr);

    @Query
    List<User> getUserList();

    @Delete
    void delete(@Cond("id") Long id);

    @Update
    void update(@Cond("id") Long id, @Column UserSaveDTO userSaveDTO);
}

// 实体类定义
import io.github.livenne.IdType;
import io.github.livenne.annotation.orm.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity("user")
public class User {
    @Id(IdType.AUTO)
    private Long id;
    private Long addr;
    private Long level;
    private Long score;
    private String email;
    private String avatar;
    private String username;
    private String nickname;
    private String password;
}
```

### 全局异常处理

使用 `@ControllerAdvice` 和 `@ExceptionHandler` 实现全局异常处理：

```java
import io.github.livenne.ResponseEntity;
import io.github.livenne.annotation.servlet.ControllerAdvice;
import io.github.livenne.annotation.servlet.ExceptionHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity exception(Exception e) {
        log.error(e.getMessage(),e.getCause());
        return ResponseEntity.err(e.getMessage());
    }
}
```

### 拦截器

使用 `@Interceptor` 注解定义拦截器：

```java
import io.github.livenne.annotation.servlet.Interceptor;

@Interceptor
public class AllFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request,response);
    }
}
```

### 创建自定义模块

Aero 提供了模块系统，允许您扩展框架功能。要创建自定义模块，实现 [Module](./aero-core/src/main/java/io/github/livenne/Module.java) 接口：

```java
import io.github.livenne.Module;
import io.github.livenne.annotation.context.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomModule implements Module {
    @Override
    public void load(Application application) {
        log.info("Loading custom module...");
        // 在这里初始化您的自定义功能
        // 此方法在应用程序启动时调用
    }
}
```

当您创建一个实现 [Module](./aero-core/src/main/java/io/github/livenne/Module.java) 接口的类并使用 `@Component` 注解标记时，它将在应用程序初始化期间自动发现并加载。这允许您在应用程序启动时扩展框架的功能或初始化自定义服务。

## 📚 注解参考

### 控制器相关注解
- `@Controller` - 定义控制器类
- `@GetMapping` - 定义 GET 请求映射
- `@PostMapping` - 定义 POST 请求映射
- `@PutMapping` - 定义 PUT 请求映射
- `@DeleteMapping` - 定义 DELETE 请求映射
- `@PathVariable` - 从 URL 路径中提取参数
- `@RequestParm` - 从请求参数中提取值
- `@RequestBody` - 从请求体中提取 JSON 数据
- `@Attribute` - 从请求属性中提取值

### 服务相关注解
- `@Service` - 定义服务类
- `@Autowired` - 自动装配依赖
- `@Value` - 注入配置值

### ORM 相关注解
- `@Repository` - 定义数据访问层接口
- `@Entity` - 定义实体类
- `@Id` - 定义主键
- `@Query` - 定义查询语句
- `@Insert` - 定义插入语句
- `@Update` - 定义更新语句
- `@Delete` - 定义删除语句
- `@Cond` - 定义条件参数
- `@Column` - 定义列映射

### 其他注解
- `@ControllerAdvice` - 全局异常处理类
- `@ExceptionHandler` - 异常处理方法
- `@Interceptor` - 拦截器类
- `@Component` - 标记一个类为组件，由框架管理

## 🌟 实际应用案例

Aero 框架已被成功应用于实际项目中，例如 WanMall 电商后端系统，展示了框架在实际开发中的能力：

- 电商商品管理
- 用户认证系统
- 订单处理系统
- 支付接口集成
- 用于特定功能的自定义模块扩展

## 🤝 贡献

欢迎提交 PR 和 Issue 来帮助改进 Aero 框架！

## 📄 许可证

Aero 框架使用 Apache 2.0 许可证，详情请见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

如果您有任何问题或建议，请随时联系：
- 邮箱: livennea@gmail.com
- GitHub: https://github.com/Livenne/Aero