package org.jcm;

import lombok.Data;
import org.jboss.resteasy.plugins.providers.ReactiveStreamProvider;
import org.jcm.dto.FollowerRequest;
import org.jcm.dto.FollowerResponse;
import org.jcm.dto.FollowersPerUserResponse;
import org.jcm.model.Follower;
import org.jcm.repository.FollowerRepository;
import org.jcm.repository.UserRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.util.stream.Collectors;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

    private FollowerRepository followerRepository;
    private UserRepository userRepository;

    @Inject
    public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository){
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }

    @PUT
    @Transactional
    public Response followUser( @PathParam("userId") Long userId, FollowerRequest request){

        if(userId.equals(request.getFollowerId())){
            return Response.status(Response.Status.CONFLICT)
                    .entity("You can't follow yourself")
                    .build();
        }
        var user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        var follower = userRepository.findById(request.getFollowerId());

        boolean follows = followerRepository.follows(follower, user);

        if(!follows){
            var entity = new Follower();
            entity.setUser(user);
            entity.setFollower(follower);
            followerRepository.persist(entity);
            return Response.status(Response.Status.CREATED).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();

    }

    @GET
    public Response listFollowers(@PathParam("userId") Long userId){

        var user = userRepository.findById(userId);
        if (user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var list = followerRepository.findByUser(userId);
        FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
        responseObject.setFollowersCount(list.size());

        var followerList = list.stream()
                .map(FollowerResponse::new)
                .collect(Collectors.toList());
        responseObject.setContent(followerList);
        return Response.ok(responseObject).build();
    }

    @DELETE
    @Transactional
    public Response unfollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId){

        var user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        followerRepository.deleteByFollowerAndUser(followerId,userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
