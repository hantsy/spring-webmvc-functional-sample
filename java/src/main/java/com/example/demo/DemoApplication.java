package com.example.demo;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static javax.persistence.GenerationType.AUTO;
import static org.springframework.web.servlet.function.RequestPredicates.*;
import static org.springframework.web.servlet.function.RouterFunctions.route;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public RouterFunction<ServerResponse> routes(PostHandler postController) {
        return route(GET("/posts"), postController::all)
            .andRoute(POST("/posts"), postController::create)
            .andRoute(GET("/posts/{id}"), postController::get)
            .andRoute(PUT("/posts/{id}"), postController::update)
            .andRoute(DELETE("/posts/{id}"), postController::delete);
    }
}


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

@Component
@Slf4j
class DataInitializer {

    private final PostRepository posts;

    public DataInitializer(PostRepository posts) {
        this.posts = posts;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initPosts() {
        log.info("initializing posts data...");
        this.posts.deleteAll();
        Stream.of("Post one", "Post two").forEach(
            title -> this.posts.save(Post.builder().title(title).content("content of " + title).build())
        );
    }

}

interface PostRepository extends JpaRepository<Post, Long> {
}


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