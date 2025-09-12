package com.example.guest.controller;

import com.example.guest.client.AppointmentServiceClient;
import com.example.guest.client.UserServiceClient;
import com.example.guest.dto.AppointmentResponse;
import com.example.guest.dto.UserResponse;
import com.example.guest.dto.request.GuestRequest;
import com.example.guest.dto.response.GuestResponse;
import com.example.guest.service.GuestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
@Slf4j
public class GuestController {

    @Autowired
    private GuestService guestService;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private AppointmentServiceClient appointmentServiceClient; // Appointment 서비스 클라이언트 추가

    // 전체 약속 목록 조회 (Appointment Service 의존)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllAppointments() {
        // AppointmentService에서 직접 데이터 조회
        List<AppointmentResponse> appointments = appointmentServiceClient.getAllAppointments();
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "전체 약속 목록 조회 성공",
            "data", appointments,
            "count", appointments != null ? appointments.size() : 0
        ));
    }

    // 약속 상세 조회 (Appointment Service 의존)
    @GetMapping("/{appointment_id}")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable String appointment_id) {
        // AppointmentService에서 직접 데이터 조회
        AppointmentResponse appointment = appointmentServiceClient.getAppointmentById(appointment_id);
        
        if (appointment == null) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "약속을 찾을 수 없습니다",
                "appointment_id", appointment_id
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "약속 상세 조회 성공",
            "data", appointment
        ));
    }

    // 약속 guest 등록
    @PostMapping("/{appointment_id}/guests")
    public ResponseEntity<GuestResponse> createGuest(
            @PathVariable String appointment_id,
            @RequestBody GuestRequest request) {
        
        // 1. AppointmentService에서 약속 존재 여부 확인
        if (!appointmentServiceClient.existsAppointment(appointment_id)) {
            throw new RuntimeException("약속을 찾을 수 없습니다. Appointment ID: " + appointment_id);
        }
        
        // 2. UserService에서 사용자 정보 조회하여 검증 (기존 코드 유지)
        UserResponse userResponse = userServiceClient.getUserById(request.getUser_id());
        if (userResponse == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다. User ID: " + request.getUser_id());
        }
        
        GuestResponse response = guestService.createGuest(appointment_id, request);
        return ResponseEntity.ok(response);
    }

    // 약속 guest 전체 조회
    @GetMapping("/{appointment_id}/guests")
    public ResponseEntity<List<GuestResponse>> getGuests(@PathVariable String appointment_id) {
        // AppointmentService에서 약속 존재 여부 확인
        if (!appointmentServiceClient.existsAppointment(appointment_id)) {
            throw new RuntimeException("약속을 찾을 수 없습니다. Appointment ID: " + appointment_id);
        }
        
        List<GuestResponse> guests = guestService.getGuests(appointment_id);
        return ResponseEntity.ok(guests);
    }

    // 약속 개별 조회
    @GetMapping("/{appointment_id}/guests/{guest_id}")
    public ResponseEntity<GuestResponse> getGuest(@PathVariable String appointment_id, @PathVariable String guest_id) {
        GuestResponse guest = guestService.getGuest(appointment_id, guest_id);
        return ResponseEntity.ok(guest);
    }

    // 참가 취소
    @DeleteMapping("/{appointment_id}/guests/{guest_id}")
    public ResponseEntity<Map<String, Object>> deleteGuest(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        
        // AppointmentService에서 약속 존재 여부 확인
        if (!appointmentServiceClient.existsAppointment(appointment_id)) {
            return ResponseEntity.status(404).body(Map.of(
                "message", "약속을 찾을 수 없습니다",
                "appointment_id", appointment_id,
                "success", false
            ));
        }
        
        boolean deleted = guestService.deleteGuest(appointment_id, guest_id);
        return ResponseEntity.ok(Map.of(
            "message", deleted ? "참가 취소 완료" : "참가 취소 실패",
            "appointment_id", appointment_id,
            "guest_id", guest_id,
            "success", deleted
        ));
    }

    // 참가자 상태 조회 (상태만 String으로 반환)
    @GetMapping("/{appointment_id}/guests/{guest_id}/guest_status")
    public ResponseEntity<Map<String, Object>> getGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        
        log.info("🚀 [API 요청 시작] GET /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}", 
                appointment_id, appointment_id, guest_id, appointment_id, guest_id);
        
        try {
            // Guest Service에서 상태만 조회 (getGuest 메서드 재사용)
            log.info("🔍 [Guest Service] 상태 조회 시작 - appointment_id: {}, guest_id: {}", appointment_id, guest_id);
            
            String status = guestService.getGuestStatus(appointment_id, guest_id);
            
            // 상태만 포함한 응답 생성
            Map<String, Object> response = Map.of(
                "guest_status", status
            );
            
            log.info("✅ [API 요청 성공] GET /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}, status: {}", 
                    appointment_id, appointment_id, guest_id, appointment_id, guest_id, status);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("💥 [API 요청 실패] GET /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}, error: {}", 
                    appointment_id, appointment_id, guest_id, appointment_id, guest_id, e.getMessage(), e);
            
            return ResponseEntity.status(400).body(Map.of(
                "error", e.getMessage(),
                "appointment_id", appointment_id,
                "guest_id", guest_id
            ));
        }
    }

    // 특정 사용자가 참여한 모든 게스트 정보 조회
    @GetMapping("/guests/user/{user_id}")
    public ResponseEntity<List<GuestResponse>> getGuestsByUserId(@PathVariable String user_id) {
        log.info("🚀 [API 요청 시작] GET /appointments/guests/user/{} - user_id: {}", user_id, user_id);
        
        try {
            List<GuestResponse> guests = guestService.getGuestsByUserId(user_id);
            
            log.info("✅ [API 요청 성공] GET /appointments/guests/user/{} - user_id: {}, 게스트 수: {}", 
                    user_id, user_id, guests.size());
            
            return ResponseEntity.ok(guests);
            
        } catch (Exception e) {
            log.error("💥 [API 요청 실패] GET /appointments/guests/user/{} - user_id: {}, error: {}", 
                    user_id, user_id, e.getMessage(), e);
            
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // 참가자 상태 변경
    @PatchMapping("/{appointment_id}/guests/{guest_id}/guest_status")
    public ResponseEntity<Object> updateGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id,
            @RequestBody GuestRequest request,
            @RequestHeader(value = "X-User-ID", required = false) String userId) {
        
        log.info("🚀 [API 요청 시작] PATCH /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}, userId: {}, newStatus: {}", 
                appointment_id, appointment_id, guest_id, appointment_id, guest_id, userId, request.getGuest_status());
        
        try {
            // AppointmentService에서 약속 존재 여부 확인
            if (!appointmentServiceClient.existsAppointment(appointment_id)) {
                return ResponseEntity.status(404).body(Map.of("error", "약속을 찾을 수 없습니다"));
            }
            
            // userId가 없으면 에러 발생
            if (userId == null) {
                log.error("❌ [API 요청 실패] X-User-ID 헤더가 누락됨 - appointment_id: {}, guest_id: {}", appointment_id, guest_id);
                return ResponseEntity.status(400).body(Map.of(
                    "error", "X-User-ID 헤더가 필요합니다",
                    "appointment_id", appointment_id,
                    "guest_id", guest_id
                ));
            }
            
            // UserService에서 요청한 사용자 정보 조회하여 검증 (기존 코드 유지)
            UserResponse userResponse = userServiceClient.getUserById(userId);
            if (userResponse == null) {
                throw new RuntimeException("요청한 사용자를 찾을 수 없습니다. User ID: " + userId);
            }
            
            log.info("🔍 [Guest Service] 상태 변경 시작 - appointment_id: {}, guest_id: {}, userId: {}", appointment_id, guest_id, userId);
            
            GuestResponse response = guestService.updateGuestStatus(appointment_id, guest_id, request, userId);
            
            log.info("✅ [API 요청 성공] PATCH /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}, newStatus: {}", 
                    appointment_id, appointment_id, guest_id, appointment_id, guest_id, response.getGuest_status());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "참가자 상태 변경 완료",
                "data", response
            ));
            
        } catch (RuntimeException e) {
            String message = e.getMessage();
            log.error("💥 [API 요청 실패] PATCH /appointments/{}/guests/{}/guest_status - appointment_id: {}, guest_id: {}, userId: {}, error: {}", 
                    appointment_id, appointment_id, guest_id, appointment_id, guest_id, userId, message, e);
            
            if (message.contains("호스트가 아닌 사용자")) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "error", message,
                    "appointment_id", appointment_id,
                    "guest_id", guest_id
                ));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "error", message,
                    "appointment_id", appointment_id,
                    "guest_id", guest_id
                ));
            }
        }
    }
}
