package com.example.springbootlogingmail.repostiory;

import com.example.springbootlogingmail.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    User findByAccount(String account);

    Optional<User> findUserByEmail(String email);

    @Query("select u from User u Where u.code=?1")
    Optional<User> findUserByCode(String code);


}
