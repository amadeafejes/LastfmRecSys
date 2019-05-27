/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.model;

import java.math.BigDecimal;

/**
 *
 * @author Amadea Fejes
 */
public class RecSong extends Song {

    //field from rec_track
    private float comp;

    public float getComp() {
        return round(comp, 2);
    }

    public void setComp(float comp) {
        this.comp = comp;
    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

}
