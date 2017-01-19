package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;
import de.tum.ase.group4.team1.services.SemesterService;
import de.tum.ase.group4.team1.utils.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LectureController extends BaseController{
    private SemesterService semesterService = new SemesterService();
    // -- List --
    @GetMapping({"/lectures"})
    public String list(Model model) {
        populateModel(semesterService.getCurrentSemester(), model);
        return "lectures/list";
    }

    @GetMapping("/{semesterSlug}")
    public String listWithSemester(@PathVariable String semesterSlug, Model model) {
        Semester semester = ObjectifyService.ofy().load().type(Semester.class).id(semesterSlug).now();
        if(semester == null){
            throw new NotFoundException();
        }
        populateModel(semester, model);
        return "lectures/list";
    }

    // -- Create --
    @PostMapping("/create")
    public String create(@ModelAttribute Lecture lecture, BindingResult result, @RequestParam(required = false) String semesterSlug, Model model) throws UnsupportedEncodingException {
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        // Prepare lecture
        lecture.setSemester(semesterKey);
        lecture.generateSlug();
        // Error validation
        if(result.hasErrors()) {
            System.out.println(result.toString());
            return "redirect:" + listUrl(semesterKey);
        }
        if(!userService.isUserLoggedIn() || !userService.isUserAdmin()) {
            // TODO display permission error message
            System.out.println("User not logged in or not administrator");
            return "redirect:" + listUrl(semesterKey);
        }
        if(lecture.getTitle() == null || lecture.getTitle().isEmpty()) {
            // TODO display message that title is required
            System.out.println("Title is required");
            return "redirect:" + listUrl(semesterKey);
        }
        if(lecture.getSemester() == null) {
            // TODO display message that semester is required
            System.out.println("Semester is required");
            return "redirect:" + listUrl(semesterKey);
        }
        if(ObjectifyService.ofy().load().type(Lecture.class).parent(lecture.getSemester()).id(lecture.getSlug()).now() != null) {
            // TODO display message that lecture with this title/slug already exists in this semester
            System.out.println("Lecture with this title already exists in this semester");
            return "redirect:" + listUrl(semesterKey);
        }
        // Save
        ObjectifyService.ofy().save().entity(lecture).now();
        // Redirect to list of lectures
        return "redirect:" + listUrl(semesterKey);
    }

    // -- Detail --
    @GetMapping("/{semesterSlug}/{lectureSlug}")
    public String detail(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model){
        // Prepare keys
        Key<Semester> semester = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lecture = Key.create(semester, Lecture.class, lectureSlug);
        if(userService.isUserLoggedIn()){ // if user signed in
            // get enrollment count and group count
            int enrollmentCount = ObjectifyService.ofy().load().type(Enrollment.class)
                    .filter("user", Key.create(aatUser)).filter("lecture", lecture).count();
            int groupCount = ObjectifyService.ofy().load().type(ExerciseGroup.class)
                    .ancestor(lecture).count();
            // if user is enrolled, go straight to session list
            // if user is admin, check whether there are groups created, if yes, go straight to session list
            if(enrollmentCount != 0 || (userService.isUserAdmin() && groupCount != 0)) {
                return "redirect:" + MvcUriComponentsBuilder.fromMappingName("SC#list")
                        .arg(0, semesterSlug).arg(1, lectureSlug).build();
            }
        }
        return "redirect:" + MvcUriComponentsBuilder.fromMappingName("GC#list")
                .arg(0, semesterSlug).arg(1, lectureSlug).build();
    }

    // -- Delete --
    @PostMapping("/{semesterSlug}/{lectureSlug}/delete")
    public String delete(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model){
        // Build keys from slugs
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Delete lecture
        ObjectifyService.ofy().delete().key(lectureKey);
        // Redirect to list of lectures
        return "redirect:" + listUrl(semesterKey);
    }

    private String listUrl(Key<Semester> semester) {
        int count = ObjectifyService.ofy().load().type(Lecture.class).ancestor(semester).count();
        boolean isCurrent = semesterService.getCurrentSemester().getSlug().equals(semester.getName());
        if(isCurrent) { // if current we can redirect to /lectures
            return MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        } else { // else we have to redirect to /{semesterSlug}
            return MvcUriComponentsBuilder.fromMappingName("LC#listWithSemester").arg(0, semester.getName()).build();
        }
    }

    private void populateModel(Semester semester, Model model){
        model.addAttribute("semester", semester);
        // Load all the semesters
        List<Semester> allSemesters = semesterService.getSemesters();
        // Take only the semesters with actuall lectures inside them
        List<Semester> semesters = new ArrayList<>(allSemesters.size());
        for(Semester s : allSemesters) {
            int count = ObjectifyService.ofy().load().type(Lecture.class).ancestor(s).count();
            if(count != 0){
                semesters.add(s);
            }
        }
        model.addAttribute("semesters", semesters);
        // Load all the lectures for the given semester
        model.addAttribute("lectures", ObjectifyService.ofy().load().type(Lecture.class).ancestor(semester).list());

        if(userService.isUserLoggedIn() && userService.isUserAdmin()) {
            // New lecture for the new lecture form to be rendered
            model.addAttribute("allSemesters", allSemesters);
            model.addAttribute("lectureToCreate", new Lecture());
        }
    }
}
