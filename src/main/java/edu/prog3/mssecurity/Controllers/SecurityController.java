package edu.prog3.mssecurity.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.EncryptionService;
import edu.prog3.mssecurity.Services.JwtService;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;

@CrossOrigin
@RestController
@RequestMapping("/api/public/security")
public class SecurityController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private EncryptionService theEncryptionService;
    @Autowired
    private JwtService theJwtService;

	
    @PostMapping("login")
    public String login(@RequestBody User theUser, final HttpServletResponse response) throws IOException {
        String token = "";
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());

        if (
            theCurrentUser != null &&
            theCurrentUser.getPassword().equals(this.theEncryptionService.convertSHA256(theUser.getPassword()))
        ) {
            token = this.theJwtService.generateToken(theCurrentUser);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return token;
    }
}
