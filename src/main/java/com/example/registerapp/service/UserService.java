package com.example.registerapp.service;

import com.example.registerapp.custumeRuntimeException.CustomRuntimeException;
import com.example.registerapp.dto.LoginDto;
import com.example.registerapp.dto.RegisterDto;
import com.example.registerapp.entity.User;
import com.example.registerapp.repository.UserRepository;
import com.example.registerapp.util.EmailUtil;
import com.example.registerapp.util.OtpUtil;
import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private OtpUtil otpUtil;
  @Autowired
  private EmailUtil emailUtil;
  @Autowired
  private UserRepository userRepository;

  public String register(RegisterDto registerDto) {
    Optional<User> existingUser = userRepository.findByEmail(registerDto.getEmail());
    if (existingUser.isPresent()) {
      return "User with this email already exists";}
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(registerDto.getEmail(), otp);
    } catch (MessagingException e) {
      throw new CustomRuntimeException("Unable to send otp please try again");
    }
    User user = new User();
    user.setName(registerDto.getName());
    user.setEmail(registerDto.getEmail());
    user.setPassword(registerDto.getPassword());
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "User registration successful";
  }

  public String verifyAccount(String email, String otp) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomRuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
        LocalDateTime.now()).getSeconds() < (5 * 60)) {
      user.setActive(true);
      userRepository.save(user);
      return "OTP verified you can login";
    }
    return "Please regenerate otp and try again";
  }

  public String regenerateOtp(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomRuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(email, otp);
    } catch (MessagingException e) {
      throw new CustomRuntimeException("Unable to send otp please try again");
    }
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 1 minute";
  }

  public String login(LoginDto loginDto) {
    User user = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(
            () -> new CustomRuntimeException("User not found with this email: " + loginDto.getEmail()));
    if (!loginDto.getPassword().equals(user.getPassword())) {
      return "Password is incorrect";
    } else if (!user.isActive()) {
      return "your account is not verified";
    }
    return "Login successful";
  }

  public String forgotPassword(String email) {
    // 1. Check if email exists in the system.
    // 2. Generate OTP.
    // 3. Send OTP to user's email.
    // 4. Store the OTP with its expiration time (consider using a cache or a temporary store).
    // 5. Return a relevant message.
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomRuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.resetPassword(email, otp);
    } catch (MessagingException e) {
      throw new CustomRuntimeException("Unable to send otp please try again");
    }
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 2 minute with set newPassword";

  }

  public String resetPassword(String email, String otp, String newPassword) {
    // 1. Check if email exists in the system.

    // 2. Check if provided OTP matches the one stored and has not expired.
    // 3. If valid, reset the password.
    // 4. Delete or invalidate the OTP.
    // 5. Return a relevant message.
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new CustomRuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
            LocalDateTime.now()).getSeconds() < (5 * 60)) {
      user.setPassword(newPassword);
      //user.setEmail(email);
      //user.setOtp(otp);

      userRepository.save(user);
  }
    return "Password reset successful!";
  }

}
