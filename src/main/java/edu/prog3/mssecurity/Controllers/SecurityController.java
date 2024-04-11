package edu.prog3.mssecurity.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Models.Permission;
import edu.prog3.mssecurity.Models.Session;
import edu.prog3.mssecurity.Models.ErrorStatistic;
import edu.prog3.mssecurity.Repositories.ErrorStatisticRepository;
import edu.prog3.mssecurity.Repositories.SessionRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.EncryptionService;
import edu.prog3.mssecurity.Services.HttpService;
import edu.prog3.mssecurity.Services.JwtService;
import edu.prog3.mssecurity.Services.ValidatorsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;
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
    private ValidatorsService theValidatorsService;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private ErrorStatisticRepository theErrorStatisticRepository;

	
    @PostMapping("login")
    public String login(
        @RequestBody User theUser,
        final HttpServletResponse response
    ) throws IOException {
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
        String message = "";

        if (theCurrentUser != null) {
            if (theCurrentUser.getPassword().equals(
                this.theEncryptionService.convertSHA256(theUser.getPassword())
            )) {
                int code = new Random().nextInt(900000) + 100000;
                Session session = new Session(code, theCurrentUser);
                this.theSessionRepository.save(session);
    
                String urlNotification="127.0.0.1:5000/send_email";
                String body = (
                    "{'to': '" + theUser.getEmail() +
                    "', 'template': 'TWOFACTOR', 'pin': " + code + "}"
                );
                new HttpService(urlNotification, body).consumePostService();
    
                message = session.get_id();
            } else {
                // TODO - Instance ghost Session (If user exists)
                
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    .getErrorStatisticByUser(theCurrentUser.get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );

                } else theErrorStatistic = new ErrorStatistic(
                    0, 1, theCurrentUser
                );
                this.theErrorStatisticRepository.save(theErrorStatistic);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
			// TODO - Instance ghost Session (If user exists)
        }
        return message;
    }

    @PostMapping("2FA")
    public String twoFactorAuth(@RequestBody Session session, final HttpServletResponse response)throws IOException{
        Session theCurrentSession = this.theSessionRepository.getById(session.get_id());
        String token = "";

        if(theCurrentSession != null) {
            if (
                theCurrentSession.getCode() == session.getCode() &&
                theCurrentSession.getExpirationDateTime().isBefore(LocalDateTime.now())
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theCurrentSession.getUser().getEmail()
                );
                token = this.theJwtService.generateToken(theCurrentUser);
            } else {
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    .getErrorStatisticByUser(theCurrentSession.getUser().get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );

                } else theErrorStatistic = new ErrorStatistic(
                    0, 1, theCurrentSession.getUser()
                );
                this.theErrorStatisticRepository.save(theErrorStatistic);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return token;
    }

    public ErrorStatistic highestSecurityErrors() {
        List<ErrorStatistic> theErrorStatistics = this.theErrorStatisticRepository.findAll();
        ErrorStatistic highest = new ErrorStatistic();
        for (ErrorStatistic es : theErrorStatistics) {
            if (es.getNumSecurityErrors() > highest.getNumSecurityErrors()) {
                highest = es;
            }
        }
        return highest;
    }

    public boolean permissionsValidation(
        final HttpServletRequest request,
        @RequestBody Permission thePermission
    ) {
        boolean success = this.theValidatorsService.validationRolePermission(
            request,
            thePermission.getUrl(),
            thePermission.getMethod()
        );
        return success;
    }
}
