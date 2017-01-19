package de.tum.ase.group4.team1.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.Attendance;
import de.tum.ase.group4.team1.models.ExerciseGroup;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class APIController {
    @GetMapping("/lectures")
    @JsonView(Lecture.Default.class)
    public List<Lecture> lectureList(){
        List<Lecture> lectures = ObjectifyService.ofy().load().type(Lecture.class).list();
        return lectures;
    }

    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/verify")
    @ResponseBody
    public Map<String, Object> verifyAttendance(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                                    @PathVariable String groupSlug, @RequestParam String token) {
        Map<String, Object> resp = new HashMap<>();
        // TODO Auth from raspberry pi
        Key<ExerciseGroup> exerciseGroupKey = Key.create(
                Key.create(
                        Key.create(Semester.class, semesterSlug), Lecture.class, lectureSlug
                ), ExerciseGroup.class, groupSlug);
        Attendance attendance = ObjectifyService.ofy().load().type(Attendance.class)
                .filter("verificationToken", token).first().now();
        if(attendance == null){
            resp.put("status", "error");
            resp.put("message", "Could not find specified attendance");
            return resp;
        }
        attendance.setVerificationToken(token);
        attendance.setVerifiedAt(new Date()); // TODO should we get this from the Raspberry PI? Offline mode?
        attendance.setExerciseGroupOnVerification(exerciseGroupKey);
        ObjectifyService.ofy().save().entity(attendance);
        resp.put("status", "success");
        return resp;
    }

    // TODO: remove this - temporary endpoint for testing auth from the android app
    @GetMapping("/currentUserInfo")
    public User currentUserInfo() {
        UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        return user;
    }
}
