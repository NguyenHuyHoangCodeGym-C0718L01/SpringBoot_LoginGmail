package com.example.springbootlogingmail.service;

import com.example.springbootlogingmail.model.PasswordResetToken;
import com.example.springbootlogingmail.model.User;
import com.example.springbootlogingmail.repostiory.PasswordTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Optional;

@Service
@Transactional
public class SecurityService {

    @Autowired
    private PasswordTokenRepository passwordTokenRepository;

    public String validatePasswordResetToken(long id, String token) {
        Optional<PasswordResetToken> passTokenOptional =
                passwordTokenRepository.findByToken(token);
        if(!passTokenOptional.isPresent() || passTokenOptional.get().getUser().getId()!=id) {
            return "invalidToken";
        }

        PasswordResetToken passToken = passTokenOptional.get();

        Calendar cal = Calendar.getInstance();
        if ((passToken.getExpiryDate()
                .getTime() - cal.getTime()
                .getTime()) <= 0) {
            return "expired";
        }

        User user = passToken.getUser();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, Arrays.asList(
                new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        return null;
    }
}
