package com.hwije.coupon_system.dto;

import java.util.Map;

public class SimulationResponse {

    private Long couponId;
    private int requestCount;
    private int threadCount;
    private int successCount;
    private int failCount;
    private Map<String, Integer> failReasons;
    private long elapsedMillis;
    private double requestsPerSecond;
    private int couponQuantity;
    private int remainingBefore;
    private int remainingAfter;
    private long totalIssuedInDb;
    private boolean consistent;

    public SimulationResponse(Long couponId, int requestCount, int threadCount,
                              int successCount, int failCount, Map<String, Integer> failReasons,
                              long elapsedMillis, int couponQuantity,
                              int remainingBefore, int remainingAfter, long totalIssuedInDb) {
        this.couponId = couponId;
        this.requestCount = requestCount;
        this.threadCount = threadCount;
        this.successCount = successCount;
        this.failCount = failCount;
        this.failReasons = failReasons;
        this.elapsedMillis = elapsedMillis;
        this.requestsPerSecond = elapsedMillis == 0
                ? requestCount
                : Math.round(requestCount * 1000.0 / elapsedMillis * 10) / 10.0;
        this.couponQuantity = couponQuantity;
        this.remainingBefore = remainingBefore;
        this.remainingAfter = remainingAfter;
        this.totalIssuedInDb = totalIssuedInDb;
        // 초과 발급이 없고, 줄어든 재고와 성공 건수, DB 발급 건수가 모두 맞아야 정상
        this.consistent = remainingAfter >= 0
                && remainingBefore - remainingAfter == successCount
                && couponQuantity - remainingAfter == totalIssuedInDb;
    }

    public Long getCouponId() {
        return couponId;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public Map<String, Integer> getFailReasons() {
        return failReasons;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getCouponQuantity() {
        return couponQuantity;
    }

    public int getRemainingBefore() {
        return remainingBefore;
    }

    public int getRemainingAfter() {
        return remainingAfter;
    }

    public long getTotalIssuedInDb() {
        return totalIssuedInDb;
    }

    public boolean isConsistent() {
        return consistent;
    }
}
