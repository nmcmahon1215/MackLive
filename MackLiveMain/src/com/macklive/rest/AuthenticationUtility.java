package com.macklive.rest;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Created by Nick on 6/26/16.
 */
public class AuthenticationUtility {

    public static boolean authenticate(String ownerId) {
        User currentUser = UserServiceFactory.getUserService().getCurrentUser();

        if (currentUser != null) {
            String currentUID = UserServiceFactory.getUserService().getCurrentUser().getUserId();
            return currentUID.equals(ownerId);
        }
        return false;
    }
}
