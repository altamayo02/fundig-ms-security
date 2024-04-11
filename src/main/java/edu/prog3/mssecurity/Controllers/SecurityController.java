package edu.prog3.mssecurity.Controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Models.Session;
import edu.prog3.mssecurity.Repositories.SessionRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.EncryptionService;
import edu.prog3.mssecurity.Services.HttpService;
import edu.prog3.mssecurity.Services.JwtService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Random;

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
    @Autowired
    private SessionRepository theSessionRepository;

	
    @PostMapping("login")
    public String login(@RequestBody User theUser, final HttpServletResponse response) throws IOException, URISyntaxException {
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
        String message="";

        if (
            theCurrentUser != null &&
            theCurrentUser.getPassword().equals(this.theEncryptionService.convertSHA256(theUser.getPassword()))
        ) {
			// TODO - Instance Session (If user exists)
            int code = new Random().nextInt(900000) + 100000;
            Session session = new Session(code, theCurrentUser);
            this.theSessionRepository.save(session);

            String urlNotification = "http://127.0.0.1:5000/send_email";
            String body="{\"to\": \""+theUser.getEmail()+"\", \"template\": \"TWOFACTOR\", \"pin\": "+code+", \"subject\": \"Init code\"}";
            System.out.println(theCurrentUser.getName()+"---------------------------------------------------------------");
            new HttpService(urlNotification, body).consumePostService();

            message = session.get_id();
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			// TODO - Instance ghost Session (If user exists)
        }
        return message;
    }

    @PostMapping("2FA")
    public String twoFactorAuth(@RequestBody Session session, final HttpServletResponse response)throws IOException{
        Session theCurrentSession = this.theSessionRepository.findBy_id(new ObjectId(session.get_id()));
        String token = "";

        if(theCurrentSession.getCode()==session.getCode() &&
        !theCurrentSession.getExpirationDateTime().isBefore(LocalDateTime.now())){

            System.out.println("jsjsjsjs--------------------------------------------------------1");
            User theCurrentUser = this.theUserRepository.getUserByEmail(theCurrentSession.getUser().getEmail());
            System.out.println("2---------------------------------------------------------------");
            token = this.theJwtService.generateToken(theCurrentUser);

        }else{
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return token;
    }
    
}
