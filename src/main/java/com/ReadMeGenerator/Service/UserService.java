package com.ReadMeGenerator.Service;


import com.ReadMeGenerator.Model.Users;
import com.ReadMeGenerator.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void storeUser(Model model, OAuth2AuthenticationToken authentication) {
        if(authentication != null && authentication.isAuthenticated()){
            OAuth2User user = authentication.getPrincipal();

            System.out.println("OAuth2 User Attributes: " + user.getAttributes());

            String email = user.getAttribute("email");
            String name = user.getAttribute("name");

            System.out.println("Extracted email: " + email);
            System.out.println("Extracted name: " + name);

            assert email != null;
            if(!userRepository.existsById(email)){
                Users users = new Users(name , email);
                userRepository.save(users);
            }
        }
    }
}