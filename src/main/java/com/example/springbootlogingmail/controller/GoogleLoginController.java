package com.example.springbootlogingmail.controller;

import com.example.springbootlogingmail.model.GenericResponse;
import com.example.springbootlogingmail.model.GooglePojo;
import com.example.springbootlogingmail.model.User;
import com.example.springbootlogingmail.service.SecurityService;
import com.example.springbootlogingmail.service.UserService;
import com.example.springbootlogingmail.util.GoogleUtils;
import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Controller
public class GoogleLoginController {

    @Autowired
    private GoogleUtils googleUtils;

    @Autowired
    private SecurityService securityService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @RequestMapping("/login-gmail")
    public String loginGoogle(HttpServletRequest request, Model model) throws ClientProtocolException, IOException {
        String code = request.getParameter("code");

        if (code == null || code.isEmpty()) {
            return "redirect:/login?google=error";
        }
        String accessToken = googleUtils.getToken(code);

        GooglePojo googlePojo = googleUtils.getUserInfo(accessToken);
        UserDetails userDetail = googleUtils.buildUser(googlePojo);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null,
                userDetail.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<User> optionalUser = userService.findUserByEmail(googlePojo.getEmail());
        if(optionalUser.isPresent()){
            model.addAttribute("user", optionalUser.get());
        }else {
            User user = new User();
            user.setAccount(googlePojo.getName());
            user.setEmail(googlePojo.getEmail());
            String passEncoded = passwordEncoder.encode("12345678");
            user.setPassword(passEncoded);
            user.setRole("user");
            userService.save(user);
            model.addAttribute("user", user);
        }

        return "user";
    }

    @GetMapping("/registration")
    public String getRegistrationPage(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String createAnAccount(@Valid @ModelAttribute User user, Model model) {
        String passEncoded = passwordEncoder.encode(user.getPassword());
        user.setPassword(passEncoded);
        userService.save(user);
        model.addAttribute("user", new User());
        model.addAttribute("message", "New user have just created successfully");
        return "registration";
    }

    @GetMapping("/403")
    public String getAccessDeniedPage() {
        return "403";
    }

    @GetMapping("/user/{id}")
    public String getUserInfo(@PathVariable int id, Model model) {
        Optional<User> user = userService.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "view";
        } else {
            return "404";
        }
    }

    @GetMapping("/list")
    public String getListUser(Model model) {
        Iterable<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "list";
    }

    @GetMapping("/forgetpassword")
    public String getForgetPasswordPage(){
        return "forgetpassword";
    }

    @PostMapping("/forgetpassword")
    public String processForgetPassword(@RequestParam("mail") String userEmail) {
        Optional<User> userOptional = userService.findUserByEmail(userEmail);
        if(userOptional.isPresent()) {
            String token = UUID.randomUUID().toString();
            User user = userOptional.get();
            user.setCode(token);
            userService.save(user);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject("Code to reset your password");
            message.setText(token);
            mailSender.send(message);
            return "passwordcode";
        }
        return "404";
    }

    @GetMapping("/passwordcode")
    public String getPasswordCodePage(){
        return "passwordcode";
    }

    @PostMapping("/passwordcode")
    public String inputCode(@RequestParam("code")String code, Model model){
        Optional<User> userOptional = userService.findUserByCode(code);
        if(userOptional.isPresent()){
            User user = userOptional.get();
            if(user.getCode().equals(code)){
                user.setCode("");
                userService.save(user);
                return "resetpassword";
            }else{
                model.addAttribute("message", "wrong code");
                return "passwordcode";
            }
        }
        return "404";
    }

//        if (!userOptional.isPresent()) {
//            throw new UserNotFoundException();
//        }
//        User user = userOptional.get();
//        String token = UUID.randomUUID().toString();
//        userService.createPasswordResetTokenForUser(user, token);
//        mailSender.send(constructResetTokenEmail(getAppUrl(request),
//                request.getLocale(), token, user));
//        return new GenericResponse(
//                messages.getMessage("message.resetPasswordEmail", null,
//                        request.getLocale()));
//    }
//
//    private SimpleMailMessage constructResetTokenEmail(
//            String contextPath, Locale locale, String token, User user) {
//        String url = contextPath + "/user/changePassword?id=" +
//                user.getId() + "&token=" + token;
//        String message = messages.getMessage("message.resetPassword",
//                null, locale);
//        return constructEmail("Reset Password", message + " \r\n" + url, user);
//    }
//
//    private SimpleMailMessage constructEmail(String subject, String body,
//                                             User user) {
//        SimpleMailMessage email = new SimpleMailMessage();
//        email.setSubject(subject);
//        email.setText(body);
//        email.setTo(user.getEmail());
//        email.setFrom(env.getProperty("support.email"));
//        return email;
//    }
//
//    @RequestMapping(value = "/changePassword", method = RequestMethod.GET)
//    public String showChangePasswordPage(Locale locale, Model model,
//                                         @RequestParam("id") long id, @RequestParam("token") String token) {
//        String result = securityService.validatePasswordResetToken(id, token);
//        if (result != null) {
//            model.addAttribute("message",
//                    messages.getMessage("auth.message." + result, null, locale));
//            return "redirect:/login?lang=" + locale.getLanguage();
//        }
//        return "redirect:/updatePassword.html";
//    }
}
