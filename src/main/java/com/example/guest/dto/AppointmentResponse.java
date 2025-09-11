package com.example.guest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String appointment_id;
    private String host_id;
    private String host_username;
    private String host_nickname;
    private String title;
    private String description;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private String location_id;
    private String appointment_status;
}