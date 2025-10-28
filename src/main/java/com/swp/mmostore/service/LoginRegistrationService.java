package com.swp.mmostore.service;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.VerificationToken;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.VerificationTokenRepository;
import com.swp.mmostore.util.AppConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class LoginRegistrationService {
    private UserRepository userRepository;
    //passwordEncoder bean here must depend on the securityConfig bean -> which also depends on loginRegistrationService bean --> circular dependency
    //--> must use @Lazy annotation
    private PasswordEncoder passwordEncoder;

    private VerificationTokenRepository verificationTokenRepository;

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


    private static final Random RANDOM = new Random();
    //Tạo token và gửi email
    public void generateRegisterTokenAndSendEmail(User user, String siteUrl) {
        User unverifiedUser = userRepository.findByEmail(user.getEmail());
        //set status to false
        user.setStatus(false);
        userRepository.save(user);
        String token = String.format("%05d", RANDOM.nextInt(100_000));
        VerificationToken verificationToken = new VerificationToken(token, unverifiedUser);
        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(unverifiedUser, token, siteUrl);
    }

    public void resendVerificationEmail(User unverifiedUser, String siteUrl) {
        VerificationToken verificationToken = verificationTokenRepository.findByUser(unverifiedUser);
        if(verificationToken != null){
            verificationTokenRepository.delete(verificationToken);
        }
        String token = String.format("%05d", RANDOM.nextInt(100_000));
        VerificationToken newVerificationToken = new VerificationToken(token, unverifiedUser); //tao moi token
        verificationTokenRepository.save(newVerificationToken);
        emailService.sendVerificationEmail(unverifiedUser, token, siteUrl);
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

    public boolean generateResetTokenAndSendEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;

        // Tạo token ngẫu nhiên 5 số
        String token = String.format("%05d", RANDOM.nextInt(100_000));


        // Gửi email
        String subject = "Mã xác thực đặt lại mật khẩu";
        String content = "Mã xác thực của bạn là: " + token;
        try {
            emailService.sendEmail(email, subject, content);
            user.setResetToken(token);
            userRepository.save(user);
            return true;
        } catch (MailException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xác thực token
    public boolean verifyResetToken(String email, String token) {
        User user = userRepository.findByEmail(email);
        if (user == null) return false;

        return token.equals(user.getResetToken());
    }

    // Cập nhật mật khẩu
    public boolean resetPassword(String email, String token, String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("Không tìm thấy user: " + email);
            return false;
        }

        System.out.println("Token trong DB: " + user.getResetToken());
        System.out.println("Token người nhập: " + token);

        if (user.getResetToken() == null) return false;
        if (!token.equals(user.getResetToken())) return false;

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        userRepository.save(user);
        return true;
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



}
