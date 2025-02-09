package example.cashcard;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(request -> request
            .requestMatchers("/cashcards/**")
            .hasRole("CARD-OWNER"))
            .httpBasic(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
        User.UserBuilder users = User.builder();

        UserDetails sid = users
            .username("sid")
            .password(passwordEncoder.encode("abc123"))
            .roles("CARD-OWNER")
            .build();

        UserDetails hankOwnsNoCards = users
            .username("hankOwnsNoCards")
            .password(passwordEncoder.encode("qwerty"))
            .roles("NON-OWNER")
            .build();

        UserDetails hardik = users
            .username("hardik")
            .password(passwordEncoder.encode("xyz123"))
            .roles("CARD-OWNER")
            .build();

        return new InMemoryUserDetailsManager(sid, hankOwnsNoCards, hardik);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}