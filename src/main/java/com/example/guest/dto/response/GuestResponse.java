package com.example.guest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestResponse {
    
    private String guest_id;
    private String appointment_id;
    private String user_id;
    private String guest_status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
