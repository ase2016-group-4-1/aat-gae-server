package de.tum.ase.group4.team1.controllers;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

public abstract class BaseController {
    @ModelAttribute
    public void populateModel(Model model, HttpServletRequest request) {
        UserService userService = UserServiceFactory.getUserService();
        model.addAttribute("userService", userService);
        model.addAttribute("user", userService.getCurrentUser());
    }
}
