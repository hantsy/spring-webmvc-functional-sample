package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DemoApplicationTests {

    @LocalServerPort
    private int port;

    private TestRestTemplate restTemplate;

    @BeforeEach
    public void setup() {
        restTemplate = new TestRestTemplate();
    }

    @Test
    public void getPosts_shouldOK() {
        Post[] posts = restTemplate.getForObject("http://localhost:" + this.port + "/posts", Post[].class);
        assertThat(posts.length == 2);

    }

}
