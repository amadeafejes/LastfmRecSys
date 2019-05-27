/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.helper;

import com.bme.amadea.recommender.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Amadea Fejes
 */
@ManagedBean(name = "userService", eager = true)
@ApplicationScoped
public class UserService {

    @Resource(name = "jdbc/lastfm_recommender")
    private DataSource ds;

    private List<User> list;
    private Map<Integer, User> userMap;

    public UserService() {
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/lastfm_recommender");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    @PostConstruct
    public void init() {
        if (ds == null) {
            try {
                throw new SQLException("Can't get data source");
            } catch (SQLException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Connection con = null;
        try {
            con = ds.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (con == null) {
            try {
                throw new SQLException("Can't get database connection");
            } catch (SQLException ex) {
                Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(
                    "select id, userid, gender, age, country, registered from users");
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }

        ResultSet result = null;
        try {
            result = ps.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }

        List<User> list = new ArrayList<User>();
        Map<Integer, User> userMap = new HashMap<>();

        try {
            while (result.next()) {
                User user = new User();

                //set other fields
                user.setId(result.getInt("id"));
                user.setUserid(result.getString("userid"));
                user.setGender(result.getString("gender"));
                user.setAge(result.getString("age"));
                user.setCountry(result.getString("country"));
                user.setRegistered(result.getString("registered"));

                //add user to the list
                list.add(user);
                //add user to the map
                userMap.put(user.getId(), user);

                /*set integer id by list index
                Integer indexOfUser = list.indexOf(user);
                user.setId(indexOfUser);
                list.set(indexOfUser, user);*/
            }
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            con.close();
        } catch (SQLException ex) {
            Logger.getLogger(UserService.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.list = list;
        this.userMap = userMap;
    }

    public List<User> getUserList() {

        //sort by id
        Collections.sort(list, new UserComparator());
        return list;
    }

    public Map<Integer, User> getUserMap() {
        return userMap;
    }

}
