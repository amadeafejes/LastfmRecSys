/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.helper;

import com.bme.amadea.recommender.model.User;
import java.util.Comparator;

/**
 *
 * @author Amadea Fejes
 */
public class UserComparator implements Comparator<User> {

    @Override
    public int compare(User o1, User o2) {
        return o1.getId() - o2.getId();
    }

}
