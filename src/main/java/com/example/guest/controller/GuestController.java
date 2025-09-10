package com.example.guest.controller;

import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.service.GuestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class GuestController {

    @Autowired
    private GuestService guestService;

    // 전체 약속 목록 조회 (Appointment Service 의존)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        // TODO: Appointment Service 호출 구현
        return ResponseEntity.ok(Map.of("message", "전체 약속 목록 조회 - 구현 예정"));
    }

    // 약속 상세 조회 (Appointment Service 의존)
    @GetMapping("/{appointment_id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable String appointment_id) {
        // TODO: Appointment Service 호출 구현
        return ResponseEntity.ok(Map.of(
            "message", "약속 상세 조회 - 구현 예정",
            "appointment_id", appointment_id
        ));
    }

    // 약속 guest 등록
    @PostMapping("/{appointment_id}/guests")
    public ResponseEntity<GuestResponse> createGuest(
            @PathVariable String appointment_id,
            @RequestBody GuestRequest request) {

        // 검증 로직 추가
        if (request.getUser_id() == null || request.getUser_id().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "user_id는 필수입니다"));
        }

        if (request.getGuest_status() != null &&
            !Arrays.asList("coming", "came", "noshow").contains(request.getGuest_status())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "guest_status는 coming, came, noshow 중 하나여야 합니다"));
        }

        // 검증 통과 후 비즈니스 로직 실행
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
    public ResponseEntity<GuestResponse> updateGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id,
            @RequestBody GuestRequest request) {
        GuestResponse response = guestService.updateGuestStatus(appointment_id, guest_id, request);
        return ResponseEntity.ok(response);
    }
}
