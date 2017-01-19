package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SessionController extends BaseController {
    // -- List --
    @GetMapping("/{semesterSlug}/{lectureSlug}/sessions")
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
        // Get the sessions
        List<Session> sessions = ObjectifyService.ofy().load().type(Session.class).ancestor(lecture).list();
        if(userService.isUserLoggedIn()){
            Key<AATUser> user = Key.create(this.aatUser);
            // Get the attendances of the user for this lecture and map them to the session slugs
            Map<String, Attendance> attendanceMap = new HashMap<>();
            List<Attendance> attendances = ObjectifyService.ofy().load().type(Attendance.class)
                    .filter("user", user).filter("lecture", lecture).list();
            for(Attendance attendance : attendances) {
                attendanceMap.put(attendance.getSession().getName(), attendance);
            }
            model.addAttribute("attendanceMap", attendanceMap);
        }
        model.addAttribute("sessions", sessions);
        if(userService.isUserLoggedIn() && userService.isUserAdmin()) {
            // New session for the new group form to be rendered
            model.addAttribute("sessionToCreate", new Session());
        }
        // Set the active tab for the UI
        model.addAttribute("activeTab", "sessions");
        // Render lecture details screen (will load session list in tab)
        return "lectures/detail";
    }

    // -- Create --
    @PostMapping("/{semesterSlug}/{lectureSlug}/sessions")
    public String create(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @ModelAttribute("sessionToCreate") Session session, BindingResult result,
                         RedirectAttributes redirectAttributes) throws UnsupportedEncodingException {
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Prepare session
        session.setLecture(lectureKey);
        session.generateSlug();
        // Validation
        if(result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.toString());
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        if(!userService.isUserLoggedIn() || !userService.isUserAdmin()){
            redirectAttributes.addFlashAttribute("errorTitle", "Could not create session");
            redirectAttributes.addFlashAttribute("errorMessage", "User not signed in or not an administrator");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        if(session.getTitle().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorTitle", "Could not create session");
            redirectAttributes.addFlashAttribute("errorMessage", "Title is a required field");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        if(ObjectifyService.ofy().load().type(Session.class).parent(session.getLecture()).id(session.getSlug()).now() != null) {
            redirectAttributes.addFlashAttribute("errorTitle", "Could not create session");
            redirectAttributes.addFlashAttribute("errorMessage", "Session with this title already exists in this lecture");
            return "redirect:" + listUrl(semesterKey, lectureKey);
        }
        // Save session
        ObjectifyService.ofy().save().entity(session).now();
        // Redirect to session list
        redirectAttributes.addFlashAttribute("successMessage", "Session successfully created");
        return "redirect:" + listUrl(semesterKey, lectureKey);
    }

    @PostMapping("/{semesterSlug}/{lectureSlug}/sessions/{sessionSlug}/activate")
    public String activate(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                           @PathVariable String sessionSlug, RedirectAttributes redirectAttributes){
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Get list of all the sessions in this lecture
        List<Session> sessions = ObjectifyService.ofy().load().type(Session.class).ancestor(lectureKey).list();
        // Go through all of them
        for(Session session : sessions){
            // and update the active state
            if(sessionSlug.equals(session.getSlug())) {
                session.setActive(true);
            } else {
                session.setActive(false);
            }
        }
        // Save the sessions back to the datastore
        ObjectifyService.ofy().save().entities(sessions);
        // Redirect to session list
        redirectAttributes.addFlashAttribute("successMessage", "Session successfully activated");
        return "redirect:" + listUrl(semesterKey, lectureKey);
    }

    // -- Delete --
    @PostMapping("/{semesterSlug}/{lectureSlug}/sessions/{sessionSlug}/delete")
    public String delete(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         @PathVariable String sessionSlug, RedirectAttributes redirectAttributes){
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        Key<Session> sessionKey = Key.create(lectureKey, Session.class, sessionSlug);
        // Delete group
        ObjectifyService.ofy().delete().key(sessionKey);
        // Redirect to group list
        redirectAttributes.addFlashAttribute("successMessage", "Session successfully deleted");
        return "redirect:" + listUrl(semesterKey, lectureKey);
    }

    private String listUrl(Key<Semester> semesterKey, Key<Lecture> lectureKey) {
        return MvcUriComponentsBuilder.fromMappingName("SC#list")
                .arg(0, semesterKey.getName()).arg(1, lectureKey.getName()).build();
    }
}
