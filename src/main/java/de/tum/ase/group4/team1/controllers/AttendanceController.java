package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@Controller
public class AttendanceController extends BaseController{
    @PostMapping("/{semesterSlug}/{lectureSlug}/sessions/{sessionSlug}/attend")
    public String create(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @PathVariable String sessionSlug, Model model) {
        // Prepare keys
        Key<Semester> semester = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lecture = Key.create(semester, Lecture.class, lectureSlug);
        Key<Session> session = Key.create(lecture, Session.class, sessionSlug);
        // Validate
        if(!userService.isUserLoggedIn()){
            // TODO display permission error message
            System.out.println("User not logged in");
            return "redirect:" + sessionListUrl(semester, lecture);
        }
        Key<AATUser> user = Key.create(aatUser);
        if(ObjectifyService.ofy().load().type(Attendance.class)
                .filter("lecture", lecture).filter("session", session).filter("user", user).count() != 0) {
            // TODO display attendance already exists
            System.out.println("Attendance already exists");
            return "redirect:" + sessionListUrl(semester, lecture);
        }
        Enrollment enrollment = ObjectifyService.ofy().load().type(Enrollment.class)
                .filter("lecture", lecture).filter("user", user).first().now();
        if(enrollment == null) {
            // TODO display that in order to attend you have first to enroll in a group
            System.out.println("No enrollment in group found");
            return "redirect:" + groupListUrl(semester, lecture);
        }
        // Get attendance
        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setLecture(lecture);
        attendance.setSession(session);
        attendance.setExerciseGroupOnCreation(enrollment.getExerciseGroup());
        // Save attendance
        ObjectifyService.ofy().save().entity(attendance).now();
        // Redirect to session list
        return "redirect:" + sessionListUrl(semester, lecture);
    }

    private String groupListUrl(Key<Semester> semester, Key<Lecture> lecture) {
        return MvcUriComponentsBuilder.fromMappingName("GC#list")
                .arg(0, semester.getName()).arg(1, lecture.getName()).build();
    }

    private String sessionListUrl(Key<Semester> semester, Key<Lecture> lecture) {
        return MvcUriComponentsBuilder.fromMappingName("SC#list")
                .arg(0, semester.getName()).arg(1, lecture.getName()).build();
    }
}
