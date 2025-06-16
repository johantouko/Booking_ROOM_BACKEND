package com.example.BookingRoom.jwt;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;

@Component
public class JwtTokenInterceptor implements HandlerInterceptor {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            try {
//                Claims claims = Jwts.parserBuilder()
//                        .setSigningKey(secret.getBytes())
//                        .build()
//                        .parseClaimsJws(token)
//                        .getBody();
//
//                request.setAttribute("userId", claims.getSubject());
//                return true;
//            } catch (Exception e) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                return false;
//            }
//        }
//        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//        return false;
//    }
}
