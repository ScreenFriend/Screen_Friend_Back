package com.golf.screen.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golf.screen.dto.PlaceSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class KakaoLocalService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 인메모리 캐시 정의
    private static final long CACHE_TTL_MS = 10 * 60 * 1000; // 10분 (600,000 ms)
    private final Map<GridKey, CacheEntry> placeCache = new ConcurrentHashMap<>();

    private static class GridKey {
        private final double latitude;
        private final double longitude;

        public GridKey(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridKey gridKey = (GridKey) o;
            return Double.compare(gridKey.latitude, latitude) == 0 &&
                    Double.compare(gridKey.longitude, longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(latitude, longitude);
        }

        @Override
        public String toString() {
            return "[" + latitude + ", " + longitude + "]";
        }
    }

    private static class CacheEntry {
        private final List<PlaceSearchResponse> data;
        private final long timestamp;

        public CacheEntry(List<PlaceSearchResponse> data) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.timestamp > CACHE_TTL_MS;
        }

        public List<PlaceSearchResponse> getData() {
            return data;
        }
    }

    private GridKey getGridKey(double latitude, double longitude) {
        // 소수점 둘째 자리까지 반올림 (약 1.1km 격자 단위)
        double latGrid = Math.round(latitude * 100.0) / 100.0;
        double lonGrid = Math.round(longitude * 100.0) / 100.0;
        return new GridKey(latGrid, lonGrid);
    }

    public List<PlaceSearchResponse> searchScreenGolf(double latitude, double longitude) {
        GridKey key = getGridKey(latitude, longitude);
        CacheEntry entry = placeCache.get(key);

        if (entry != null && !entry.isExpired()) {
            System.out.println("[KakaoLocalService] 캐시 히트! 격자: " + key + ", 기존 데이터 재사용 (크기: " + entry.getData().size() + ")");
            return entry.getData();
        }

        List<PlaceSearchResponse> results = new ArrayList<>();

        if ("YOUR_KAKAO_REST_KEY".equals(kakaoApiKey) || kakaoApiKey == null || kakaoApiKey.trim().isEmpty()) {
            System.err.println("[KakaoLocalService] 카카오 REST API 키가 설정되지 않았습니다. application-secret.properties를 확인해 주세요.");
            return results;
        }

        try {
            // 카카오 로컬 키워드 검색 API 엔드포인트 URI 생성
            URI uri = UriComponentsBuilder.fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json")
                    .queryParam("query", "스크린골프")
                    .queryParam("x", String.valueOf(longitude))
                    .queryParam("y", String.valueOf(latitude))
                    .queryParam("radius", 5000)
                    .queryParam("sort", "distance")
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            System.out.println("[KakaoLocalService] 카카오 API 직접 호출 발생! 격자: " + key + ", 좌표: " + latitude + ", " + longitude);
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode documents = root.path("documents");

                if (documents.isArray()) {
                    for (JsonNode doc : documents) {
                        String id = doc.path("id").asText();
                        String name = doc.path("place_name").asText();
                        double x = doc.path("x").asDouble(); // 경도
                        double y = doc.path("y").asDouble(); // 위도
                        String address = doc.path("address_name").asText();
                        if (address.isEmpty()) {
                            address = doc.path("road_address_name").asText();
                        }
                        String phone = doc.path("phone").asText();

                        results.add(PlaceSearchResponse.builder()
                                .id(id)
                                .name(name)
                                .latitude(y)
                                .longitude(x)
                                .address(address)
                                .phone(phone)
                                .build());
                    }
                }
            }

            // 결과를 캐시에 저장 (캐시 미스 해결)
            if (!results.isEmpty()) {
                placeCache.put(key, new CacheEntry(results));
            }
        } catch (Exception e) {
            System.err.println("[KakaoLocalService] 카카오 API 호출 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    public String getRegionDong(double latitude, double longitude) {
        if ("YOUR_KAKAO_REST_KEY".equals(kakaoApiKey) || kakaoApiKey == null || kakaoApiKey.trim().isEmpty()) {
            System.err.println("[KakaoLocalService] 카카오 REST API 키가 설정되지 않았습니다.");
            return "장안동";
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString("https://dapi.kakao.com/v2/local/geo/coord2regioncode.json")
                    .queryParam("x", String.valueOf(longitude))
                    .queryParam("y", String.valueOf(latitude))
                    .build()
                    .encode()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode documents = root.path("documents");

                if (documents.isArray()) {
                    for (JsonNode doc : documents) {
                        String regionType = doc.path("region_type").asText();
                        if ("H".equals(regionType)) {
                            String region3 = doc.path("region_3depth_name").asText();
                            if (region3 != null && !region3.isEmpty()) {
                                return region3.replaceAll("\\d+", "");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[KakaoLocalService] 카카오 행정동 변환 API 호출 중 오류: " + e.getMessage());
            e.printStackTrace();
        }

        return "기타";
    }
}
