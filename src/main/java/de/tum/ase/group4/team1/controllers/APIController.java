package de.tum.ase.group4.team1.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.appengine.api.users.User;
import com.google.appengine.repackaged.com.google.api.client.util.Base64;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import de.tum.ase.group4.team1.models.Attendance;
import de.tum.ase.group4.team1.models.ExerciseGroup;
import de.tum.ase.group4.team1.models.Lecture;
import de.tum.ase.group4.team1.models.Semester;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class APIController extends BaseController {
    @GetMapping("/lectures")
    @JsonView(Lecture.Default.class)
    public List<Lecture> lectureList(){
        List<Lecture> lectures = ObjectifyService.ofy().load().type(Lecture.class).list();
        List<Lecture> lecturesWithoutEmptyGroups = new ArrayList<>(lectures.size());
        for(Lecture lecture : lectures) {
            int exerciseGroupCount = ObjectifyService.ofy().load().type(ExerciseGroup.class).ancestor(lecture).count();
            if(exerciseGroupCount != 0) {
                lecturesWithoutEmptyGroups.add(lecture);
            }
        }
        return lecturesWithoutEmptyGroups;
    }

    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyAttendance(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                                                @PathVariable String groupSlug, @RequestParam String token,
                                                                @RequestParam String mode,
                                                                @RequestHeader("Authorization") String auth) {
        Map<String, Object> resp = new HashMap<>();
        if(!checkBasicAuth(auth)){
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

    // TODO remove this - temporary endpoint for testing the verification
    @PostMapping("/{semesterSlug}/{lectureSlug}/groups/{groupSlug}/unverify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unverifyAttendance(@PathVariable String semesterSlug, @PathVariable String lectureSlug,
                                                                  @PathVariable String groupSlug, @RequestParam String token,
                                                                  @RequestParam String mode,
                                                                  @RequestHeader("Authorization") String auth) {
        Map<String, Object> resp = new HashMap<>();
        if(!checkBasicAuth(auth)){
            resp.put("status", "error");
            resp.put("message", "Auth failed");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
        }
        Attendance attendance = ObjectifyService.ofy().load().type(Attendance.class)
                .filter("verificationToken", token).first().now();
        if(attendance == null){
            resp.put("status", "error");
            resp.put("message", "Could not find specified attendance");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        attendance.setVerifiedAt(null);
        attendance.setMode(null);
        attendance.setExerciseGroupOnVerification(null);
        ObjectifyService.ofy().save().entity(attendance);
        resp.put("status", "success");
        return ResponseEntity.ok(resp);
    }

    // TODO: remove this - temporary endpoint for testing auth from the android app
    @GetMapping("/currentUserInfo")
    public User currentUserInfo() {
        return user;
    }

    private boolean checkBasicAuth(String authorizationHeader){
        if(authorizationHeader == null || !authorizationHeader.startsWith("Basic ")){
            return false;
        }
        String encoded = authorizationHeader.substring(authorizationHeader.indexOf(' '));
        String decoded = new String(Base64.decodeBase64(encoded.getBytes()));
        if(decoded.contentEquals("Steve:dummy")){
            return true;
        } else {
            return false;
        }
    }
}
