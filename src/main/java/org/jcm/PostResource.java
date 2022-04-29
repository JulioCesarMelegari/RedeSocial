package org.jcm;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.jcm.dto.CreatePostRequest;
import org.jcm.dto.PostResponse;
import org.jcm.model.Post;
import org.jcm.model.User;
import org.jcm.repository.PostRepository;
import org.jcm.repository.UserRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.stream.Collectors;

@Path("/users/{userId}/posts")
public class PostResource {

    private UserRepository userRepository;
    private PostRepository postRepository;

    @Inject
    public PostResource(UserRepository userRepository, PostRepository postRepository){

        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @POST
    @Transactional
    public Response savePost(@PathParam("userId") Long userId, CreatePostRequest request){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Post post = new Post();
        post.setText(request.getText());
        post.setUser(user);
        postRepository.persist(post);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public Response listPosts(@PathParam("userId") Long userId){
        User user = userRepository.findById(userId);
        if(user == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        PanacheQuery<Post> query = postRepository.find("user", Sort.by("dataTime", Sort.Direction.Descending) , user);
        var list = query.list();
        var postResponseList = list.stream()
                //.map(post -> PostResponse.fromEntity(post))
                .map(PostResponse::fromEntity)
                .collect(Collectors.toList());
        return Response.ok(postResponseList).build();
    }

}
