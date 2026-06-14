package com.golf.screen.controller;

import com.golf.screen.dto.JoinPostRequest;
import com.golf.screen.dto.JoinPostResponse;
import com.golf.screen.dto.JoinApplicationResponse;
import com.golf.screen.entity.JoinStatus;
import com.golf.screen.service.JoinPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/joins")
@RequiredArgsConstructor
public class JoinPostController {

    private final JoinPostService joinPostService;

    @PostMapping
    public ResponseEntity<JoinPostResponse> createJoinPost(@RequestBody JoinPostRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JoinPostResponse response = joinPostService.createJoinPost(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JoinPostResponse>> getAllJoinPosts(@RequestParam(required = false) String dong) {
        List<JoinPostResponse> responses = joinPostService.getAllJoinPosts(dong);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JoinPostResponse> getJoinPost(@PathVariable Long id) {
        JoinPostResponse response = joinPostService.getJoinPost(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JoinPostResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam JoinStatus status) {
        JoinPostResponse response = joinPostService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<JoinPostResponse> joinPost(@PathVariable Long id) {
        JoinPostResponse response = joinPostService.joinPost(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<JoinPostResponse> leavePost(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JoinPostResponse response = joinPostService.leavePost(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<Void> applyJoin(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        joinPostService.applyJoin(id, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<List<JoinApplicationResponse>> getApplications(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<JoinApplicationResponse> responses = joinPostService.getApplications(id, principal.getName());
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/applications/{applicationId}/accept")
    public ResponseEntity<JoinPostResponse> acceptApplication(@PathVariable Long applicationId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JoinPostResponse response = joinPostService.acceptApplication(applicationId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable Long applicationId, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        joinPostService.rejectApplication(applicationId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/my-application")
    public ResponseEntity<JoinApplicationResponse> getMyApplication(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JoinApplicationResponse response = joinPostService.getMyApplication(id, principal.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my/created")
    public ResponseEntity<List<JoinPostResponse>> getMyCreatedJoins(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<JoinPostResponse> responses = joinPostService.getMyCreatedJoins(principal.getName());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/applied")
    public ResponseEntity<List<JoinPostResponse>> getMyAppliedJoins(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<JoinPostResponse> responses = joinPostService.getMyAppliedJoins(principal.getName());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/completed")
    public ResponseEntity<List<JoinPostResponse>> getMyCompletedJoins(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<JoinPostResponse> responses = joinPostService.getMyCompletedJoins(principal.getName());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJoinPost(@PathVariable Long id, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        joinPostService.deleteJoinPost(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reserved")
    public ResponseEntity<JoinPostResponse> updateReserved(
            @PathVariable Long id,
            @RequestParam boolean isReserved,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        JoinPostResponse response = joinPostService.updateReserved(id, isReserved, principal.getName());
        return ResponseEntity.ok(response);
    }
}
