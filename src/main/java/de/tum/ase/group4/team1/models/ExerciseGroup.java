package de.tum.ase.group4.team1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Load;
import com.googlecode.objectify.annotation.Parent;
import de.tum.ase.group4.team1.services.AATUserService;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Entity
public class ExerciseGroup {
    @Id String slug;
    String title;
    @Load @Parent Key<Lecture> lecture;

    public ExerciseGroup() { }

    public void generateSlug() throws UnsupportedEncodingException {
        if(slug == null && title != null) {
            this.slug = URLEncoder.encode(title, "UTF-8");
        }
    }

    @JsonView(Lecture.Default.class)
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonView(Lecture.Default.class)
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    @Override
    public String toString() {
        Lecture lecture = ObjectifyService.ofy().load().key(this.lecture).now();
        return title + " - " + lecture.toString();
    }

    // -- Serialization --

    @JsonView(Lecture.Default.class)
    @JsonProperty("enrolled")
    boolean enrollmentStatus() {
        UserService userService = UserServiceFactory.getUserService();
        if(userService.isUserLoggedIn()) {
            User user = userService.getCurrentUser();
            AATUser aatUser = AATUserService.getOrCreateAATUser(user);
            int count = ObjectifyService.ofy().load().type(Enrollment.class)
                    .filter("user", Key.create(aatUser)).filter("lecture", lecture).filter("exerciseGroup", Key.create(this)).count();
            if(count != 0){
                return true;
            }
        }
        return false;
    }

    @JsonView(Lecture.Default.class)
    @JsonProperty("verificationUrl")
    String getVerificationUrl(){
        return MvcUriComponentsBuilder.fromMappingName("APIC#verifyAttendance")
                .arg(0, lecture.getParent().getName()).arg(1, lecture.getName()).arg(2, slug).arg(3, "REPLACE_WITH_TOKEN").arg(4, "REPLACE_WITH_MODE").build();
    }
}
