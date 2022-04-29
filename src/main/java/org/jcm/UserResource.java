package org.jcm;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.jcm.dto.CreateUserRequest;
import org.jcm.dto.ResponseError;
import org.jcm.model.User;
import org.jcm.repository.UserRepository;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    private UserRepository repository;
    private Validator validator;

    @Inject
    public UserResource(UserRepository repository, Validator validator){
        this.repository = repository;
        this.validator = validator;
    }

    @POST
    @Transactional
    public Response createUser(CreateUserRequest userRequest){
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userRequest);

        if (!violations.isEmpty()){
            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }
        User user = new User();
        user.setAge(userRequest.getAge());
        user.setName(userRequest.getName());
        repository.persist(user);
        return Response
                .status(Response.Status.CREATED.getStatusCode())
                .entity(user)
                .build();
    }

    @GET
    public Response listAllUsers(){
        PanacheQuery<User> query = repository.findAll();
        return Response.ok(query.list()).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteUser(@PathParam("id") Long id){
        User user = repository.findById(id);

        if(user != null){
            try {
                repository.delete(user);
                return Response.noContent().build();
            }catch (io.quarkus.arc.ArcUndeclaredThrowableException p){
                return Response.status(ResponseError.ERROR_USER_WITH_RELATIONSHIPS).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response uptadeUser(@PathParam("id") Long id, CreateUserRequest userData){
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(userData);

        if (!violations.isEmpty()){
            return ResponseError
                    .createFromValidation(violations)
                    .withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
        }
        User user = repository.findById(id);

        if(user !=null){
            user.setName(userData.getName());
            user.setAge((userData.getAge()));
            repository.persist(user);
            return Response.ok(user).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
