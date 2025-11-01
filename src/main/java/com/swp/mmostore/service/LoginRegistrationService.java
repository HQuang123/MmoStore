package com.swp.mmostore.service;

import com.swp.mmostore.entity.PasswordResetToken;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.VerificationToken;
import com.swp.mmostore.repository.PasswordResetTokenRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.VerificationTokenRepository;
import com.swp.mmostore.util.AppConstant;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import static org.hibernate.annotations.UuidGenerator.Style.RANDOM;

@Service
public class LoginRegistrationService {
    private UserRepository userRepository;
    //passwordEncoder bean here must depend on the securityConfig bean -> which also depends on loginRegistrationService bean --> circular dependency
    //--> must use @Lazy annotation
    private PasswordEncoder passwordEncoder;

    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public LoginRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
    }
    @Autowired
    private EmailService emailService;

    public User saveUser(User user) {
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
    public boolean validateUser(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;

        // check password match
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatch) return false;

        // check locked status
        return user.getAccountStatusNonLocked() == null
                || Boolean.TRUE.equals(user.getAccountStatusNonLocked());
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

    public User findByProviderId(String providerId){
        return userRepository.findByProviderId(providerId);
    }


    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private String generateSecureToken() {
        // 32 bytes = 256 bits of entropy, which is very secure
        byte[] tokenBytes = new byte[32];
        SECURE_RANDOM.nextBytes(tokenBytes);
        return BASE64_ENCODER.encodeToString(tokenBytes);
    }
    //Tạo token và gửi email
    public void generateRegisterTokenAndSendEmail(User user, String siteUrl) {
        saveUser(user);
        String token = generateSecureToken();
        VerificationToken verificationToken = new VerificationToken(token, user);
        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationToken(user, token, siteUrl);
    }

    public void resendVerificationEmail(User unverifiedUser, String siteUrl) {
        //only process when user is not verified and status of user is false (inactive)
        if (unverifiedUser != null && !unverifiedUser.getStatus()) {
            VerificationToken verificationToken = verificationTokenRepository.findByUser(unverifiedUser);
            if (verificationToken != null) {
                verificationTokenRepository.delete(verificationToken);
            }
            String token = generateSecureToken();
            VerificationToken newVerificationToken = new VerificationToken(token, unverifiedUser); //tao moi token
            verificationTokenRepository.save(newVerificationToken);
            emailService.sendVerificationToken(unverifiedUser, token, siteUrl);
        }
    }


    public String verifyUserToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "invalid"; // Token not found
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenRepository.delete(verificationToken); // Clean up expired token
            return "expired"; // Token is expired
        }

        User user = verificationToken.getUser();
        user.setStatus(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken); // Token is used, delete it
        return "success";
    }

    @Transactional
    public void generateResetTokenAndSendEmail(String email, String siteUrl) {
        User user = userRepository.findByEmail(email);
        if (user == null) return ;
        //check to see if there is old token -> then delete
        PasswordResetToken oldToken = passwordResetTokenRepository.findByUser(user);
        if(oldToken !=  null){
            passwordResetTokenRepository.delete(oldToken);
            passwordResetTokenRepository.flush();
        }
        // create new one
        String token = generateSecureToken();
        PasswordResetToken newToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(newToken);
        emailService.sendResetPasswordToken(user, token, siteUrl);
    }

    // Xác thực token
    @Transactional
    public String verifyResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if(passwordResetToken == null){
            return "invalid";
        }else if(passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }
        return "valid";
    }

    // Cập nhật mật khẩu
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        if(passwordResetToken == null){
            throw new RuntimeException("Invalid token");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
    }



    public void updateUser(User user) {
        userRepository.save(user);
    }


    public void registerAsSeller(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.addRole("ROLE_SELLER");
            userRepository.save(user);
        }
    }

    public void deleteUser(Integer id) {
        userRepository.deleteById(id);
    }



    public void updatePassword(String email, String rawPassword, PasswordEncoder encoder) {
        String encoded = encoder.encode(rawPassword);
        boolean matches = passwordEncoder.matches("654321",
                "$2a$10$5IxvgR40SEUcnXHl1wN0/uUEkEZu.n2gqCS.LqGRVPiUQKXgq.ZQa");

        if (matches) {
            System.out.println("Mật khẩu đúng");
        } else {
            System.out.println("Sai mật khẩu");
        }
        userRepository.updatePasswordByEmail(encoded, email);
    }



}
