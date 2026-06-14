package com.golf.screen.controller;

import com.golf.screen.dto.MannerReviewRequest;
import com.golf.screen.dto.ParticipantResponse;
import com.golf.screen.service.MannerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class MannerReviewController {

    private final MannerReviewService mannerReviewService;

    @GetMapping("/participants/{joinPostId}")
    public ResponseEntity<List<ParticipantResponse>> getParticipantsToReview(
            @PathVariable Long joinPostId,
            Principal principal
    ) {
        List<ParticipantResponse> response = mannerReviewService.getParticipantsToReview(joinPostId, principal.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Void> createMannerReview(
            @RequestBody MannerReviewRequest request,
            Principal principal
    ) {
        mannerReviewService.createMannerReview(request, principal.getName());
        return ResponseEntity.ok().build();
    }
}
