package com.example.guest.controller;

import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class GuestController {

    @Autowired
    private GuestService guestService;

    // 전체 약속 목록 조회 (Appointment Service 의존)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        return guestService.getAllAppointments();
    }

    // 약속 상세 조회 (Appointment Service 의존)
    @GetMapping("/{appointment_id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable String appointment_id) {
        return guestService.getAppointment(appointment_id);
    }

    // 약속 guest 등록
    @PostMapping("/{appointment_id}/guests")
    public ResponseEntity<GuestResponse> createGuest(
            @PathVariable String appointment_id,
            @RequestBody GuestRequest request) {
        GuestResponse response = guestService.createGuest(appointment_id, request);
        return ResponseEntity.ok(response);
    }

    // 약속 guest 전체 조회
    @GetMapping("/{appointment_id}/guests")
    public ResponseEntity<List<GuestResponse>> getGuests(@PathVariable String appointment_id) {
        List<GuestResponse> guests = guestService.getGuests(appointment_id);
        return ResponseEntity.ok(guests);
    }

    // 참가 취소
    @DeleteMapping("/{appointment_id}/guests/{guest_id}")
    public ResponseEntity<Map<String, Object>> deleteGuest(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        boolean deleted = guestService.deleteGuest(appointment_id, guest_id);
        return ResponseEntity.ok(Map.of(
            "message", deleted ? "참가 취소 완료" : "참가 취소 실패",
            "appointment_id", appointment_id,
            "guest_id", guest_id,
            "success", deleted
        ));
    }

    // 참가자 상태 조회
    @GetMapping("/{appointment_id}/guests/{guest_id}/guest_status")
    public ResponseEntity<GuestResponse> getGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        GuestResponse response = guestService.getGuestStatus(appointment_id, guest_id);
        return ResponseEntity.ok(response);
    }

    // 참가자 상태 변경
    @PatchMapping("/{appointment_id}/guests/{guest_id}/guest_status")
    public ResponseEntity<Object> updateGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id,
            @RequestBody GuestRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        try {
            // userId가 없으면 임시로 설정 (실제로는 인증에서 가져와야 함)
            if (userId == null) {
                userId = "temp_user";
            }
            
            GuestResponse response = guestService.updateGuestStatus(appointment_id, guest_id, request, userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("호스트가 아닌 사용자")) {
                return ResponseEntity.status(403).body(Map.of("error", message));
            } else {
                return ResponseEntity.status(400).body(Map.of("error", message));
            }
        }
    }
}
