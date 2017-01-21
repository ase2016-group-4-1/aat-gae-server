package de.tum.ase.group4.team1.controllers;

import com.googlecode.objectify.Key;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import de.tum.ase.group4.team1.services.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class NotificationController {
    private NotificationService notificationService = new NotificationService();

    @PostMapping("/{semesterSlug}/{lectureSlug}/notify")
    public String notify(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                         RedirectAttributes redirectAttributes){
        // Prepare keys
        Key<Semester> semesterKey = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lectureKey = Key.create(semesterKey, Lecture.class, lectureSlug);
        // Send notifications
        notificationService.sendNotification(lectureKey);
        // Redirect to detail view of lecture
        redirectAttributes.addFlashAttribute("successMessage", "Notifications have been sent");
        return "redirect:" + MvcUriComponentsBuilder.fromMappingName("EC#list")
                .arg(0, semesterSlug).arg(1, lectureSlug).build();
    }
}
