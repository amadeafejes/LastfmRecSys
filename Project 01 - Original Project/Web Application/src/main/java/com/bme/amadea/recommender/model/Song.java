/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.model;

/**
 *
 * @author Amadea Fejes
 */
public class Song {

    //fields from track
    private Integer id;

    private String artid;
    private String artname;
    private String traid;
    private String traname;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getArtid() {
        return artid;
    }

    public void setArtid(String artid) {
        this.artid = artid;
    }

    public String getArtname() {
        return artname;
    }

    public void setArtname(String artname) {
        this.artname = artname;
    }

    public String getTraid() {
        return traid;
    }

    public void setTraid(String traid) {
        this.traid = traid;
    }

    public String getTraname() {
        return traname;
    }

    public void setTraname(String traname) {
        this.traname = traname;
    }

}
