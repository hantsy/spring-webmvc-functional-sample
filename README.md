# Functional programming in Spring MVC
Spring 5.0 embraced ReactiveStreams specification, introduced a new Reactive Stack as an alternative of traditional Servlet stack.  And it also  brought a new functional programming model to developers. But it only supported Reactive stack.

The good news is that in the incoming 5.2 the functional like APIs are being ported back to the Servlet stack. For those developers who are stick on Servlet stack and want to experience the new programming model, it is absolutely a startling news.

In this post, let's  take a glance at the new functional feature in Spring MVC.

Create a Spring Boot project using Spring initializr(http://start.spring.io), add **Web**, **JPA**,  **Lombok**,  and **H2**  starters as dependencies. 

> NOTE: Please select the new Spring Boot version  2.2.0.BUILD-SNAPSHOT to get the new Spring 5.2.M1 in its dependencies.



Create a  simple JPA Entity `Post`. 

```java
@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
class Post {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;
    private String title;
    private String content;

}
```

And create a Repository for the `Post` Entity.

```java
interface PostRepository extends JpaRepository<Post, Long> {}
```

Define a  `RouterFuncation` bean to handle the routing rules.

```java
@Bean
public RouterFunction<ServerResponse> routes(PostHandler postController) {
    return route(GET("/posts"), postController::all)
        .andRoute(POST("/posts"), postController::create)
        .andRoute(GET("/posts/{id}"), postController::get)
        .andRoute(PUT("/posts/{id}"), postController::update)
        .andRoute(DELETE("/posts/{id}"), postController::delete);
}
```

The codes are almost same as the ones  we have used in Reactive stack, but note here the `ServerRequest`, `ServerResponse` and `RouterFunction` are imported from the new package:`org.springframework.web.servlet.function`.

Let's have a look at the details of `PostHandler`.

```java
@Component
class PostHandler {

    private final PostRepository posts;

    public PostHandler(PostRepository posts) {
        this.posts = posts;
    }

    public ServerResponse all(ServerRequest req) {
        return ServerResponse.ok().body(this.posts.findAll());
    }

    public ServerResponse create(ServerRequest req) throws ServletException, IOException {

        var saved = this.posts.save(req.body(Post.class));
        return ServerResponse.created(URI.create("/posts/" + saved.getId())).build();
    }

    public ServerResponse get(ServerRequest req) {
        return this.posts.findById(Long.valueOf(req.pathVariable("id")))
            .map(post -> ServerResponse.ok().body(post))
            .orElse(ServerResponse.notFound().build());
    }

    public ServerResponse update(ServerRequest req) throws ServletException, IOException {
        var data = req.body(Post.class);

        return this.posts.findById(Long.valueOf(req.pathVariable("id")))
            .map(
                post -> {
                    post.setTitle(data.getTitle());
                    post.setContent(data.getContent());
                    return post;
                }
            )
            .map(post -> this.posts.save(post))
            .map(post -> ServerResponse.noContent().build())
            .orElse(ServerResponse.notFound().build());

    }

    public ServerResponse delete(ServerRequest req) {
        return this.posts.findById(Long.valueOf(req.pathVariable("id")))
            .map(
                post -> {
                    this.posts.delete(post);
                    return ServerResponse.noContent().build();
                }
            )
            .orElse(ServerResponse.notFound().build());
    }

}
```

It is very similar to the codes of Reactive stack, and but the methods return a `ServerResponse` instead of `Mono<ServerResponse>`. 

Like the RouterFunctionDSL  feature provided in Reactive stack, the routing rules also can be written in Kotlin DSL.

```ko
 router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("{id}", postHandler::update)
            DELETE("{id}", postHandler::delete)
        }

    }
```



Besides these,   MockMvc also gets support of Kotlin DSL,  you can write your tests in a fluent style like the following.

```kot
  @Test
  fun `Get all posts should ok`() {
        mockMvc
                .get("/posts") {
                    accept = APPLICATION_JSON
                    headers {
                        contentLanguage = Locale.ENGLISH
                    }
                }
                .andExpect {
                    status { isOk }
                    content { contentType(APPLICATION_JSON_UTF8) }
                    jsonPath("$[0].title") { value(containsString("post")) } 
                }
                .andDo {
                    print()
                }
    }
```

Check out the [source codes](https://github.com/hantsy/spring-webmvc-functional-sample) from my Github , and compare it with [the codes](https://github.com/hantsy/spring-reactive-sample)  that I had written to demonstrate Reactive stack .











