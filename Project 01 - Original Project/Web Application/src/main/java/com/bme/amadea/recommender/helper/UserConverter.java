/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bme.amadea.recommender.helper;

/**
 *
 * @author Amadea Fejes
 */
import com.bme.amadea.recommender.model.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author Amadea Fejes
 */

@FacesConverter("userConverter")
public class UserConverter implements Converter {

    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        Object result = null;
        if (value != null && value.trim().length() > 0) {
            try {
                UserService service = (UserService) fc.getExternalContext().getApplicationMap().get("userService");
                List<User> userList = service.getUserList();
                Map<Integer, User> userMap = service.getUserMap();
                //create integer from string
                String intPart = value.substring(5);
                int intValue = Integer.parseInt(intPart);
                result = userMap.get(intValue);
            } catch (NumberFormatException e) {
                throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid user."));
            }
        }
        return result;
    }

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null) {
            User user = (User) object;
            String stringValue = user.getUserid();
            return stringValue;
        } else {
            return null;
        }
    }
}
