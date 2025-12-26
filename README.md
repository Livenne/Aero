# Aero Framework

Aero is a lightweight, fast-starting Java Web framework that provides a Spring Boot-like development experience with a smaller footprint and faster startup time. The framework features a modular architecture design with built-in dependency injection, MVC, and ORM capabilities.

## 🌐 Language

- [English README](README.md)
- [中文说明](README_zh.md)

## 🚀 Core Features

- **Lightweight Architecture**: Fast startup with minimal resource consumption
- **Modular Design**: Module-based system for flexible feature expansion
- **Automatic Dependency Injection**: Annotation-based auto-wiring mechanism
- **MVC Architecture Support**: Complete implementation of controller, service, and data access layers
- **Built-in ORM**: Database operation support with automatic table structure generation
- **Global Exception Handling**: Unified exception handling mechanism
- **Interceptor Support**: Customizable request interception handling
- **Embedded Tomcat**: Built-in web server, no external deployment required
- **Extensible Module System**: Framework and application-level modules can be created and automatically loaded

## 📦 Project Architecture

Aero framework adopts a modular architecture design, primarily consisting of the following modules:

### 1. Core Module
- [Application](./aero-core/src/main/java/io/github/livenne/Application.java): Application entry point
- [ApplicationContext](./aero-core/src/main/java/io/github/livenne/ApplicationContext.java): Application context management
- [BeanFactory](./aero-core/src/main/java/io/github/livenne/BeanFactory.java): Dependency injection container

### 2. Servlet Module
- [ServletModule](./aero-core/src/main/java/io/github/livenne/module/servlet/ServletModule.java): HTTP request and routing handler
- [ServiceServlet](./aero-core/src/main/java/io/github/livenne/module/servlet/ServiceServlet.java): Core Servlet implementation
- [RouterMapping](./aero-core/src/main/java/io/github/livenne/module/servlet/RouterMapping.java): Route mapping management
- [TomcatServer](./aero-core/src/main/java/io/github/livenne/module/servlet/TomcatServer.java): Embedded Tomcat server

### 3. ORM Module
- [ORMModule](./aero-core/src/main/java/io/github/livenne/module/orm/ORMModule.java): ORM functionality management
- [SQLMethodProxy](./aero-core/src/main/java/io/github/livenne/module/orm/SQLMethodProxy.java): SQL method proxy
- [SQLUtils](./aero-core/src/main/java/io/github/livenne/utils/SQLUtils.java): SQL utility class

### 4. Module System
The Module system allows both framework-level and application-level modules to be automatically loaded during initialization. Framework modules like [ServletModule](./aero-core/src/main/java/io/github/livenne/module/servlet/ServletModule.java) and [ORMModule](./aero-core/src/main/java/io/github/livenne/module/orm/ORMModule.java) are automatically loaded. Developers can also create custom modules by implementing the [Module](./aero-core/src/main/java/io/github/livenne/Module.java) interface.

## 🛠️ Quick Start

### 1. Add Dependencies

In your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.livenne:aero:1.0.4")
    // Or use Bill of Materials (BOM)
    implementation(platform("io.github.livenne:aero-bom:1.0.4"))
    implementation("io.github.livenne:aero")
}
```

Or in your Maven `pom.xml`:

```xml
<dependency>
    <groupId>io.github.livenne</groupId>
    <artifactId>aero</artifactId>
    <version>1.0.4</version>
</dependency>

<!-- Or use Bill of Materials (BOM) -->
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

### 2. Create Startup Class

```java
import io.github.livenne.Application;

public class Main {
    public static void main(String[] args) {
        Application.run(Main.class);
    }
}
```

### 3. Configure Application Properties (Optional)

Create `application.properties` in the `resources` directory:

```properties
server.port=8080
```

## 🔧 Core Functionality Explained

### Controllers

Use the `@Controller` annotation to define controllers, supporting multiple request mappings:

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

### Services

Use the `@Service` annotation to mark service classes:

```java
import io.github.livenne.annotation.context.Service;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public User login(UserLoginDTO userLoginDTO) {
        // Implement login logic
    }

    @Override
    public User register(UserRegisterDTO userRegisterDTO) {
        // Implement registration logic
    }
}
```

### Data Access Layer

Repositories must be interfaces extending [BaseMapper](./aero-core/src/main/java/io/github/livenne/BaseMapper.java), with ORM annotations for database operations:

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

// Entity class definition
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

### Global Exception Handling

Use `@ControllerAdvice` and `@ExceptionHandler` for global exception handling:

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

### Interceptors

Use the `@Interceptor` annotation to define interceptors:

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

### Creating Custom Modules

Aero provides a module system that allows you to extend framework functionality. To create a custom module, implement the [Module](./aero-core/src/main/java/io/github/livenne/Module.java) interface:

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
        // Initialize your custom functionality here
        // This method is called during application startup
    }
}
```

When you create a class that implements the [Module](./aero-core/src/main/java/io/github/livenne/Module.java) interface and mark it with `@Component` annotation, it will be automatically discovered and loaded during application initialization. This allows you to extend the framework's functionality or initialize custom services when the application starts.

## 📚 Annotation Reference

### Controller-related Annotations
- `@Controller` - Defines a controller class
- `@GetMapping` - Defines GET request mapping
- `@PostMapping` - Defines POST request mapping
- `@PutMapping` - Defines PUT request mapping
- `@DeleteMapping` - Defines DELETE request mapping
- `@PathVariable` - Extracts parameters from URL path
- `@RequestParm` - Extracts values from request parameters
- `@RequestBody` - Extracts JSON data from request body
- `@Attribute` - Extracts values from request attributes

### Service-related Annotations
- `@Service` - Defines a service class
- `@Autowired` - Auto-wires dependencies
- `@Value` - Injects configuration values

### ORM-related Annotations
- `@Repository` - Defines a data access layer interface
- `@Entity` - Defines an entity class
- `@Id` - Defines a primary key
- `@Query` - Defines a query statement
- `@Insert` - Defines an insert statement
- `@Update` - Defines an update statement
- `@Delete` - Defines a delete statement
- `@Cond` - Defines a condition parameter
- `@Column` - Defines a column mapping

### Other Annotations
- `@ControllerAdvice` - Global exception handling class
- `@ExceptionHandler` - Exception handling method
- `@Interceptor` - Interceptor class
- `@Component` - Marks a class as a component to be managed by the framework

## 🌟 Real-World Application Example

The Aero framework has been successfully applied to real projects, such as the WanMall e-commerce backend system, demonstrating the framework's capabilities in practical development:

- E-commerce product management
- User authentication system
- Order processing system
- Payment interface integration
- Custom module extensions for specialized functionality

## 🤝 Contributing

Contributions via PRs and Issues are welcome to help improve the Aero framework!

## 📄 License

Aero framework is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details.

## 📞 Contact

If you have any questions or suggestions, feel free to contact:
- Email: livennea@gmail.com
- GitHub: https://github.com/Livenne/Aero