package de.tum.ase.group4.team1.controllers;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.ExerciseGroup;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import de.tum.ase.group4.team1.models.Session;
import de.tum.ase.group4.team1.utils.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.thymeleaf.spring4.expression.Mvc;

import java.util.ArrayList;
import java.util.List;

@Controller
public class LectureController extends BaseController{
    // -- List --
    @RequestMapping({"/lectures"})
    public String list(Model model) {
        populateModelForList(Semester.getSemesterForCurrentDate(), model);
        return "lectures/list";
    }

    @RequestMapping("/{semesterSlug}")
    public String listWithSemester(@PathVariable String semesterSlug, Model model) {
        Semester selectedSemester = ObjectifyService.ofy().load().type(Semester.class).id(semesterSlug).now();
        if(selectedSemester == null){
            throw new NotFoundException();
        }
        populateModelForList(selectedSemester, model);
        return "lectures/list";
    }

    private void populateModelForList(Semester semester, Model model){
        List<Semester> semesters = Semester.getSemesters();
        List<Semester> semestersNotEmpty = new ArrayList<>(semesters.size());
        for(Semester s : semesters) {
            if(ObjectifyService.ofy().load().type(Lecture.class).ancestor(s).count() != 0){
                semestersNotEmpty.add(s);
            }
        }
        model.addAttribute("semesters", semestersNotEmpty);
        model.addAttribute("allSemesters", semesters);
        model.addAttribute("selectedSemester", semester);
        model.addAttribute("lectures",
                ObjectifyService.ofy().load().type(Lecture.class).ancestor(semester).list()
        );
        model.addAttribute("lectureToCreate", new Lecture());
    }

    // -- Create --
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String create(@ModelAttribute("lectureToCreate") Lecture lecture, BindingResult result, Model model) {
        UserService userService = UserServiceFactory.getUserService();
        if(!userService.isUserLoggedIn() || !userService.isUserAdmin()){
            // TODO display permission error message
            System.out.println("User not logged in or not administrator");
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        }
        if(lecture.getSlug().isEmpty() || lecture.getSemesterKey() == null) {
            // TODO display message that title should not be empty
            System.out.println("Title should not be empty");
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        }
        if(ObjectifyService.ofy().load().type(Lecture.class).parent(lecture.getSemesterKey()).id(lecture.getSlug()).now() != null) {
            // TODO display message that lecture with this title/slug already exists in this semester
            System.out.println("Lecture with this title already exists in this semester");
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        }
        System.out.println("Saving " + lecture);
        ObjectifyService.ofy().save().entity(lecture).now();
        if(lecture.getSemester() == Semester.getSemesterForCurrentDate()) {
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        } else {
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#listWithSemester").arg(0, lecture.getSemester().getSlug()).build();
        }
    }

    // -- Delete --
    @RequestMapping("/{semesterSlug}/{lectureSlug}/delete")
    public String delete(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model){
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        ObjectifyService.ofy().delete().key(lectureKey);
        int count = ObjectifyService.ofy().load().type(Lecture.class).ancestor(semesterKey).count();
        // return to /lectures if it is the current semester or if there are not lectures left in the current semester
        if(semesterSlug == Semester.getSemesterForCurrentDate().getSlug() || count == 0) {
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#list").build();
        } else {
            return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#listWithSemester").arg(0, semesterSlug).build();
        }
    }

    // -- Detail --
    // -- -- Groups -- --
    // -- -- -- List -- -- --
    @RequestMapping(value = "/{semesterSlug}/{lectureSlug}/groups", method = RequestMethod.GET)
    public String detailGroupsList(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model) {
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Lecture lecture =  ObjectifyService.ofy().load().type(Lecture.class).parent(semesterKey).id(lectureSlug).now();
        List<ExerciseGroup> groups = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lecture).list();
        System.out.println(groups);
        model.addAttribute("lecture", lecture);
        model.addAttribute("groups", groups);
        model.addAttribute("activeTab", "groups");
        model.addAttribute("groupToCreate", new ExerciseGroup());
        return "lectures/detail";
    }

    // -- -- -- Create -- -- --
    @RequestMapping(value = "/{semesterSlug}/{lectureSlug}/groups", method = RequestMethod.POST)
    public String detailGroupsCreate(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                     @ModelAttribute("groupToCreate") ExerciseGroup exerciseGroup, BindingResult result, Model model) {
        UserService userService = UserServiceFactory.getUserService();
        String listUrl = MvcUriComponentsBuilder.fromMappingName("LC#detailGroupsList").arg(0, semesterSlug).arg(1, lectureSlug).build();
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        exerciseGroup.setLectureKey(lectureKey);
        if(!userService.isUserLoggedIn() || !userService.isUserAdmin()){
            // TODO display permission error message
            System.out.println("User not logged in or not administrator");
            return "redirect:" + listUrl;
        }
        if(exerciseGroup.getSlug().isEmpty()) {
            // TODO display message that title should not be empty
            System.out.println("Title should not be empty");
            return "redirect:" + listUrl;
        }
        if(ObjectifyService.ofy().load().type(ExerciseGroup.class).parent(exerciseGroup.getLectureKey()).id(exerciseGroup.getSlug()).now() != null) {
            // TODO display message that lecture with this title/slug already exists in this semester
            System.out.println("Exercise group with this title already exists in this lecture");
            return "redirect:" + listUrl;
        }
        ObjectifyService.ofy().save().entity(exerciseGroup).now();
        return "redirect:" + listUrl;
    }

    // -- -- -- Delete -- -- --
    @RequestMapping("/{semesterSlug}/{lectureSlug}/{groupSlug}/delete")
    public String detailGroupsDelete(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                     @PathVariable String groupSlug, Model model){
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        Key<ExerciseGroup> groupKey = Key.create(lectureKey, ExerciseGroup.class, groupSlug);
        ObjectifyService.ofy().delete().key(groupKey);
        return "redirect:" + MvcUriComponentsBuilder.fromMappingName("LC#detailGroupsList").arg(0, semesterSlug).arg(1, lectureSlug).build();
    }


    // -- -- Sessions -- --
    // -- -- -- List -- -- --
    @RequestMapping(value = "/{semesterSlug}/{lectureSlug}/sessions", method = RequestMethod.GET)
    public String detailSessionsList(@PathVariable String semesterSlug, @PathVariable String lectureSlug, Model model) {
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Lecture lecture =  ObjectifyService.ofy().load().type(Lecture.class).parent(semesterKey).id(lectureSlug).now();
        List<Session> sessions = ObjectifyService.ofy().load().type(Session.class).ancestor(lecture).list();
        model.addAttribute("lecture", lecture);
        model.addAttribute("sessions", sessions);
        model.addAttribute("activeTab", "sessions");
        return "lectures/detail";
    }

    // -- -- -- Create -- -- --

}
