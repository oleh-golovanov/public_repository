package com.adidas.poc.neo4jext.service;

import com.adidas.poc.neo4jext.domain.User;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Oleh_Golovanov on 4/8/2015 for ADI-COM-trunk
 */
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private final GraphDatabaseService graphDatabaseService;
    private UserDao userDao;
    private AtomicLong idHolder;

    public UserService(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
        userDao = new UserDao(this.graphDatabaseService);
        initIdHolder();
    }

    public User createUser(User newUser) {
        LOG.debug("createUser service method has been invoked");
        User result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            newUser.setId(generateUniqueId());
            result = userDao.create(newUser);
            tx.success();
        }
        return result;
    }

    public User findUser(String email) {
        LOG.debug("findUser service method has been invoked");
        User result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = userDao.find(email);
            tx.success();
        }
        return result;
    }

    public Collection<User> findAllUsers() {
        LOG.debug("findAllUsers service method has been invoked");
        Collection<User> result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = userDao.findAll();
            tx.success();
        }
        return result;
    }

    public User deleteUser(String email) {
        LOG.debug("deleteUser service method has been invoked");
        User result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = userDao.delete(email);
            tx.success();
        }
        return result;
    }

    public boolean checkIsUserExists(String email) {
        LOG.debug("checkIsUserExists service method has been invoked");
        boolean result;
        try (Transaction tx = graphDatabaseService.beginTx()) {
            result = userDao.find(email) != null;
            tx.success();
        }
        return result;
    }

    private void initIdHolder() {
        idHolder = new AtomicLong(getMaxID());
    }

    private long getMaxID() {
        return findAllUsers().size();
    }

    private long generateUniqueId() {
        return idHolder.incrementAndGet();
    }
}
