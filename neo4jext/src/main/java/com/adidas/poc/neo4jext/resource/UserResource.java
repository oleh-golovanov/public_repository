package com.adidas.poc.neo4jext.resource;

import com.adidas.poc.neo4jext.domain.User;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Oleh_Golovanov on 4/7/2015 for ADI-COM-trunk
 */
@Path("/user")
public class UserResource {

    private GraphDatabaseService graphDatabaseService;
    private AtomicLong idHolder;

    public UserResource(@Context GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
        initIdHolder();
    }

    private void initIdHolder() {
        idHolder = new AtomicLong(getMaxID());
    }

    private long getMaxID() {
        Result result = graphDatabaseService.execute(getMaxIdQuery());
        return Long.valueOf(result.resultAsString()).longValue();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User create(User user) {
        User createdUser = null;
        if (!isUserExists(user.getEmail())) {
            createdUser = createUser(user);
        }
        return createdUser;
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{email}")
    public Response userExists(@PathParam("email")String email) {
        return  Response.status(Response.Status.OK).entity("User with specified email " + (isUserExists(email) ? "already " : "not ") + " exists"). build();
    }

    private User createUser(User user) {
        User result = user;
        final Map<String, Object> params = MapUtil.map("firstName", user.getFirstName(), "secondName", user.getSecondName(), "email", user.getEmail(), "nick", user.getNick(), "id", generateUniqueId());
        try (Transaction tx = graphDatabaseService.beginTx();) {
            graphDatabaseService.beginTx();
            graphDatabaseService.execute(createQuery(), params);
            tx.success();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    private boolean isUserExists(String email) {
        boolean result;
        final Map<String, Object> params = MapUtil.map("email", email);
        try (Transaction tx = graphDatabaseService.beginTx();) {
            graphDatabaseService.beginTx();
            Result r = graphDatabaseService.execute(findUserByEmailQuery(), params);
            result = r.hasNext();
            tx.success();
        } catch (Exception e) {
            result = true;
        }
        return result;
    }

    private long generateUniqueId() {
        return idHolder.incrementAndGet();
    }

    private String findUserByEmailQuery() {
        return "MATCH (u:USER {email: {email}) RETURN u";
    }

    private String createQuery() {
        return "CREATE (u:USER {id: {id}, firstName: {firstName}, secondName: {secondName}, email: {email}, nick: {nick}}) RETURN u";
    }

    private String getMaxIdQuery() {
        return "MATCH (u:USER) RETURN count(u)";
    }
}
