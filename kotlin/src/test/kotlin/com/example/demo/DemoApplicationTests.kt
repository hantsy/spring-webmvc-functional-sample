package com.example.demo


import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*


class TestConfigInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(applicationContext: GenericApplicationContext) {
        beans.initialize(applicationContext)
    }

}

@SpringBootTest(
        webEnvironment = MOCK,
        properties = ["context.initializer.classes=com.example.demo.TestConfigInitializer"]
)
class DemoApplicationTests {

    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var appContext: WebApplicationContext


    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(appContext).build();
    }

    @Test
    fun `Get all posts should ok`() {
        mockMvc
                .get("/posts") {
                    //secure = true
                    accept = APPLICATION_JSON
                    headers {
                        contentLanguage = Locale.ENGLISH
                    }
                    //principal = Principal { "foo" }
                }
                .andExpect {
                    status { isOk }
                    content { contentType(APPLICATION_JSON_UTF8) }
                    jsonPath("$[0].title") { value(containsString("post")) } //content { json("""{"someBoolean": false}""", false) }
                }
                .andDo {
                    print()
                }
    }

}
