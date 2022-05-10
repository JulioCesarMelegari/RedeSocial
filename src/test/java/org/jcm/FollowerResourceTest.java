package org.jcm;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.Matchers;
import org.jcm.dto.FollowerRequest;
import org.jcm.model.Follower;
import org.jcm.model.User;
import org.jcm.repository.FollowerRepository;
import org.jcm.repository.UserRepository;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

    @Inject
    UserRepository userRepository;

    @Inject
    FollowerRepository followerRepository;

    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {

        //usuario padrão testes
        User user = new User();
        user.setAge(38);
        user.setName("Valdomiro");
        userRepository.persist(user);
        userId = user.getId();
        //seguidor
        var follower = new User();
        follower.setAge(44);
        follower.setName("Ananinas Anão");
        userRepository.persist(follower);
        followerId = follower.getId();

        var followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("should return 409 when followerId is equal to User id")
    public void sameUserAsFollowerTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);
        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParams("userId", userId)
                .when()
                .put()
                .then().statusCode(Response.Status.CONFLICT.getStatusCode())
                .body(Matchers.is("You can't follow yourself"));
    }

    @Test
    @DisplayName("should return 404 on follow a user when User id doesn't exist")
    public void userNotFoundWhenTryingToFollowTest() {

        var body = new FollowerRequest();
        body.setFollowerId(userId);

        var nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParams("userId", nonexistentUserId)
                .when()
                .put()
                .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should follow a user")
    public void followUserTest() {

        var body = new FollowerRequest();
        body.setFollowerId(followerId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .pathParams("userId", userId)
                .when()
                .put()
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @DisplayName("should return 404 on list user followers and User id doesn't exist")
    public void userNotFoundWhenListingFollowersTest() {
        var nonexistentUserId = 999;

        given()
                .contentType(ContentType.JSON)
                .pathParams("userId", nonexistentUserId)
            .when()
                .get()
                .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
    @Test
    @DisplayName("should list a user's followers")
    public void listFollowersTest(){
        var response =
                given().contentType(ContentType.JSON)
                        .pathParams("userId", userId)
                        .when().get()
                        .then().extract().response();

        var followersCount = response.jsonPath().get("followersCount");
        var followersContent = response.jsonPath().getList("content");

        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
        assertEquals(1,followersCount);
        assertEquals(1, followersContent.size());
    }
    @Test
    @DisplayName("should return 404 on unfollow user and User id doesn't exist")
    public void userNotFoundWhenUnfollowingAUserTest(){
        var inexistentUserId = 999;

        given()
                .pathParam("userId", inexistentUserId)
                .queryParam("followerId", followerId)
                .when().delete()
                .then().statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("should Unfollow an user")
    public void unfollowUserTest(){
        given()
                .pathParam("userId", userId)
                .queryParam("followerId", followerId)
                .when().delete()
                .then().statusCode(Response.Status.NO_CONTENT.getStatusCode());
    }
}