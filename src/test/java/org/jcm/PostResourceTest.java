package org.jcm;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.jcm.dto.CreatePostRequest;
import org.jcm.model.Follower;
import org.jcm.model.Post;
import org.jcm.model.User;
import org.jcm.repository.FollowerRepository;
import org.jcm.repository.PostRepository;
import org.jcm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static io.restassured.RestAssured.given;

@QuarkusTest
@TestHTTPEndpoint(PostResource.class) //com esta anotação a propria classe busca a url na classe originaria
class PostResourceTest {

    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;

    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp(){
        var user =  new User();
        user.setAge(30);
        user.setName("Bolotão Nervoso");
        userRepository.persist(user);
        userId = user.getId();

        //postagem para usuario
        Post post = new Post();
        post.setText("Postagem Postada");
        post.setUser(user);
        postRepository.persist(post);

        var userNotFollower = new User();
        userNotFollower.setAge(45);
        userNotFollower.setName("Marcelo Martelo");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //usuario seguidor

        var userFollower = new User();
        userFollower.setAge(22);
        userFollower.setName("Bibiano Martins");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("should create a post for a user")
    public void createPostTest(){
        var postRequest = new CreatePostRequest();
        postRequest.setText("Algum comentario maldoso");

        var userId = 1;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParams("userId", userId)
            .when()
                .post()
            .then()
                .statusCode(201);
    }
//-----------------testes
    @Test
    @DisplayName("should return 404 when trying to make a post for an nonexistent user")
    public void postForAnNonexistentUserTest(){

        var postRequest = new CreatePostRequest();
        postRequest.setText("o Rato roeu a roupa do rei de roma");

        var nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(postRequest)
                .pathParams("userId",nonexistentUserId)
            .when()
                .post()
            .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("should return 404 when user doesn't exist")
    public void listPostUserNotFoundTest(){
        var nonexistentUserId = 999;

        given()
                .pathParams("userId", nonexistentUserId)
                .when().get()
                .then().statusCode(404);
    }

    @Test
    @DisplayName("should return 400 when followerId header is not present")
    public void listPostFollowerHeaderNotSendTest(){
        given()
                .pathParams("userId", userId)
                .when().get()
                .then().statusCode(400)
                .body(Matchers.is("You forgot the header followerId"));

    }

    @Test
    @DisplayName("should return 400 when follower doesn't exist")
    public void listPostFollowerNotFoundTest(){
        var nonexistentUserId = 999;

        given()
                .pathParams("userId", userId).header("followerId", nonexistentUserId)
                .when().get()
                .then().statusCode(400).body(Matchers.is("Inexistent followerId"));

    }

    @Test
    @DisplayName("should return 403 when follower isn't a follower")
    public void listPostNotAFollower(){
        given()
                .pathParams("userId", userId).header("followerId", userNotFollowerId)
                .when().get()
                .then().statusCode(403).body(Matchers.is("You can't see these posts"));

    }

    @Test
    @DisplayName("should return posts")
    public void listPostTest(){
        given()
                .pathParams("userId", userId).header("followerId", userFollowerId)
                .when().get()
                .then().statusCode(200).body("size()", Matchers.is(1));
    }

}