/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.backing;

import com.bme.amadea.recommender.model.FavSong;
import com.bme.amadea.recommender.model.RecSong;
import com.bme.amadea.recommender.model.User;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Amadea Fejes
 */
@ManagedBean(name = "list")
@SessionScoped
public class ListBean implements Serializable {

    @Resource(name = "jdbc/lastfm_recommender")
    private DataSource ds;

    @ManagedProperty(value = "#{login}")
    private LoginBean loginBean;

    private User actualUser;

    @PostConstruct
    public void init() {
        actualUser = loginBean.getSelectedUser();

        int h = 65;
    }

    public ListBean() {
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/jdbc/lastfm_recommender");
        } catch (NamingException e) {
            e.printStackTrace();
        }

    }

    public String logoutUser() {

        // re-initialize the login session bean
        //loginBean = new LoginBean();
        //FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("loginBean", loginBean);
        User newUser = new User();
        loginBean.setSelectedUser(newUser);

        return "login?faces-redirect=true";
    }

    public List<FavSong> getFavSongList() throws SQLException {

        //setup user
        actualUser = getActualUser();

        if (ds == null) {
            throw new SQLException("Can't get data source");
        }

        Connection con = ds.getConnection();

        if (con == null) {
            throw new SQLException("Can't get database connection");
        }

        PreparedStatement selectFavTracks
                = con.prepareStatement(
                        "SELECT f.tra_id, f.counter, t.id, t.artid, t.artname, t.traname, t.traid "
                        + "FROM fav_tracks f JOIN tracks t ON f.tra_id = t.id"
                        + " where f.user_id = ? order by f.counter desc");

        selectFavTracks.setInt(1, actualUser.getId());

        ResultSet result = selectFavTracks.executeQuery();

        List<FavSong> list = new ArrayList<FavSong>();

        while (result.next()) {
            FavSong favSong = new FavSong();

            favSong.setTraid(result.getString("t.traid"));
            favSong.setCounter(result.getInt("f.counter"));

            favSong.setArtid(result.getString("t.artid"));
            favSong.setArtname(result.getString("t.artname"));
            favSong.setTraname(result.getString("t.traname"));

            list.add(favSong);
        }

        con.close();

        return list;
    }

    public List<RecSong> getRecSongList() throws SQLException {

        //setup user
        actualUser = getActualUser();

        if (ds == null) {
            throw new SQLException("Can't get data source");
        }

        Connection con = ds.getConnection();

        if (con == null) {
            throw new SQLException("Can't get database connection");
        }

        PreparedStatement selectRecTracks
                = con.prepareStatement(
                        "SELECT r.tra_id, r.comp, t.id, t.artid, t.artname, t.traname, t.traid "
                        + "FROM rec_tracks r JOIN tracks t ON r.tra_id = t.id"
                        + " where r.user_id = ? order by r.comp desc");

        selectRecTracks.setInt(1, actualUser.getId());

        ResultSet result = selectRecTracks.executeQuery();

        List<RecSong> list = new ArrayList<RecSong>();

        while (result.next()) {
            RecSong recSong = new RecSong();

            recSong.setTraid(result.getString("t.traid"));
            recSong.setComp(result.getFloat("r.comp"));

            recSong.setArtid(result.getString("t.artid"));
            recSong.setArtname(result.getString("t.artname"));
            recSong.setTraname(result.getString("t.traname"));

            list.add(recSong);
        }

        con.close();

        return list;
    }

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public User getActualUser() {
        actualUser = loginBean.getSelectedUser();
        return actualUser;
    }

    public void setActualUser(User actualUser) {
        this.actualUser = actualUser;
    }

}
