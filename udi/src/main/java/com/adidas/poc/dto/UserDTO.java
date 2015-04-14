package com.adidas.poc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Oleh_Golovanov on 4/9/2015 for ADI-COM-trunk
 */
//@XmlRootElement
public class UserDTO {
    private long id;
    private String firstName;
    private String secondName;
    private String email;
    private String nick;

    public UserDTO() {
    }

    public UserDTO(String firstName, String secondName, String email, String nick) {
        this();
        this.firstName = firstName;
        this.secondName = secondName;
        this.email = email;
        this.nick = nick;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", email='" + email + '\'' +
                ", nick='" + nick + '\'' +
                '}';
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
}
