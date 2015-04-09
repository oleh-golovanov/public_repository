package com.adidas.poc.neo4jext.resource;

import com.adidas.poc.neo4jext.domain.User;
import com.adidas.poc.neo4jext.service.UserService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.Collection;

/**
 * Created by Oleh_Golovanov on 4/7/2015 for ADI-COM-trunk
 */
@Path("/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    private UserService userService;

    public UserResource(@Context GraphDatabaseService graphDatabaseService) {
        this.userService = new UserService(graphDatabaseService);
    }


    @POST
    public Response create(User user) {
        LOG.debug("create rest method has been invoked with incomming parameter {}", user);
        User createdUser = null;
        String email = null;
        if (user != null) {
            email = user.getEmail();
            if(email != null && !userService.checkIsUserExists(email))
                createdUser = userService.createUser(user);
        }
        Serializable rEntity = createdUser == null ? String.format("User with email %s already exists", email) : createdUser;
        return Response.ok(rEntity).build();

    }

    @GET
    @Path("/check/{email}")
    public Response userExists(@PathParam("email") String email) {
        String respMessage = String.format("User with email %s %s exists", email, (userService.checkIsUserExists(email) ? "already " : "doesn't "));
        return Response.ok(respMessage).build();
    }

    @GET
    @Path("/{email}")
    public Response findUser(@PathParam("email") String email) {
        LOG.debug("findUser rest method has been invoked");
        User user = userService.findUser(email);
        String respMessage = user == null ? String.format("User with email %s doesn't exists", email) : user.toString();
        return Response.ok(respMessage).build();
    }

    @GET
    @Path("/list")
    public Response findAllUsers() {
        LOG.debug("findAllUsers rest method has been invoked");
        Collection<User> users = userService.findAllUsers();
        return Response.ok(users).build();
    }

    @DELETE
    @Path("/{email}")
    public Response deleteUser(@PathParam("email") String email) {
        boolean userExists = userService.checkIsUserExists(email);
        if (userExists) {
            userService.deleteUser(email);
        }
        String respMessage = String.format("User with email %s %s", email, !userExists ? "doesn't exist" : "has been deleted");
        return Response.ok(respMessage).build();
    }


}
