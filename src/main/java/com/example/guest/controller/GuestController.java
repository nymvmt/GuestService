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
    private AppointmentServiceClient appointmentServiceClient;

    // 전체 약속 목록 조회 (Appointment Service에서 가져옴)
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        try {
            List<AppointmentResponse> appointments = appointmentServiceClient.getAllAppointments();
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            log.error("전체 약속 목록 조회 실패", e);
            throw new RuntimeException("약속 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    // 약속 상세 조회 (Appointment Service에서 가져옴)
    @GetMapping("/{appointment_id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable String appointment_id) {
        try {
            AppointmentResponse appointment = appointmentServiceClient.getAppointmentById(appointment_id);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(appointment);
        } catch (Exception e) {
            log.error("약속 상세 조회 실패 - appointmentId: {}", appointment_id, e);
            throw new RuntimeException("약속 조회에 실패했습니다: " + e.getMessage());
        }
    }

    // 약속 guest 등록
    @PostMapping("/{appointment_id}/guests")
    public ResponseEntity<GuestResponse> createGuest(
            @PathVariable String appointment_id,
            @RequestBody GuestRequest request) {
        
        try {
            // 1. Appointment 존재 여부 확인
            if (!appointmentServiceClient.existsAppointment(appointment_id)) {
                return ResponseEntity.notFound().build();
            }
            
            // 2. UserService에서 사용자 정보 조회하여 검증
            UserResponse userResponse = userServiceClient.getUserById(request.getUser_id());
            if (userResponse == null) {
                return ResponseEntity.badRequest().body(null);
            }
            
            GuestResponse response = guestService.createGuest(appointment_id, request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Guest 등록 실패 - appointmentId: {}, userId: {}", 
                    appointment_id, request.getUser_id(), e);
            throw new RuntimeException("Guest 등록에 실패했습니다: " + e.getMessage());
        }
    }

    // 약속 guest 전체 조회
    @GetMapping("/{appointment_id}/guests")
    public ResponseEntity<List<GuestResponse>> getGuests(@PathVariable String appointment_id) {
        try {
            // Appointment 존재 여부 확인
            if (!appointmentServiceClient.existsAppointment(appointment_id)) {
                return ResponseEntity.notFound().build();
            }
            
            List<GuestResponse> guests = guestService.getGuests(appointment_id);
            return ResponseEntity.ok(guests);
            
        } catch (Exception e) {
            log.error("Guest 목록 조회 실패 - appointmentId: {}", appointment_id, e);
            throw new RuntimeException("Guest 목록 조회에 실패했습니다: " + e.getMessage());
        }
    }

    // 참가 취소
    @DeleteMapping("/{appointment_id}/guests/{guest_id}")
    public ResponseEntity<Map<String, Object>> deleteGuest(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        
        try {
            // Appointment 존재 여부 확인
            if (!appointmentServiceClient.existsAppointment(appointment_id)) {
                return ResponseEntity.notFound().build();
            }
            
            boolean deleted = guestService.deleteGuest(appointment_id, guest_id);
            return ResponseEntity.ok(Map.of(
                "message", deleted ? "참가 취소 완료" : "참가 취소 실패",
                "appointment_id", appointment_id,
                "guest_id", guest_id,
                "success", deleted
            ));
            
        } catch (Exception e) {
            log.error("Guest 삭제 실패 - appointmentId: {}, guestId: {}", 
                    appointment_id, guest_id, e);
            throw new RuntimeException("참가 취소에 실패했습니다: " + e.getMessage());
        }
    }

    // 참가자 상태 조회
    @GetMapping("/{appointment_id}/guests/{guest_id}/guest_status")
    public ResponseEntity<GuestResponse> getGuestStatus(
            @PathVariable String appointment_id,
            @PathVariable String guest_id) {
        
        try {
            // Appointment 존재 여부 확인
            if (!appointmentServiceClient.existsAppointment(appointment_id)) {
                return ResponseEntity.notFound().build();
            }
            
            GuestResponse response = guestService.getGuestStatus(appointment_id, guest_id);
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Guest 상태 조회 실패 - appointmentId: {}, guestId: {}", 
                    appointment_id, guest_id, e);
            throw new RuntimeException("Guest 상태 조회에 실패했습니다: " + e.getMessage());
        }
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
            
            // 1. Appointment 정보 조회 (존재 여부 확인)
            AppointmentResponse appointment = appointmentServiceClient.getAppointmentById(appointment_id);
            if (appointment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // 2. 호스트 권한 확인
            List<AppointmentResponse> hostAppointments = appointmentServiceClient.getAppointmentsByHostId(userId);
            boolean isHost = hostAppointments.stream()
                    .anyMatch(apt -> apt.getAppointmentId().equals(appointment_id));
            
            if (!isHost) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "호스트만 참가자 상태를 변경할 수 있습니다."
                ));
            }
            
            // 3. UserService에서 요청한 사용자 정보 조회하여 검증
            UserResponse userResponse = userServiceClient.getUserById(userId);
            if (userResponse == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "요청한 사용자를 찾을 수 없습니다. User ID: " + userId
                ));
            }
            
            // 4. Guest 상태 변경 (시간 체크는 Appointment 서비스에서 처리)
            GuestResponse response = guestService.updateGuestStatus(appointment_id, guest_id, request, userId);
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message.contains("호스트가 아닌 사용자") || message.contains("호스트만")) {
                return ResponseEntity.status(403).body(Map.of("error", message));
            } else if (message.contains("종료되지 않았습니다") || message.contains("허용되지 않은") || 
                      message.contains("알림 이전") || message.contains("시간")) {
                return ResponseEntity.status(409).body(Map.of("error", message));
            } else {
                return ResponseEntity.status(400).body(Map.of("error", message));
            }
        } catch (Exception e) {
            log.error("Guest 상태 변경 실패 - appointmentId: {}, guestId: {}, userId: {}", 
                    appointment_id, guest_id, userId, e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Guest 상태 변경에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}
