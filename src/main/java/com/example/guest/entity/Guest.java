package com.example.guest.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "guests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {
    
    @Id
    @Column(name = "guest_id")
    private String guest_id;
    
    @Column(name = "appointment_id")
    private String appointment_id;
    
    @Column(name = "user_id")
    private String user_id;
    
    @Column(name = "guest_status")
    private String guest_status = "coming";  // 기본값 설정
    
    @Column(name = "created_at")
    private LocalDateTime created_at;
    
    @Column(name = "updated_at")
    private LocalDateTime updated_at;
}
