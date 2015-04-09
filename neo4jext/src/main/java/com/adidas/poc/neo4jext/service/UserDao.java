package com.adidas.poc.neo4jext.service;

import com.adidas.poc.neo4jext.domain.PocLabel;
import com.adidas.poc.neo4jext.domain.User;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
public class UserDao implements Dao<String, User> {
    private static final Logger LOG = LoggerFactory.getLogger(UserDao.class);
    public static final String EMAIL = "email";
    public static final String FIRST_NAME = "firstName";
    public static final String SECOND_NAME = "secondName";
    public static final String NICK = "nick";
    public static final String ID = "id";
    private final GraphDatabaseService graphDatabaseService;

    public UserDao(GraphDatabaseService graphDatabaseService) {
        LOG.debug("UserDaoImpl constructor has ben triggered");
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public User create(User input) {
        Node userNode = graphDatabaseService.createNode(PocLabel.USER);
        mapUserToNode(input, userNode);
        return input;
    }

    @Override
    public User update(User input) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User delete(String email) {
        Node node = findUserNodeByEmail(email);
        User deleted = mapNodeToUser(node);
        if(node != null){
            node.delete();
        }
        return deleted;
    }

    private Node findUserNodeByEmail(String email) {
        return graphDatabaseService.findNode(PocLabel.USER, EMAIL, email);
    }

    @Override
    public User find(String email) {
        return mapNodeToUser(findUserNodeByEmail(email));
    }

    @Override
    public Collection<User> findAll() {
        Collection<User> result = new ArrayList<>();
        try (ResourceIterator<Node> userNodes = graphDatabaseService.findNodes(PocLabel.USER)) {
            while(userNodes.hasNext()){
                result.add(mapNodeToUser(userNodes.next()));
           }
        }
        return result;
    }

    private User mapNodeToUser(Node node) {
        User user = null;
        if(node != null) {
            user = new User();
            String email = (String) node.getProperty(EMAIL);
            String firstName = (String) node.getProperty(FIRST_NAME);
            String secondName = (String) node.getProperty(SECOND_NAME);
            String nick = (String) node.getProperty(NICK);
            long id = Long.parseLong(String.valueOf(node.getProperty(ID)));
            user.setFirstName(firstName);
            user.setSecondName(secondName);
            user.setEmail(email);
            user.setNick(nick);
            user.setId(id);
        }
        return user;
    }

    private void mapUserToNode(User user, Node node) {
        node.setProperty(EMAIL, user.getEmail());
        node.setProperty(FIRST_NAME, user.getFirstName());
        node.setProperty(SECOND_NAME, user.getSecondName());
        node.setProperty(NICK, user.getNick());
        node.setProperty(ID, user.getId());
    }
}
