package com.jeonjueats.service;

import com.jeonjueats.dto.LoginRequestDto;
import com.jeonjueats.dto.LoginResponseDto;
import com.jeonjueats.dto.SignupRequestDto;
import com.jeonjueats.dto.SignupResponseDto;
import com.jeonjueats.entity.Cart;
import com.jeonjueats.entity.User;
import com.jeonjueats.entity.UserRole;
import com.jeonjueats.repository.CartRepository;
import com.jeonjueats.repository.UserRepository;
import com.jeonjueats.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스
 * 회원가입, 로그인, 로그아웃 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 처리
     * 
     * @param requestDto 회원가입 요청 정보
     * @return 회원가입 응답 정보
     * @throws IllegalArgumentException 이메일 또는 닉네임 중복 시
     */
    @Transactional
    public SignupResponseDto signup(SignupRequestDto requestDto) {
        log.info("회원가입 요청: email={}, nickname={}, role={}", 
                requestDto.getEmail(), requestDto.getNickname(), requestDto.getRole());

        // 1. 이메일 중복 확인
        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 2. 닉네임 중복 확인
        if (userRepository.existsByNickname(requestDto.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        // 3. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 4. UserRole enum 변환
        UserRole userRole;
        try {
            userRole = UserRole.valueOf(requestDto.getRole());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 역할입니다: " + requestDto.getRole());
        }

        // 5. User 엔티티 생성 및 저장
        User user = new User(
                requestDto.getEmail(),
                encodedPassword,
                requestDto.getNickname(),
                userRole
        );

        User savedUser = userRepository.save(user);
        log.info("사용자 생성 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        // 6. 빈 장바구니 생성 (사용자마다 하나의 장바구니)
        Cart cart = new Cart(savedUser.getId());
        
        cartRepository.save(cart);
        log.info("장바구니 생성 완료: cartId={}, userId={}", cart.getId(), savedUser.getId());

        // 7. 응답 DTO 생성
        return SignupResponseDto.of(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname(),
                savedUser.getRole().name()
        );
    }

    /**
     * 사용자 로그인
     * 이메일과 비밀번호로 인증 후 JWT 토큰 발급
     *
     * @param requestDto 로그인 요청 정보 (이메일, 비밀번호)
     * @return 로그인 응답 정보 (토큰, 사용자 정보)
     * @throws RuntimeException 인증 실패 시
     */
    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto requestDto) {
        log.info("로그인 요청: email={}", requestDto.getEmail());

        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> {
                    log.warn("존재하지 않는 이메일로 로그인 시도: {}", requestDto.getEmail());
                    return new RuntimeException("AUTHENTICATION_FAILED");
                });

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            log.warn("잘못된 비밀번호로 로그인 시도: email={}", requestDto.getEmail());
            throw new RuntimeException("AUTHENTICATION_FAILED");
        }

        // 3. JWT 토큰 생성
        String accessToken = jwtUtil.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        // 4. 응답 DTO 생성 및 반환
        return LoginResponseDto.of(
                accessToken,
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
} 