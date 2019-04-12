package com.example.demo


import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.ServerResponse.*
import org.springframework.web.servlet.function.router
import java.io.IOException
import java.net.URI
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.servlet.ServletException

@SpringBootApplication
class DemoApplication


val beans = beans {
    bean {
        CommandLineRunner {
            println("start data initialization...")
            val posts = ref<PostRepository>()

            posts.deleteAll()

            arrayListOf(
                    Post(null, "my first post", "content of my first post"),
                    Post(null, "my second post", "content of my second post")
            ).forEach { posts.save(it) }

            println("done data initialization...")

        }
    }

    bean {
        PostRoutes(PostHandler(ref())).routes()
    }


}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args) {
        addInitializers(beans)
    }
}

class PostRoutes(private val postHandler: PostHandler) {
    fun routes() = router {
        "/posts".nest {
            GET("", postHandler::all)
            GET("{id}", postHandler::get)
            POST("", postHandler::create)
            PUT("{id}", postHandler::update)
            DELETE("{id}", postHandler::delete)
        }

    }
}

class PostHandler(private val posts: PostRepository) {

    fun all(req: ServerRequest): ServerResponse {
        return ok().body(this.posts.findAll())
    }

    @Throws(ServletException::class, IOException::class)
    fun create(req: ServerRequest): ServerResponse {

        val saved = this.posts.save(req.body(Post::class.java))
        return created(URI.create("/posts/" + saved.id)).build()
    }

    operator fun get(req: ServerRequest): ServerResponse {
        return this.posts.findById(req.pathVariable("id").toLong())
                .map { post -> ok().body(post) }
                .orElse(notFound().build())
    }

    @Throws(ServletException::class, IOException::class)
    fun update(req: ServerRequest): ServerResponse {
        val data = req.body(Post::class.java)

        return this.posts.findById(req.pathVariable("id").toLong())
                .map { it.copy(title = data.title, content = data.content) }
                .map { this.posts.save(it) }
                .map { noContent().build() }
                .orElse(notFound().build())

    }

    fun delete(req: ServerRequest): ServerResponse {
        return this.posts.findById(java.lang.Long.valueOf(req.pathVariable("id")))
                .map {
                    this.posts.delete(it)
                    noContent().build()
                }
                .orElse(notFound().build())
    }

}

interface PostRepository : JpaRepository<Post, Long>

@Entity
data class Post(@Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
                val title: String? = null,
                val content: String? = null
)


