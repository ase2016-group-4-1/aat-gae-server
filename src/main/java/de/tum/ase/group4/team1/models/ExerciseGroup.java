package de.tum.ase.group4.team1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
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
    @Ignore
    public boolean enrollmentStatus = false;

    @JsonView(Lecture.Default.class)
    @JsonProperty("verificationUrl")
    @Ignore
    public String verificationUrl = "";
}
