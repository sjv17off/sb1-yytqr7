package com.example.tasko.service;

import com.example.tasko.model.Attendance;
import com.example.tasko.model.User;
import com.example.tasko.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;

    public Attendance logAttendance(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        if (attendanceRepository.findByUserAndCheckInBetween(user, startOfDay, endOfDay).isPresent()) {
            throw new RuntimeException("Attendance already logged for today");
        }

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setCheckIn(now);
        attendance.setStatus("PRESENT");
        return attendanceRepository.save(attendance);
    }

    public Attendance getTodayAttendance(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        return attendanceRepository.findByUserAndCheckInBetween(user, startOfDay, endOfDay).orElse(null);
    }

    public void checkOut(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.with(LocalTime.MIN);
        LocalDateTime endOfDay = now.with(LocalTime.MAX);

        Attendance attendance = attendanceRepository.findByUserAndCheckInBetween(user, startOfDay, endOfDay)
            .orElseThrow(() -> new RuntimeException("No attendance found for today"));

        attendance.setCheckOut(now);
        attendanceRepository.save(attendance);
    }

    public int countPresentDays(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        return attendanceRepository.countByUserAndCheckInBetweenAndStatus(user, start, end, "PRESENT");
    }

    public double calculateOvertimeHours(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Attendance> attendances = attendanceRepository.findByUserAndCheckInBetweenOrderByCheckInDesc(user, start, end);
        
        double totalOvertime = 0.0;
        for (Attendance attendance : attendances) {
            if (attendance.getCheckOut() != null) {
                LocalDateTime standardEndTime = attendance.getCheckIn().toLocalDate().atTime(17, 0); // 5 PM
                if (attendance.getCheckOut().isAfter(standardEndTime)) {
                    double overtime = java.time.Duration.between(standardEndTime, attendance.getCheckOut()).toHours();
                    totalOvertime += overtime;
                }
            }
        }
        return totalOvertime;
    }

    public List<Attendance> getMonthlyAttendance(User user, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        return attendanceRepository.findByUserAndCheckInBetweenOrderByCheckInDesc(
            user,
            startDate.atStartOfDay(),
            endDate.atTime(LocalTime.MAX)
        );
    }
}