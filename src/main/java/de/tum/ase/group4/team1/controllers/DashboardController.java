package de.tum.ase.group4.team1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {
    @RequestMapping("/")
    public String dashboard(Model model) {
        return "dashboard";
    }
}
