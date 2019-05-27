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
public class FavSong extends Song {

    //field from fav_track
    private int counter;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}
