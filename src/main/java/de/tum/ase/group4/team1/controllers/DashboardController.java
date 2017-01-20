package de.tum.ase.group4.team1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController extends BaseController {
    @RequestMapping("/")
    public String dashboard() {
        return "dashboard";
    }
}
