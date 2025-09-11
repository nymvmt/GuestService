package com.example.guest.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestRequest {
    
    private String user_id;  // Guest 등록 시에만 사용
    private String guest_status;
}
