package com.jobconnect.controller;

import com.jobconnect.dto.LoginRequest;
import com.jobconnect.dto.RegisterRequest;
import com.jobconnect.exception.EmailAlreadyExistsException;
import com.jobconnect.model.Role;
import com.jobconnect.model.User;
import com.jobconnect.service.UserService;
import com.jobconnect.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        model.addAttribute("roles", Role.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                            BindingResult bindingResult,
                            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "register";
        }
        try {
            userService.register(request);
        } catch (EmailAlreadyExistsException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("roles", Role.values());
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("loginRequest") LoginRequest request,
                         Model model,
                         HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(request.getEmail());
            String token = jwtUtil.generateToken(userDetails, user.getRole().name());

            Cookie jwtCookie = new Cookie("jwt", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60);
            response.addCookie(jwtCookie);

            return "redirect:/jobs";
        } catch (BadCredentialsException ex) {
            model.addAttribute("errorMessage", "Invalid email or password.");
            return "login";
        }
    }

    // -------- JSON API variants (useful for non-browser clients) --------

    @PostMapping("/api/auth/register")
    @ResponseBody
    public ResponseEntity<?> apiRegister(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request);
            return ResponseEntity.ok(new ApiUserResponse(user.getId(), user.getEmail(), user.getRole().name()));
        } catch (EmailAlreadyExistsException ex) {
            return ResponseEntity.badRequest().body(new ApiError(ex.getMessage()));
        }
    }

    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByEmail(request.getEmail());
            String token = jwtUtil.generateToken(userDetails, user.getRole().name());
            return ResponseEntity.ok(new ApiTokenResponse(token));
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(401).body(new ApiError("Invalid email or password."));
        }
    }

    record ApiTokenResponse(String token) {}
    record ApiUserResponse(String id, String email, String role) {}
    record ApiError(String message) {}
}
