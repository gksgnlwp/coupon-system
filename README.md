# 선착순 쿠폰 발급 시스템

Spring Boot + JPA + MySQL 로 만든 선착순 쿠폰 발급 API 입니다.
JWT 로그인, 쿠폰 생성/조회/발급, 동시 요청 시 재고 초과 발급 방지를 구현했습니다.

## 기술 스택
- Java 17, Spring Boot 4, Spring Security, Spring Data JPA
- MySQL (테스트는 H2 인메모리)
- JWT (jjwt)

## 주요 기능
| API | 설명 | 인증 |
|---|---|---|
| `POST /members/signup` | 회원가입 | - |
| `POST /members/login` | 로그인 (JWT 발급) | - |
| `GET /members/me` | 내 정보 | 필요 |
| `GET /coupons` | 쿠폰 목록 | - |
| `POST /coupons` | 쿠폰 생성 | 필요 |
| `POST /coupons/{couponId}/issue` | 쿠폰 발급 | 필요 |
| `GET /coupons/me` | 내가 발급받은 쿠폰 | 필요 |

## 동시성 처리
- 발급 시 쿠폰 행에 비관적 락(`SELECT ... FOR UPDATE`)을 건 뒤 중복 여부와 재고를 확인하고 차감합니다.
- `coupon_issue (member_id, coupon_id)` 유니크 제약으로 중복 발급을 DB 레벨에서도 막습니다.
- `CouponServiceTest` 에서 100명이 수량 30개 쿠폰에 동시에 요청해 정확히 30개만 발급되는지 검증합니다.

## 실행 방법
1. MySQL 에 `coupon_system` 데이터베이스를 만듭니다.
2. `src/main/resources/application-local.properties.example` 을 `application-local.properties` 로 복사하고 DB 비밀번호와 JWT 키를 채웁니다.
3. 서버 실행
   ```bash
   ./gradlew bootRun
   ```
4. http://localhost:8080 에서 테스트 화면을 사용할 수 있습니다.

## 테스트
```bash
./gradlew test
```
