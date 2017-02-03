package de.tum.ase.group4.team1.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.*;
import de.tum.ase.group4.team1.utils.GoogleVerificationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class APIController extends BaseController {
    private ObjectMapper mapper = new ObjectMapper();

    @GetMapping({"/","/lectures"})
    @JsonView(Lecture.Default.class)
    public List<Lecture> lectureList(@RequestHeader(value = "Authorization", required = false) String auth){
        checkAuth(auth);
        List<Lecture> lectures = ObjectifyService.ofy().load().type(Lecture.class).list();
        List<Lecture> lecturesWithoutEmptyGroups = new ArrayList<>(lectures.size());
        for(Lecture lecture : lectures) {
            int exerciseGroupCount = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lecture).count();
            if(exerciseGroupCount != 0) {
                lecturesWithoutEmptyGroups.add(lecture);
            }
        }
        for(Lecture lecture : lecturesWithoutEmptyGroups){
            lecture.exerciseGroups = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lecture).list();
            for(ExerciseGroup exerciseGroup : lecture.exerciseGroups) {
                if(aatUser != null) {
                    exerciseGroup.enrollmentStatus = ObjectifyService.ofy().load().type(Enrollment.class)
                            .filter("user", Key.create(aatUser)).filter("lecture", Key.create(lecture))
                            .filter("exerciseGroup", Key.create(exerciseGroup)).count() != 0;
                }
                exerciseGroup.verificationUrl = MvcUriComponentsBuilder.fromMappingName("APIC#verifyAttendance")
                        .arg(0, lecture.getSemester().getName()).arg(1, lecture.getSlug()).arg(2, exerciseGroup.getSlug())
                        .arg(3, "REPLACE_WITH_TOKEN").arg(4, "REPLACE_WITH_MODE").build();
            }
            lecture.sessions = ObjectifyService.ofy().load().type(Session.class).ancestor(lecture).list();
            for(Session session : lecture.sessions) {
                if(aatUser != null){
                    Attendance attendance = ObjectifyService.ofy().load().type(Attendance.class)
                            .filter("user", Key.create(aatUser)).filter("lecture", Key.create(lecture)).filter("session", Key.create(session))
                            .first().now();
                    if(attendance != null) {
                        if(attendance.getVerifiedAt() == null) {
                            session.attendance.put("status", "pending");
                            session.attendance.put("verificationToken", attendance.getVerificationToken());
                        } else {
                            session.attendance.put("status", "verified");
                            session.attendance.put("verificationToken", attendance.getVerificationToken());
                        }
                    } else {
                        session.attendance.put("status", "none");
                    }
                }
                session.attendanceUrl = MvcUriComponentsBuilder.fromMappingName("APIC#attendSession")
                        .arg(0, lecture.getSemester().getName()).arg(1, lecture.getSlug()).arg(2, session.getSlug())
                        .build();
            }
        }
        return lecturesWithoutEmptyGroups;
    }

    @PostMapping("/{semesterSlug}/{lectureSlug}/sessions/{sessionSlug}/attend")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> attendSession(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                                             @PathVariable String sessionSlug, @RequestHeader("Authorization") String auth){
        Map<String, Object> resp = new HashMap<>();
        // Check Auth
        if(!checkAuth(auth)){
            resp.put("status", "error");
            resp.put("message", "Auth failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        // Prepare keys
        Key<Semester> semester = Key.create(Semester.class, semesterSlug);
        Key<Lecture> lecture = Key.create(semester, Lecture.class, lectureSlug);
        Key<Session> session = Key.create(lecture, Session.class, sessionSlug);

        Enrollment enrollment = ObjectifyService.ofy().load().type(Enrollment.class)
                .filter("lecture", lecture).filter("user", Key.create(aatUser)).first().now();
        if(enrollment == null) {
            resp.put("status", "error");
            resp.put("message", "No enrollment found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        Attendance attendance = new Attendance();
        attendance.setUser(Key.create(aatUser));
        attendance.setLecture(lecture);
        attendance.setSession(session);
        attendance.setExerciseGroupOnCreation(enrollment.getExerciseGroup());
        // Save attendance
        ObjectifyService.ofy().save().entity(attendance).now();
        resp.put("status", "success");
        resp.put("verificationToken", attendance.getVerificationToken());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAttendance(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                                                @PathVariable String groupSlug, @RequestParam String token,
                                                                @RequestParam String mode,
                                                                @RequestHeader("Authorization") String auth) {
        Map<String, Object> resp = new HashMap<>();
        if(!checkAuth(auth)){
            resp.put("status", "error");
            resp.put("message", "Auth failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        Key<ExerciseGroup> exerciseGroupKey = Key.create(
                Key.create(
                        Key.create(Semester.class, semesterSlug), Lecture.class, lectureSlug
                ), ExerciseGroup.class, groupSlug);
        Attendance attendance = ObjectifyService.ofy().load().type(Attendance.class)
                .filter("verificationToken", token).first().now();
        if(attendance == null){
            resp.put("status", "error");
            resp.put("message", "Could not find specified attendance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        attendance.setVerifiedAt(new Date());
        attendance.setMode(mode);
        attendance.setExerciseGroupOnVerification(exerciseGroupKey);
        ObjectifyService.ofy().save().entity(attendance);
        resp.put("status", "success");
        return ResponseEntity.ok(resp);
    }

    private boolean checkAuth(String authorizationHeader){
        if(authorizationHeader == null) {
            return false;
        } else if(authorizationHeader.startsWith("Basic ")){
            String encoded = authorizationHeader.substring(authorizationHeader.indexOf(' '));
            String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
            if(decoded.contentEquals("Steve:dummy")){
                return true;
            } else {
                return false;
            }
        } else if(authorizationHeader.startsWith("Bearer ")){
            String token = authorizationHeader.substring(authorizationHeader.indexOf(' ') + 1);
            try {
                URL url = new URL("https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=" + token);
                GoogleVerificationResponse response = mapper.readValue(url, GoogleVerificationResponse.class);
                System.out.println(mapper.writeValueAsString(response));
                if(response.email_verified.contentEquals("true") && response.email != null) {
                    aatUser = ObjectifyService.ofy().load().type(AATUser.class).filter("email", response.email).first().now();
                    if(aatUser == null){
                        System.out.println("User " + response.email + " could not be found");
                        return false;
                    }
                    Logger.getAnonymousLogger().info("Auth with user " + aatUser.getEmail());
                    System.out.println("Auth with user " + aatUser.getEmail());
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            aatUser = null;
        }
        return false;
    }
}
