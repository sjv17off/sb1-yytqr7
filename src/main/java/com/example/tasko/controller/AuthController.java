package com.example.tasko.controller;

import com.example.tasko.model.User;
import com.example.tasko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            if (userService.existsByUsername(user.getUsername())) {
                redirectAttributes.addFlashAttribute("error", "Username already exists");
                return "redirect:/register";
            }
            
            if (!user.getPassword().equals(user.getConfirmPassword())) {
                redirectAttributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }
            
            user.setRole("ROLE_USER"); // Fixed: Added ROLE_ prefix
            user.setActive(true);
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }
}