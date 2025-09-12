package com.example.guest.repository;

import com.example.guest.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, String> {

    /**
     * 약속 ID로 모든 Guest 조회
     */
    @Query("SELECT g FROM Guest g WHERE g.appointment_id = :appointmentId")
    List<Guest> findByAppointmentId(@Param("appointmentId") String appointmentId);

    /**
     * Guest 상태 업데이트
     */
    @Modifying
    @Query("UPDATE Guest g SET g.guest_status = :newStatus, g.updated_at = :updatedAt WHERE g.guest_id = :guestId")
    int updateGuestStatus(@Param("guestId") String guestId, 
                         @Param("newStatus") String newStatus, 
                         @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Guest 존재 여부 확인 (삭제되지 않은 것만)
     */
    @Query("SELECT COUNT(g) > 0 FROM Guest g WHERE g.guest_id = :guestId")
    boolean existsByGuestId(@Param("guestId") String guestId);

    /**
     * 특정 약속의 특정 사용자 Guest 조회
     */
    @Query("SELECT g FROM Guest g WHERE g.appointment_id = :appointmentId AND g.user_id = :userId")
    Optional<Guest> findByAppointmentIdAndUserId(@Param("appointmentId") String appointmentId, 
                                                @Param("userId") String userId);

    /**
     * 특정 약속에 특정 사용자가 이미 참여했는지 확인 (중복 체크용)
     */
    @Query("SELECT COUNT(g) > 0 FROM Guest g WHERE g.appointment_id = :appointmentId AND g.user_id = :userId")
    boolean existsByAppointmentIdAndUserId(@Param("appointmentId") String appointmentId, 
                                          @Param("userId") String userId);

    /**
     * 특정 상태의 Guest들 조회
     */
    @Query("SELECT g FROM Guest g WHERE g.guest_status = :guestStatus")
    List<Guest> findByGuestStatus(@Param("guestStatus") String guestStatus);

    /**
     * 특정 약속의 특정 상태 Guest들 조회
     */
    @Query("SELECT g FROM Guest g WHERE g.appointment_id = :appointmentId AND g.guest_status = :guestStatus")
    List<Guest> findByAppointmentIdAndGuestStatus(@Param("appointmentId") String appointmentId, 
                                                 @Param("guestStatus") String guestStatus);

    /**
     * 특정 사용자가 참여한 모든 Guest 조회
     */
    @Query("SELECT g FROM Guest g WHERE g.user_id = :userId")
    List<Guest> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT g FROM Guest g WHERE g.userId = :userId AND g.guestStatus = :status")
    List<Guest> findByUserIdAndGuestStatus(@Param("userId") String userId, @Param("status") String status);
}
