/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.backing;

import com.bme.amadea.recommender.helper.UserService;
import com.bme.amadea.recommender.model.User;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;

/**
 *
 * @author Amadea Fejes
 */
@ManagedBean(name = "login")
@SessionScoped
public class LoginBean {

    private User selectedUser;
    private List<User> userList;

    @ManagedProperty("#{userService}")
    private UserService service;

    @PostConstruct
    public void init() {
        selectedUser = new User();
        userList = service.getUserList();
    }

    public String loginUser() {
        return "lists?faces-redirect=true";
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(User selectedUser) {
        this.selectedUser = selectedUser;
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public UserService getService() {
        return service;
    }

    public void setService(UserService service) {
        this.service = service;
    }

}
