package de.tum.ase.group4.team1.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.AATUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {
    UserService userService = null;
    User user = null;
    AATUser aatUser = null;

    @ModelAttribute
    public void populateModel(Model model, HttpServletRequest request) {
        UserService userService = UserServiceFactory.getUserService();
        model.addAttribute("userService", userService);
        this.userService = userService;

        if(userService.isUserLoggedIn()) {
            User user = userService.getCurrentUser();
            model.addAttribute("user", user);
            this.user = user;

            AATUser aatUser = ObjectifyService.ofy().load().type(AATUser.class)
                    .filter("email", user.getEmail()).filter("authDomain", user.getAuthDomain()).first().now();
            if (aatUser == null) {
                // create new AATUser from current user if it does not already exist
                aatUser = new AATUser(user);
                ObjectifyService.ofy().save().entity(aatUser).now();
            }
            model.addAttribute("aatUser", aatUser);
            this.aatUser = aatUser;
        }
    }
}
