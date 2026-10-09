package com.hwije.coupon_system.coupon;

import com.hwije.coupon_system.dto.CreateCouponRequest;
import com.hwije.coupon_system.dto.MyCouponResponse;
import com.hwije.coupon_system.entity.Coupon;
import com.hwije.coupon_system.entity.CouponIssue;
import com.hwije.coupon_system.entity.Member;
import com.hwije.coupon_system.repository.CouponIssueRepository;
import com.hwije.coupon_system.repository.CouponRepository;
import com.hwije.coupon_system.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hwije.coupon_system.dto.CouponResponse;
import java.time.LocalDateTime;
import java.util.List;
@Service
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final MemberRepository memberRepository;

    public CouponService(CouponRepository couponRepository,
                         CouponIssueRepository couponIssueRepository,
                         MemberRepository memberRepository) {
        this.couponRepository = couponRepository;
        this.couponIssueRepository = couponIssueRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Coupon createCoupon(CreateCouponRequest request) {

        Coupon coupon = new Coupon();

        coupon.setName(request.getName());

        coupon.setQuantity(request.getQuantity());

        coupon.setRemainingQuantity(request.getQuantity());

        coupon.setCreatedAt(LocalDateTime.now());

        coupon.setStartAt(LocalDateTime.now());

        coupon.setEndAt(LocalDateTime.now().plusDays(30));

        return couponRepository.save(coupon);
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> getCoupons() {

        return couponRepository.findAll()
                .stream()
                .map(coupon -> new CouponResponse(
                        coupon.getId(),
                        coupon.getName(),
                        coupon.getRemainingQuantity()
                ))
                .toList();
    }

    @Transactional
    public CouponIssue issueCoupon(Long couponId, String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 쿠폰 행을 잠근 뒤에 중복 여부를 확인해야 같은 회원의 동시 요청도 막을 수 있다
        Coupon coupon = couponRepository.findByIdForUpdate(couponId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쿠폰입니다."));

        if (couponIssueRepository.existsByMemberIdAndCouponId(member.getId(), couponId)) {
            throw new IllegalStateException("이미 발급받은 쿠폰입니다.");
        }

        LocalDateTime now = LocalDateTime.now();

        coupon.issue(now);

        CouponIssue couponIssue = new CouponIssue();
        couponIssue.setMember(member);
        couponIssue.setCoupon(coupon);
        couponIssue.setIssuedAt(now);

        return couponIssueRepository.save(couponIssue);
    }

    @Transactional(readOnly = true)
    public List<MyCouponResponse> getMyCoupons(String email) {

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return couponIssueRepository.findAllByMemberIdWithCoupon(member.getId())
                .stream()
                .map(issue -> new MyCouponResponse(
                        issue.getId(),
                        issue.getCoupon().getId(),
                        issue.getCoupon().getName(),
                        issue.getIssuedAt(),
                        issue.getCoupon().getEndAt()
                ))
                .toList();
    }
}
