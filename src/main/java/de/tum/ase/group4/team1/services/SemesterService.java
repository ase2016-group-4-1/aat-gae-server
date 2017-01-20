package de.tum.ase.group4.team1.services;

import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.Semester;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SemesterService {
    public Semester loadSemester(String semesterSlug) {
        return ObjectifyService.ofy().load().type(Semester.class).id(semesterSlug).now();
    }

    public List<Semester> getSemesters() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        getCurrentSemester();
        for(int i = 0; i < 4; i++) {
            calendar.add(Calendar.MONTH, 6);
            semesterForDate(calendar.getTime());
        }
        return ObjectifyService.ofy().load().type(Semester.class).order("begin").list();
    }

    public Semester getCurrentSemester() {
        Date now = new Date();
        return semesterForDate(now);
    }

    private Semester semesterForDate(Date date) {
        Semester semester = null;
        // Filtering in-memory as GAE Datastore only supports one inequality filter per query
        List<Semester> semesters = ObjectifyService.ofy().load().type(Semester.class).list();
        for(Semester s: semesters) {
            if(s.getBegin() != null && s.getEnd() != null) {
                if (s.getBegin().before(date) && s.getEnd().after(date)) {
                    semester = s;
                }
            }
        }
        if(semester == null){
            semester = new Semester(date);
            ObjectifyService.ofy().save().entity(semester).now();
        }
        return semester;
    }
}
