package com.golf.screen.controller;

import com.golf.screen.dto.PlaceSearchResponse;
import com.golf.screen.service.KakaoLocalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final KakaoLocalService kakaoLocalService;

    @GetMapping("/search")
    public ResponseEntity<List<PlaceSearchResponse>> searchScreenGolf(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        List<PlaceSearchResponse> responses = kakaoLocalService.searchScreenGolf(latitude, longitude);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/region")
    public ResponseEntity<String> getRegionDong(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        String dong = kakaoLocalService.getRegionDong(latitude, longitude);
        return ResponseEntity.ok(dong);
    }
}
