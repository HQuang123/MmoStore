package com.swp.mmostore.service;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class LoginRegistrationService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public LoginRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User saveUser(User user) {
        user.setRole("User");
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        try{
            return userRepository.save(user);
        }catch (Exception e){
            throw new RuntimeException("Failed to save user");
        }
    }

    public User getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsersByRole(String role){
        return userRepository.findByRole(role);
    }

    public Boolean updateUserStatus(Boolean status, int id){
        Optional<User> userById = userRepository.findById(id);
        if(userById.isPresent()){
            User user = userById.get();
            user.setStatus(status);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void userFailedAttemptIncrease(User user){
            user.setAccountFailedAttempt(user.getAccountFailedAttempt()+1);
            userRepository.save(user);
    }

    public void userAccountLock(User user) {
        user.setAccountStatusNonLocked(false);
        user.setAccountLockTime(new Date());
        userRepository.save(user);
    }

    public boolean isUnlockAccountTimeExpired(User user) {
       long accountLockTime = user.getAccountLockTime().getTime();
       long accountUnlockTime = accountLockTime + AppConstant.UNLOCK_DURATION_TIME;
       long currentTime = System.currentTimeMillis();

       if(accountUnlockTime < currentTime){
           user.setAccountStatusNonLocked(true);
           user.setAccountFailedAttempt(0);
           user.setAccountLockTime(null);
           userRepository.save(user);
           return true;
       }
       return false;
    }

    public void updateUserResetTokenForSendingEmail(String email, String resetToken){
        User user = userRepository.findByEmail(email);
        user.setResetToken(resetToken);
        userRepository.save(user);
    }

    public User getUserByResetToken(String resetToken){
        return userRepository.findByResetToken(resetToken);
    }

    //need to check this
    public User updateUserWhileResettingPassword(User user){
        return userRepository.save(user);
    }










}
