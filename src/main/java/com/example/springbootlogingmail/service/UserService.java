package com.example.springbootlogingmail.service;

import com.example.springbootlogingmail.model.User;

import java.util.Optional;

public interface UserService {

    Iterable<User> findAll();

    Optional<User> findById(int id);

    void remove(int id);

    void save(User user);

    Optional<User> findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    Optional<User> findUserByCode(String code);
}
