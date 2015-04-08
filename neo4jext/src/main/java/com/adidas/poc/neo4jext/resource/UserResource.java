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
import java.nio.charset.Charset;

/**
 * Created by Oleh_Golovanov on 4/7/2015 for ADI-COM-trunk
 */
@Path("/user")
public class UserResource {

    private static  final Logger LOG = LoggerFactory.getLogger(UserResource.class);

    private UserService userService;

    public UserResource(@Context GraphDatabaseService graphDatabaseService) {
        this.userService = new UserService(graphDatabaseService);
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(@FormParam("firstName") String firstName, @FormParam("secondName") String secondName, @FormParam("email") String email, @FormParam("nick") String nick) {
        User createdUser = null;
        String respMessage = String.format("User with email %s already exists", email);
        if (email != null && !userService.checkIsUserExists(email)) {
            User newUser = new User(firstName, secondName, email, nick);
            createdUser = userService.createUser(newUser);
            respMessage = String.format("Created new %s", createdUser);
        }
        return Response.status(Response.Status.OK).entity(respMessage.getBytes(Charset.forName("UTF-8"))).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/check/{email}")
    public Response userExists(@PathParam("email") String email) {
        String respMessage = String.format("User with email %s %s exists", email, (userService.checkIsUserExists(email) ? "already " : "doesn't "));
        return Response.status(Response.Status.OK).entity(respMessage.getBytes(Charset.forName("UTF-8"))).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{email}")
    public Response findUser(@PathParam("email") String email) {
        LOG.debug("findUser rest method has been invoked");
        User user = userService.findUser(email);
        String respMessage = user == null ? String.format("User with email %s doesn't exists", email) : user.toString();
        return Response.status(Response.Status.OK).entity(respMessage.getBytes(Charset.forName("UTF-8"))).build();
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{email}")
    public Response deleteUser(@PathParam("email") String email) {
        boolean userExists = userService.checkIsUserExists(email);
        if(userExists){
            userService.deleteUser(email);
        }
        String respMessage = String.format("User with email %s %s", email, !userExists ? "doesn't exist": "has been deleted");
        return Response.status(Response.Status.OK).entity(respMessage.getBytes(Charset.forName("UTF-8"))).build();
    }


}
