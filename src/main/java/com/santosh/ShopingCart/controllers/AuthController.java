package com.santosh.ShopingCart.controllers;

import com.santosh.ShopingCart.dto.ChangePasswordDto;
import com.santosh.ShopingCart.dto.LoginDto;
import com.santosh.ShopingCart.dto.ResetPasswordDto;
import com.santosh.ShopingCart.entites.Users;
import com.santosh.ShopingCart.repos.UserRepo;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class AuthController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JavaMailSender javaMailSender;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDto loginDto) throws NotFoundException {
        String email = loginDto.getEmail(); // pavan@yopmail.com
        String password = loginDto.getPassword(); // pavan

        Users usersFromDB = userRepo.findByEmail(email);
        if(usersFromDB == null){
            return new ResponseEntity<>("Invalid email ("+loginDto.getEmail()+")",HttpStatus.NOT_FOUND);
        }

        String hashedPassword = usersFromDB.getPassword(); // asdfasdfasdfasd$asdfasdf
        if(!encoder.matches(password,hashedPassword)){
            return new ResponseEntity<>("Password is incorrect ("+loginDto.getEmail()+")",HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(usersFromDB,HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid Users users) throws MessagingException {

        Users savedUsers = userRepo.save(users);

        // generate OTP
        String otp = generateOtp(6);
        Date createdOtpTime = new Date();
        Date expiryOtpTime = new Date(createdOtpTime.getTime() + TimeUnit.HOURS.toMillis(1)); // Adds 1 hours

        savedUsers.setOtp(otp.toString());
        savedUsers.setCreatedOtpTime(createdOtpTime);
        savedUsers.setExpiryOtpTime(expiryOtpTime);

        userRepo.save(savedUsers);

        sendSignupMail(savedUsers.getEmail(),"http://localhost:3000/resetPassword?otp="+otp);
        return new ResponseEntity<>(savedUsers,HttpStatus.OK);
    }

    public String generateOtp(int length){
        Random random = new Random();
        char[] otp = new char[length];
        for (int i=0; i<length; i++)
        {
            otp[i]= (char)(random.nextInt(10)+48);
        }

        return otp.toString();
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto changePasswordDto){

        Users userFromDb = userRepo.findByEmail(changePasswordDto.getEmail());
        String hashedPassword = userFromDb.getPassword();
        String oldPass = changePasswordDto.getOldPassword();
        String newPass = changePasswordDto.getNewPassword();

        if(!encoder.matches(oldPass,hashedPassword)){
            return new ResponseEntity<>("password doesn't match, please try again",HttpStatus.BAD_REQUEST);
        }

        userFromDb.setPassword(newPass);
        userRepo.save(userFromDb);
        return new ResponseEntity<>("Password updated successfully",HttpStatus.OK);
    }


    @PostMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordDto resetPasswordDto){

            // 1. check user Generated OTP from DB with requested OTP from post request
            Users userFromDb = userRepo.findByEmail(resetPasswordDto.getEmail());

            if(userFromDb == null){
                return new ResponseEntity<>("Invalid Email",HttpStatus.BAD_REQUEST);
            }

            if(userFromDb.getOtp() == null){
                return new ResponseEntity<>("OTP not generated yet please generated OTP",HttpStatus.BAD_REQUEST);
            }

            if(!userFromDb.getOtp().equals(resetPasswordDto.getOtp())){
                return new ResponseEntity<>("Invalid OTP",HttpStatus.BAD_REQUEST);
            }

            // 2. validate OTP expiry : check whether expiry time is greater then current time
            Date otpExpiryTime = userFromDb.getExpiryOtpTime(); // UTC
            if(otpExpiryTime.before(new Date())){
                return new ResponseEntity<>("OTP got expired please generate OTP",HttpStatus.BAD_REQUEST);
            }

            // 3. If no, Update the password by ecrypting with bycrpt
            String hashedPassword = encoder.encode(resetPasswordDto.getPassword());
            userFromDb.setPassword(hashedPassword);
            userFromDb.setOtp(null);
            userFromDb.setCreatedOtpTime(null);
            userFromDb.setExpiryOtpTime(null);

            userRepo.save(userFromDb);

            return new ResponseEntity<>("Password updated successfully",HttpStatus.OK);
    }

    @GetMapping("/generateOtp")
    public ResponseEntity<?> generatePassword(@RequestParam @NotNull String email) throws MessagingException {


        Users userFromDb  = userRepo.findByEmail(email);

        if(userFromDb == null){
            return new ResponseEntity<>("Invalid email", HttpStatus.BAD_REQUEST);
        }

        String otp = generateOtp(6);
        Date createdOtpTime = new Date();
        Date expiryOtpTime = new Date(createdOtpTime.getTime() + TimeUnit.HOURS.toMillis(1)); // Adds 1 hours

        userFromDb.setOtp(otp);
        userFromDb.setCreatedOtpTime(createdOtpTime);
        userFromDb.setExpiryOtpTime(expiryOtpTime);

        userRepo.save(userFromDb);

        sendSignupMail(userFromDb.getEmail(),"test.com");

        return new ResponseEntity<>("Otp generated successfully!..", HttpStatus.OK);
    }


    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(12);
    }

    public void sendSignupMail(String toMail,String link) throws MessagingException {
        MimeMessage msg = javaMailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(msg, true);
        helper.setTo(toMail);
        helper.setSubject("Email Verification");
        helper.setText("<h1> Please click below link to verify your email !</h1><br/> <a href="+link+">"+link+"</a> ", true);

        javaMailSender.send(msg);
    }

}
