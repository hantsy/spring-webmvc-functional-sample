package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
public class ApplicationTests {


    @Autowired
    WebApplicationContext applicationContext;

    private MockMvc mvc;

    @BeforeAll
    public void setup() {
        this.mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    @DisplayName("get blog properties")
    public void getBlogProperties() throws Exception {
        this.mvc
                .perform(
                        get("/info")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", equalTo("Hantsy'Blog")))
                .andExpect(jsonPath("$.description", equalTo("Description of Hantsy's Blog")))
                .andExpect(jsonPath("$.author", equalTo("Hantsy")));
    }

    @Test
    @DisplayName("get all posts should be ok")
    public void getPosts_shouldOK() throws Exception {
        this.mvc
                .perform(
                        get("/posts")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].title", containsString("one")))
                .andExpect(jsonPath("$.[1].title", containsString("two")));
    }

}
