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

// Appointment Service 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AppointmentResponse {
    private String appointment_id;
    private String host_id;
    private String title;
    private String description;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private String location_id;
    private String appointment_status;
}

// Appointment 목록 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
class AppointmentListResponse {
    private java.util.List<AppointmentResponse> appointments;
    private int total;
    private int page;
    private int size;
}