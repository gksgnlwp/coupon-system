package com.hwije.coupon_system.simulation;

import com.hwije.coupon_system.coupon.CouponService;
import com.hwije.coupon_system.dto.CreateCouponRequest;
import com.hwije.coupon_system.dto.SimulationResponse;
import com.hwije.coupon_system.entity.Coupon;
import com.hwije.coupon_system.repository.CouponIssueRepository;
import com.hwije.coupon_system.repository.CouponRepository;
import com.hwije.coupon_system.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SimulationServiceTest {

    @Autowired
    private SimulationService simulationService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        couponIssueRepository.deleteAll();
        couponRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private Coupon createCoupon(int quantity) {
        CreateCouponRequest request = new CreateCouponRequest();
        request.setName("시뮬레이션 쿠폰");
        request.setQuantity(quantity);
        return couponService.createCoupon(request);
    }

    @Test
    void 수량보다_많은_인원이_몰려도_수량만큼만_발급된다() {
        Coupon coupon = createCoupon(50);

        SimulationResponse result = simulationService.simulateIssue(coupon.getId(), 300, 50);

        assertEquals(300, result.getRequestCount());
        assertEquals(50, result.getSuccessCount());
        assertEquals(250, result.getFailCount());
        assertEquals(250, result.getFailReasons().get("쿠폰이 모두 소진되었습니다."));
        assertEquals(0, result.getRemainingAfter());
        assertEquals(50, result.getTotalIssuedInDb());
        assertTrue(result.isConsistent());
    }

    @Test
    void 가상_회원은_재사용되고_같은_쿠폰은_중복_발급되지_않는다() {
        Coupon coupon = createCoupon(100);

        simulationService.simulateIssue(coupon.getId(), 30, 10);
        long memberCount = memberRepository.count();

        SimulationResponse second = simulationService.simulateIssue(coupon.getId(), 30, 10);

        assertEquals(memberCount, memberRepository.count());
        assertEquals(0, second.getSuccessCount());
        assertEquals(30, second.getFailReasons().get("이미 발급받은 쿠폰입니다."));
        assertTrue(second.isConsistent());
    }
}
