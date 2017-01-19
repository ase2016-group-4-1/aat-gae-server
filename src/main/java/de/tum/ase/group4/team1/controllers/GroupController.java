package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.Enrollment;
import de.tum.ase.group4.team1.models.ExerciseGroup;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
public class GroupController extends BaseController {
    // -- List --
    @GetMapping("/{semesterSlug}/{lectureSlug}/groups")
    public String list(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model) {
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Get the semester
        Semester semester = ObjectifyService.ofy().load().key(semesterKey).now();
        model.addAttribute("semester", semester);
        // Get the lecture
        Lecture lecture =  ObjectifyService.ofy().load().key(lectureKey).now();
        model.addAttribute("lecture", lecture);
        // Get the groups
        List<ExerciseGroup> groups = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lecture).list();
        if(userService.isUserLoggedIn()){
            // Get the enrollment of the user for this lecture
            Enrollment enrollment = ObjectifyService.ofy().load().type(Enrollment.class)
                    .filter("user", Key.create(aatUser)).filter("lecture", lectureKey).first().now();
            model.addAttribute("enrollment", enrollment);
        }
        model.addAttribute("groups", groups);
        if(userService.isUserLoggedIn() && userService.isUserAdmin()) {
            // New group for the new group form to be rendered
            model.addAttribute("groupToCreate", new ExerciseGroup());
        }
        // Set the active tab for the UIs
        model.addAttribute("activeTab", "groups");
        // Render lecture details screen (will load group list in tab)
        return "lectures/detail";
    }

    // -- Create --
    @PostMapping(value = "/{semesterSlug}/{lectureSlug}/groups")
    public String create(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @ModelAttribute("groupToCreate") ExerciseGroup exerciseGroup, BindingResult result,
                         Model model) throws UnsupportedEncodingException {
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Prepare lecture
        exerciseGroup.setLecture(lectureKey);
        exerciseGroup.generateSlug();
        // Validation
        if(!userService.isUserLoggedIn() || !userService.isUserAdmin()){
            // TODO display permission error message
            System.out.println("User not logged in or not administrator");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        if(exerciseGroup.getTitle().isEmpty()) {
            // TODO display message that title should not be empty
            System.out.println("Title should not be empty");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        if(ObjectifyService.ofy().load().type(ExerciseGroup.class).parent(exerciseGroup.getLecture()).id(exerciseGroup.getSlug()).now() != null) {
            // TODO display message that lecture with this title/slug already exists in this semester
            System.out.println("Exercise group with this title already exists in this lecture");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        // Save group
        ObjectifyService.ofy().save().entity(exerciseGroup).now();
        // Redirect to group list
        // TODO display success message
        return "redirect:" + listUrl(semesterKey, lectureKey);
    }

    // -- Delete --
    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/delete")
    public String delete(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @PathVariable String groupSlug, Model model){
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        Key<ExerciseGroup> groupKey = Key.create(lectureKey, ExerciseGroup.class, groupSlug);
        // Delete group
        ObjectifyService.ofy().delete().key(groupKey);
        // Redirect to group list
        return "redirect:" + listUrl(semesterKey, lectureKey);
    }

    private String listUrl(Key<Semester> semesterKey, Key<Lecture> lectureKey) {
        return MvcUriComponentsBuilder.fromMappingName("GC#list")
                .arg(0, semesterKey.getName()).arg(1, lectureKey.getName()).build();
    }
}
