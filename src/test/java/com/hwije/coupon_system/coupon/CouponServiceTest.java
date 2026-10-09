package com.hwije.coupon_system.coupon;

import com.hwije.coupon_system.dto.CreateCouponRequest;
import com.hwije.coupon_system.entity.Coupon;
import com.hwije.coupon_system.repository.CouponIssueRepository;
import com.hwije.coupon_system.repository.CouponRepository;
import com.hwije.coupon_system.repository.MemberRepository;
import com.hwije.coupon_system.service.MemberService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class CouponServiceTest {

    @Autowired
    private CouponService couponService;

    @Autowired
    private MemberService memberService;

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
        request.setName("테스트 쿠폰");
        request.setQuantity(quantity);
        return couponService.createCoupon(request);
    }

    @Test
    void 쿠폰을_발급하면_재고가_줄어든다() {
        memberService.signup("a@test.com", "1234", "a");
        Coupon coupon = createCoupon(10);

        couponService.issueCoupon(coupon.getId(), "a@test.com");

        Coupon found = couponRepository.findById(coupon.getId()).orElseThrow();
        assertEquals(9, found.getRemainingQuantity());
        assertEquals(1, couponService.getMyCoupons("a@test.com").size());
    }

    @Test
    void 같은_쿠폰은_중복_발급되지_않는다() {
        memberService.signup("a@test.com", "1234", "a");
        Coupon coupon = createCoupon(10);

        couponService.issueCoupon(coupon.getId(), "a@test.com");

        assertThrows(IllegalStateException.class,
                () -> couponService.issueCoupon(coupon.getId(), "a@test.com"));
    }

    @Test
    void 재고가_소진되면_발급되지_않는다() {
        memberService.signup("a@test.com", "1234", "a");
        memberService.signup("b@test.com", "1234", "b");
        Coupon coupon = createCoupon(1);

        couponService.issueCoupon(coupon.getId(), "a@test.com");

        assertThrows(IllegalStateException.class,
                () -> couponService.issueCoupon(coupon.getId(), "b@test.com"));
    }

    @Test
    void 동시에_요청해도_수량만큼만_발급된다() throws InterruptedException {
        int quantity = 30;
        int requestCount = 100;

        for (int i = 0; i < requestCount; i++) {
            memberService.signup("user" + i + "@test.com", "1234", "user" + i);
        }
        Coupon coupon = createCoupon(quantity);

        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(requestCount);
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < requestCount; i++) {
            String email = "user" + i + "@test.com";
            executor.submit(() -> {
                try {
                    couponService.issueCoupon(coupon.getId(), email);
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        Coupon found = couponRepository.findById(coupon.getId()).orElseThrow();
        assertEquals(quantity, successCount.get());
        assertEquals(0, found.getRemainingQuantity());
        assertEquals(quantity, couponIssueRepository.countByCouponId(coupon.getId()));
    }
}
