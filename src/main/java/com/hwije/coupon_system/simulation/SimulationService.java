package com.hwije.coupon_system.simulation;

import com.hwije.coupon_system.coupon.CouponService;
import com.hwije.coupon_system.dto.SimulationResponse;
import com.hwije.coupon_system.entity.Coupon;
import com.hwije.coupon_system.entity.Member;
import com.hwije.coupon_system.repository.CouponIssueRepository;
import com.hwije.coupon_system.repository.CouponRepository;
import com.hwije.coupon_system.repository.MemberRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 가상 회원 여러 명이 같은 쿠폰을 동시에 발급받는 상황을 서버 안에서 재현한다.
 * 브라우저는 서버당 동시 요청 수가 제한되어 있어서, 진짜 동시 요청은 서버 스레드로 만든다.
 */
@Service
@ConditionalOnProperty(name = "simulation.enabled", havingValue = "true")
public class SimulationService {

    public static final int MAX_USERS = 10000;
    public static final int MAX_THREADS = 200;

    private static final String EMAIL_PREFIX = "sim-user-";
    private static final String EMAIL_DOMAIN = "@sim.test";

    private final CouponService couponService;
    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 시뮬레이션끼리 겹치면 결과를 해석할 수 없으므로 한 번에 하나만 실행
    private final AtomicBoolean running = new AtomicBoolean(false);

    public SimulationService(CouponService couponService,
                             CouponRepository couponRepository,
                             CouponIssueRepository couponIssueRepository,
                             MemberRepository memberRepository,
                             PasswordEncoder passwordEncoder) {
        this.couponService = couponService;
        this.couponRepository = couponRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public SimulationResponse simulateIssue(Long couponId, int userCount, int threadCount) {

        if (userCount < 1 || userCount > MAX_USERS) {
            throw new IllegalArgumentException("인원은 1~" + MAX_USERS + "명 사이여야 합니다.");
        }
        if (threadCount < 1 || threadCount > MAX_THREADS) {
            throw new IllegalArgumentException("스레드 수는 1~" + MAX_THREADS + "개 사이여야 합니다.");
        }
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("이미 시뮬레이션이 실행 중입니다. 잠시 후 다시 시도하세요.");
        }

        try {
            Coupon before = couponRepository.findById(couponId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

            List<String> emails = prepareUsers(userCount);

            return run(before, emails, Math.min(threadCount, userCount));
        } finally {
            running.set(false);
        }
    }

    private List<String> prepareUsers(int userCount) {

        List<String> emails = new ArrayList<>(userCount);
        for (int i = 1; i <= userCount; i++) {
            emails.add(String.format("%s%05d%s", EMAIL_PREFIX, i, EMAIL_DOMAIN));
        }

        Set<String> existing = new HashSet<>(memberRepository.findEmailsStartingWith(EMAIL_PREFIX));

        // BCrypt 는 일부러 느린 연산이라 가상 회원 전체가 같은 해시를 공유한다 (비밀번호: 1234)
        String password = passwordEncoder.encode("1234");
        LocalDateTime now = LocalDateTime.now();

        List<Member> newMembers = new ArrayList<>();
        for (String email : emails) {
            if (existing.contains(email)) {
                continue;
            }
            Member member = new Member();
            member.setEmail(email);
            member.setPassword(password);
            member.setName("가상회원");
            member.setCreatedAt(now);
            newMembers.add(member);
        }
        memberRepository.saveAll(newMembers);

        return emails;
    }

    private SimulationResponse run(Coupon before, List<String> emails, int threadCount) {

        Long couponId = before.getId();
        int quantity = before.getQuantity();
        int remainingBefore = couponRepository.findRemainingQuantityById(couponId).orElseThrow();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(emails.size());

        AtomicInteger successCount = new AtomicInteger();
        Map<String, AtomicInteger> failReasons = new ConcurrentHashMap<>();

        try {
            // 스레드를 모두 띄워 놓고 출발 신호를 기다리게 해서 첫 요청들이 정말 동시에 들어가게 한다
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            ready.await();

            for (String email : emails) {
                executor.submit(() -> {
                    try {
                        couponService.issueCoupon(couponId, email);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        String reason = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                        failReasons.computeIfAbsent(reason, k -> new AtomicInteger()).incrementAndGet();
                    } finally {
                        done.countDown();
                    }
                });
            }

            long startedAt = System.nanoTime();
            start.countDown();
            done.await();
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;

            int remainingAfter = couponRepository.findRemainingQuantityById(couponId).orElseThrow();
            long totalIssued = couponIssueRepository.countByCouponId(couponId);

            Map<String, Integer> reasons = new TreeMap<>();
            failReasons.forEach((k, v) -> reasons.put(k, v.get()));

            return new SimulationResponse(
                    couponId,
                    emails.size(),
                    threadCount,
                    successCount.get(),
                    emails.size() - successCount.get(),
                    reasons,
                    elapsedMillis,
                    quantity,
                    remainingBefore,
                    remainingAfter,
                    totalIssued
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("시뮬레이션이 중단되었습니다.");
        } finally {
            executor.shutdownNow();
        }
    }
}
