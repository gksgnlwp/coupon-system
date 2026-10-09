package com.hwije.coupon_system.controller;

import com.hwije.coupon_system.coupon.CouponService;
import com.hwije.coupon_system.dto.CreateCouponRequest;
import com.hwije.coupon_system.dto.CreateCouponResponse;
import com.hwije.coupon_system.dto.IssueCouponResponse;
import com.hwije.coupon_system.dto.MyCouponResponse;
import com.hwije.coupon_system.entity.Coupon;
import com.hwije.coupon_system.entity.CouponIssue;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.hwije.coupon_system.dto.CouponResponse;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @PostMapping
    public CreateCouponResponse createCoupon(
            @Valid @RequestBody CreateCouponRequest request
    ) {

        Coupon coupon = couponService.createCoupon(request);

        return new CreateCouponResponse(
                coupon.getId(),
                "쿠폰이 생성되었습니다."
        );
    }
    @GetMapping
    public List<CouponResponse> getCoupons() {
        return couponService.getCoupons();
    }

    @PostMapping("/{couponId}/issue")
    public IssueCouponResponse issueCoupon(
            @PathVariable Long couponId,
            Authentication authentication
    ) {

        CouponIssue couponIssue = couponService.issueCoupon(
                couponId,
                authentication.getName()
        );

        return new IssueCouponResponse(
                couponIssue.getId(),
                "쿠폰이 발급되었습니다."
        );
    }

    @GetMapping("/me")
    public List<MyCouponResponse> getMyCoupons(Authentication authentication) {
        return couponService.getMyCoupons(authentication.getName());
    }
}
