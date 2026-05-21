package com.newgen.bookingsystem.dao;

import com.newgen.bookingsystem.entity.User;
import com.newgen.bookingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class UserDataAccess {
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Integer id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    
    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }
    
    public boolean userExists(Integer id) {
        return userRepository.existsById(id);
    }
    
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
    
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }
    
    public long getUserCount() {
        return userRepository.count();
    }
}
