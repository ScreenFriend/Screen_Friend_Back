package com.golf.screen.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FindPasswordRequest {
    private String email;
    private String phoneNumber;
}
