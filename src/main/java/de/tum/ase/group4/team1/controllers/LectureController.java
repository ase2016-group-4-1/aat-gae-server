package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.utils.NotFoundException;
import de.tum.ase.group4.team1.models.Semester;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping(value = "/lectures")
public class LectureController {
    @RequestMapping(value = "/create", method = RequestMethod.GET)
    public String createForm(Model model) {
        return "lectures/new";
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createSubmit(Model model) {

        return "redirect:/lectures";
    }

    @RequestMapping("/edit")
    public String edit(Model model) {
        return "lectures/edit";
    }

    @RequestMapping({"", "/"})
    public String list(Model model) {
        List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).list();
        model.addAttribute("semesters", semesters);

        Semester defaultSemester = Semester.getCurrentSemester();
        model.addAttribute("selectedSemester", defaultSemester);

        return "lectures/list";
    }

    @RequestMapping("/{semesterSlug}")
    public String listWithSemester(@PathVariable String semesterSlug, Model model) {
        List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).list();
        model.addAttribute("semesters", semesters);

        Semester selectedSemester = ObjectifyService.ofy().load().type(Semester.class).id(semesterSlug).now();
        if(selectedSemester == null){
            throw new NotFoundException();
        }
        model.addAttribute("selectedSemester", selectedSemester);

        return "lectures/list";
    }
}
