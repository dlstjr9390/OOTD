package com.sparta_a5.ootd.user.controller;

import com.sparta_a5.ootd.CommonResponseDTO;
import com.sparta_a5.ootd.user.dto.*;
import com.sparta_a5.ootd.common.configuration.JwtUtil;
import com.sparta_a5.ootd.user.security.UserDetailsImpl;
import com.sparta_a5.ootd.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<CommonResponseDTO> signup(@Valid @RequestBody UserRequestDto userRequestDto) {
        try {
            userService.signup(userRequestDto);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(new CommonResponseDTO("중복된 username 입니다.", HttpStatus.BAD_REQUEST.value()));
        }

        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(new CommonResponseDTO("회원가입을 환영합니다.", HttpStatus.CREATED.value()));
    }

    @PostMapping("/login")
    public ResponseEntity<CommonResponseDTO> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse response) {
        try {
            userService.login(loginRequestDto, response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new CommonResponseDTO("로그인에 실패하였습니다.", HttpStatus.BAD_REQUEST.value()));
        }

        return ResponseEntity.ok().body(new CommonResponseDTO("로그인에 성공하였습니다.", HttpStatus.OK.value()));
    }

    /*@PostMapping("/logout")
    public ResponseEntity<CommonResponseDto> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok().body(new CommonResponseDto("로그아웃 하였습니다.", HttpStatus.OK.value()));
    } */

    @GetMapping("/profile/{userId}") // username으로 조회
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok().body(userService.getUserByUsername(userId));
    }

    @PutMapping("/profile/{userId}") // username으로 수정
    public ResponseEntity<CommonResponseDTO> updateUser(@RequestBody UpdateRequestDto updateRequestDto, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        userService.updateUser(updateRequestDto,  userDetails);
        return ResponseEntity.ok().body(new CommonResponseDTO("프로필 수정을 성공하였습니다.", HttpStatus.BAD_REQUEST.value()));
    }
}
