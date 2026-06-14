package com.golf.screen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceSearchResponse {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String phone;
}
