package de.tum.ase.group4.team1.services;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.AATUser;

public class AATUserService {
    public static AATUser getOrCreateAATUser(User user){
        AATUser aatUser = ObjectifyService.ofy().load().type(AATUser.class)
                .filter("email", user.getEmail()).filter("authDomain", user.getAuthDomain()).first().now();
        if (aatUser == null) {
            // create new AATUser from current user if it does not already exist
            aatUser = new AATUser(user);
            ObjectifyService.ofy().save().entity(aatUser).now();
        }
        return aatUser;
    }
}
