package de.tum.ase.group4.team1.services;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;

import java.util.List;

public class LectureService {
    public void deleteLecture(Key<Lecture> lectureKey) {
        List<Key<ExerciseGroup>> groupKeys = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lectureKey).keys().list();
        ObjectifyService.ofy().delete().keys(groupKeys);
        List<Key<Session>> sessionKeys = ObjectifyService.ofy().load().type(Session.class).ancestor(lectureKey).keys().list();
        ObjectifyService.ofy().delete().keys(sessionKeys);
        List<Key<Enrollment>> enrollmentKeys = ObjectifyService.ofy().load().type(Enrollment.class).filter("lecture", lectureKey).keys().list();
        ObjectifyService.ofy().delete().keys(enrollmentKeys);
        List<Key<Attendance>> attendanceKeys = ObjectifyService.ofy().load().type(Attendance.class).filter("lecture", lectureKey).keys().list();
        ObjectifyService.ofy().delete().keys(attendanceKeys);
        // Delete lecture
        ObjectifyService.ofy().delete().key(lectureKey);
    }
}
