package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EnrollmentController extends BaseController {
    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/enroll")
    public String create(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @PathVariable String groupSlug, RedirectAttributes redirectAttributes) {
        // Prepare keys
        Key<Semester> semester = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lecture = Key.create(semester, Lecture.class, lectureSlug);
        Key<ExerciseGroup> exerciseGroup = Key.create(lecture, ExerciseGroup.class, groupSlug);
        // Validate
        if(!userService.isUserLoggedIn()){
            redirectAttributes.addFlashAttribute("errorTitle", "Failed to enroll");
            redirectAttributes.addFlashAttribute("errorMessage", "User not signed in");
            return "redirect:" + listUrl(semester, lecture);
        }
        // Get user
        Key<AATUser> user = Key.create(aatUser);
        // Get enrollment
        Enrollment enrollment = ObjectifyService.ofy().load().type(Enrollment.class)
                .filter("lecture", lecture).filter("user", user).first().now();
        if(enrollment != null) { // if it already exists, just update the group
            enrollment.setExerciseGroup(exerciseGroup);
        } else { // else create new enrollment
            enrollment = new Enrollment();
            enrollment.setUser(user);
            enrollment.setLecture(lecture);
            enrollment.setExerciseGroup(exerciseGroup);
        }
        // Save enrollment
        ObjectifyService.ofy().save().entity(enrollment).now();
        // Redirect to group list
        redirectAttributes.addFlashAttribute("successMessage", "Successfully enrolled");
        return "redirect:" + listUrl(semester, lecture);
    }

    private String listUrl(Key<Semester> semester, Key<Lecture> lecture) {
        return MvcUriComponentsBuilder.fromMappingName("GC#list")
                .arg(0, semester.getName()).arg(1, lecture.getName()).build();
    }
}
