package cl.duoc.wim100.config;

import cl.duoc.wim100.dto.LoginResponse;
import cl.duoc.wim100.model.Rol;
import jakarta.servlet.http.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.*;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class AccesoApi implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                HttpSession session = request.getSession(false);
                if (session == null || !(session.getAttribute("cuenta") instanceof LoginResponse cuenta)) {
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión para continuar");
                }
                if (!request.getMethod().equals("GET") && cuenta.rol() != Rol.EVALUADORA
                        && !request.getRequestURI().equals(request.getContextPath() + "/api/auth/logout")) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el rol Evaluadora");
                }
                return true;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/auth/login");
    }
}
