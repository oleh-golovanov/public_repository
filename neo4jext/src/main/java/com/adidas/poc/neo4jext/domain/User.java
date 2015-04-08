package com.adidas.poc.neo4jext.domain;

/**
 * Created by Oleh_Golovanov on 4/7/2015 for ADI-COM-trunk
 */
public class User {
    private long id;
    private String firstName;
    private String secondName;
    //unique property
    private String email;
    private String nick;

    public User(String firstName, String secondName, String email, String nick) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.email = email;
        this.nick = nick;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", email='" + email + '\'' +
                ", nick='" + nick + '\'' +
                '}';
    }
}
