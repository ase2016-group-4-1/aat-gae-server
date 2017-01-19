package de.tum.ase.group4.team1.models;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Enrollment {
    @Id Long id;
    @Index Key<AATUser> user;
    @Index Key<Lecture> lecture;
    @Index Key<ExerciseGroup> exerciseGroup;

    public Enrollment() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Key<AATUser> getUser() { return user; }
    public void setUser(Key<AATUser> user) { this.user = user; }

    public Key<Lecture> getLecture() { return lecture; }
    public void setLecture(Key<Lecture> lecture) { this.lecture = lecture; }

    public Key<ExerciseGroup> getExerciseGroup() { return exerciseGroup; }
    public void setExerciseGroup(Key<ExerciseGroup> exerciseGroup) { this.exerciseGroup = exerciseGroup; }

    @Override
    public String toString() {
        ExerciseGroup exerciseGroup = ObjectifyService.ofy().load().key(this.exerciseGroup).now();
        AATUser user = ObjectifyService.ofy().load().key(this.user).now();
        return user.getEmail() + " enrolled in " + exerciseGroup.toString();
    }
}
