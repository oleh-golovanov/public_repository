package com.adidas.poc.neo4jext.service;

import com.adidas.poc.neo4jext.domain.User;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Oleh_Golovanov on 4/8/2015 for ADI-COM-trunk
 */
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    public static final String EMAIL = "email";
    public static final String FIRST_NAME = "firstName";
    public static final String SECOND_NAME = "secondName";
    public static final String NICK = "nick";
    public static final String ID = "id";
    private final GraphDatabaseService graphDatabaseService;
    private AtomicLong idHolder;

    public UserService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
        initIdHolder();
    }

    public User createUser(User newUser){
        return createNewUser(newUser);
    }

    public User findUser(String email){
        LOG.debug("findUser service method has been invoked");
        User result = null;
        try {
            result = findUserInternal(email);
        } catch (Exception e){
            LOG.error("Exception has been thrown ", e);
        }
        return result;
    }

    private User findUserInternal(String email) {
        User result = null;
        try(Transaction tx = graphDatabaseService.beginTx()) {
            Result dbResult = graphDatabaseService.execute(findUserQuery(), MapUtil.map(EMAIL, email));
            if(dbResult.hasNext()){
                Map<String, Object> mapResult = dbResult.next();
                LOG.debug("mapResult {}", mapResult);
                Node user = (Node)mapResult.get("u");
                LOG.debug("Node u {}", user);
                if(user != null){
                    String firstName = (String)user.getProperty(FIRST_NAME);
                    String secondName = (String)user.getProperty(SECOND_NAME);
                    String nick = (String)user.getProperty(NICK);
                    LOG.debug("Id = {}", user.getProperty(ID));
                    Long id = Long.parseLong(String.valueOf(user.getProperty(ID)));
                    result = new User(firstName,secondName,email,nick);
                    result.setId(id);
                }
            } else {
                LOG.debug("No user found by {}", email);
            }
            tx.success();
        }

        return result;
    }


    public void deleteUser(String email){
        final Map<String, Object> params = MapUtil.map(EMAIL, email);
        try (Transaction tx = graphDatabaseService.beginTx();) {
            graphDatabaseService.beginTx();
            graphDatabaseService.execute(deleteUserQuery(), params);
            tx.success();
        } catch (Exception e){
            //TODO
        }


    }

    public boolean checkIsUserExists(String email){
        boolean result;
        final Map<String, Object> params = MapUtil.map(EMAIL, email);
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

    private User createNewUser(User newUser) {
        newUser.setId(generateUniqueId());
        User result = newUser;
        final Map<String, Object> params = MapUtil.map(FIRST_NAME, result.getFirstName(), SECOND_NAME, result.getSecondName(), EMAIL, result.getEmail(), NICK, result.getNick(), ID, result.getId());
        try (Transaction tx = graphDatabaseService.beginTx();) {
            graphDatabaseService.beginTx();
            graphDatabaseService.execute(createQuery(), params);
            tx.success();
        } catch (Exception e) {
            result = null;
        }
        return result;
    }

    private void initIdHolder() {
        idHolder = new AtomicLong(getMaxID());
    }

    private long getMaxID() {
        Result result = graphDatabaseService.execute(getMaxIdQuery());
        long userCount = 0l;
        if(result.hasNext()){
            Map<String, Object> mapResult = result.next();
            Object count = mapResult.get("count");
            if(count != null){
                userCount = Long.valueOf(String.valueOf(count));
            }
        }
        return userCount;
    }


    private long generateUniqueId() {
        return idHolder.incrementAndGet();
    }

    private String findUserByEmailQuery() {
        return "MATCH (u:USER {email: {email}}) RETURN u";
    }

    private String createQuery() {
        return "CREATE (u:USER {id: {id}, firstName: {firstName}, secondName: {secondName}, email: {email}, nick: {nick}}) RETURN u";
    }

    private String getMaxIdQuery() {
        return "MATCH (u:USER) RETURN count(u) as count";
    }
    private String findUserQuery() {
        return "MATCH (u:USER {email: {email}}) RETURN u";
    }
    private String deleteUserQuery() {
        return "MATCH (u:USER {email: {email}}) DELETE u";
    }
}
