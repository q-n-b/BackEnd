package qnb.common.JWT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import qnb.common.config.GptUserProperties;
import qnb.user.security.UserDetailsImpl;
import qnb.user.service.CustomUserDetailsService;

import java.io.IOException;

/**
 * 요청의 JWT를 검증하고, 유효하면 SecurityContext에 인증 정보를 세팅한다.
 * - GPT 시스템 계정 토큰은 인증을 세팅하지 않고 통과(차단)한다.
 * - 예외는 삼켜서 401/403 처리는 이후 체인/엔드포인트에서 하도록 한다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final GptUserProperties gptUserProperties;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService userDetailsService,
                                   GptUserProperties gptUserProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.gptUserProperties = gptUserProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 이미 인증이 세팅되어 있으면 건너뛰기 (필요시)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String token = getJwtFromRequest(request);

                if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                    Long userId = jwtTokenProvider.getUserIdFromToken(token);

                    // GPT 시스템 계정 토큰 차단(2차 방어선)
                    Long gptUserId = gptUserProperties.getUserId();
                    if (gptUserId != null && gptUserId.equals(userId)) {
                        // 인증을 세팅하지 않고 체인 진행 → 보호된 자원 접근 시 401/403 유도
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // 정상 사용자면 UserDetails 로드 후 컨텍스트 세팅
                    UserDetailsImpl userDetails = userDetailsService.loadUserById(userId);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            // 토큰 파싱/유효성/DB 조회 등의 오류가 나도 필터는 계속 진행
            // (엔드포인트에서 인증 필요 시 자연스럽게 401/403으로 처리됨)
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
