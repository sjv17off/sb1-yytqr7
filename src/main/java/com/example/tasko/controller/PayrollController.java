package com.example.tasko.controller;

import com.example.tasko.model.Enterprise;
import com.example.tasko.model.Payroll;
import com.example.tasko.model.User;
import com.example.tasko.service.EnterpriseService;
import com.example.tasko.service.PayrollService;
import com.example.tasko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService payrollService;
    private final UserService userService;
    private final EnterpriseService enterpriseService;

    @GetMapping("/dashboard")
    public String payrollDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByUsername(userDetails.getUsername());
        if (user.getRole().equals("ADMIN")) {
            List<Enterprise> enterprises = enterpriseService.getAllEnterprises();
            model.addAttribute("enterprises", enterprises);
        } else {
            List<Payroll> payrollHistory = payrollService.getPayrollHistory(user);
            model.addAttribute("payrollHistory", payrollHistory);
        }
        return user.getRole().equals("ADMIN") ? "admin/payroll" : "user/payroll";
    }

    @PostMapping("/generate")
    @ResponseBody
    public ResponseEntity<?> generatePayroll(@RequestBody Map<String, Object> request) {
        try {
            int year = (int) request.get("year");
            int month = (int) request.get("month");
            Long enterpriseId = request.get("enterpriseId") != null ? 
                Long.parseLong(request.get("enterpriseId").toString()) : null;

            if (enterpriseId != null) {
                Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
                List<Payroll> payrolls = payrollService.generateEnterprisePayroll(enterprise, year, month);
                return ResponseEntity.ok(payrolls);
            } else {
                return ResponseEntity.badRequest().body("Enterprise ID is required");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserPayroll(
            @PathVariable Long userId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            User user = userService.findById(userId);
            Payroll payroll = payrollService.generatePayroll(user, year, month);
            return ResponseEntity.ok(payroll);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/enterprise/{enterpriseId}")
    @ResponseBody
    public ResponseEntity<?> getEnterprisePayroll(
            @PathVariable Long enterpriseId,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
            List<Payroll> payrolls = payrollService.getEnterprisePayroll(enterprise, year, month);
            return ResponseEntity.ok(payrolls);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}