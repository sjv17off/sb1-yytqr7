package com.example.tasko.controller;

import com.example.tasko.model.User;
import com.example.tasko.service.UserService;
import com.example.tasko.service.TaskService;
import com.example.tasko.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    
    private final UserService userService;
    private final TaskService taskService;
    private final AttendanceService attendanceService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, 
                          Model model, 
                          RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                return "redirect:/login";
            }

            User user = userService.findByUsername(userDetails.getUsername());
            
            if ("ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            
            model.addAttribute("user", user);
            model.addAttribute("tasks", taskService.getUserTasks(user));
            model.addAttribute("todayAttendance", attendanceService.getTodayAttendance(user));
            
            return "dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, 
                               Model model, 
                               RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                return "redirect:/login";
            }

            User user = userService.findByUsername(userDetails.getUsername());
            if (!"ROLE_ADMIN".equals(user.getRole())) {
                return "redirect:/access-denied";
            }
            
            model.addAttribute("user", user);
            // Add admin-specific data here
            return "admin/dashboard";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error loading admin dashboard: " + e.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}