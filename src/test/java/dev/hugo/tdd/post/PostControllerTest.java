package dev.hugo.tdd.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.StringTemplate.STR;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    PostRepository postRepository;

    List<Post> posts = new ArrayList<>();
    @BeforeEach
    void setUp() {
        posts = List.of(
                new Post(1,1,"Hello, World!", "This is my first post.",null),
                new Post(2,1,"Second Post", "This is my second post.",null)
        );
    }

    // REST API

    // List
    @Test
    void shouldFindAllPosts() throws Exception {
        String jsonResponse = """
                [
                    {
                        "id":1,
                        "userId":1,
                        "title":"Hello, World!",
                        "body":"This is my first post.",
                        "version": null
                    },
                    {
                        "id":2,
                        "userId":1,
                        "title":"Second Post",
                        "body":"This is my second post.",
                        "version": null
                    }
                ]
                """;

        when(postRepository.findAll()).thenReturn(posts);

        ResultActions resultActions = mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        JSONAssert.assertEquals(jsonResponse, resultActions.andReturn().getResponse().getContentAsString(), false);

    }

    // /api/posts/1
    @Test
    void shouldFindPostWhenGivenValidId() throws Exception {
        when(postRepository.findById(1)).thenReturn(Optional.of(posts.get(0)));

        mockMvc.perform(get("/api/posts/1"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotFindPostWhenGivenInvalidId() throws Exception {
        when(postRepository.findById(999)).thenThrow(PostNotFoundException.class);

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostWhenPostIsValid() throws Exception {
        var post = new Post(3, 1, "New Title", "New body", null);
        when(postRepository.save(post)).thenReturn(post);
        String json = STR."""
                {
                    "id":\{post.id()},
                    "userId":\{post.userId()},
                    "title":"\{post.title()}",
                    "body":"\{post.body()}",
                    "version": null
                }
                """;

        mockMvc.perform(post("/api/posts").contentType("application/json")
                .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreatePostWhenPostIsInvalid() throws Exception{
        var post = new Post(3, 1, "", "", null);
        when(postRepository.save(post)).thenReturn(post);
        String json = STR."""
                {
                    "id":\{post.id()},
                    "userId":\{post.userId()},
                    "title":"\{post.title()}",
                    "body":"\{post.body()}",
                    "version": null
                }
                """;

        mockMvc.perform(post("/api/posts").contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
