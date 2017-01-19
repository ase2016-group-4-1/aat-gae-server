package de.tum.ase.group4.team1.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    @GetMapping("/lectures")
    @JsonView(Lecture.Default.class)
    public List<Lecture> lectureList(){
        //List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).first().now();
        List<Lecture> lectures = ObjectifyService.ofy().load().type(Lecture.class).list();
        System.out.println(lectures);
        return lectures;
    }
}
