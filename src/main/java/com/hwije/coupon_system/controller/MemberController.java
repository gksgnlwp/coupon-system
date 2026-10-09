package com.hwije.coupon_system.controller;

import com.hwije.coupon_system.dto.SignupRequest;
import com.hwije.coupon_system.dto.SignupResponse;
import com.hwije.coupon_system.entity.Member;
import com.hwije.coupon_system.service.MemberService;
import org.springframework.web.bind.annotation.*;
import com.hwije.coupon_system.dto.LoginRequest;
import com.hwije.coupon_system.dto.LoginResponse;
import com.hwije.coupon_system.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import com.hwije.coupon_system.dto.MeResponse;
@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public MemberController(MemberService memberService,
                            JwtProvider jwtProvider) {

        this.memberService = memberService;
        this.jwtProvider = jwtProvider;
    }

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {

        Member member = memberService.signup(
                request.getEmail(),
                request.getPassword(),
                request.getName()
        );

        return new SignupResponse(
                member.getId(),
                "회원가입이 완료되었습니다."
        );
    }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Member member = memberService.login(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtProvider.createToken(member.getEmail());

        return new LoginResponse(
                "로그인 성공",
                token
        );
    }
    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {

        return new MeResponse(authentication.getName());
    }
}