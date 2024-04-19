package edu.prog3.mssecurity.Controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.json.JSONObject;

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
import java.net.URISyntaxException;
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
                Session session = new Session(""+code, theCurrentUser);
                this.theSessionRepository.save(session);
    
                String urlNotification="http://127.0.0.1:5000/send_email";
                String body = (
                    "{\"to\": \"" + theUser.getEmail() +
                    "\", \"template\": \"TWOFACTOR\", \"pin\": " + code + ", \"subject\": \"noneImportant\"}"
                );
                // FIXME - Catch properly
                try {
                    new HttpService(urlNotification, body).consumePostService();
                } catch(Exception e) {}
    
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

    @PostMapping("restablecer")
    public String restablecer(@RequestBody User theUser, final HttpServletResponse response) throws IOException, URISyntaxException {
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
        String message="";

        if (theCurrentUser != null) {
            String resetCode = generarRandom(6);    

            Session session = new Session(resetCode, theCurrentUser);
            this.theSessionRepository.save(session);

            JSONObject body = new JSONObject();
            body.put("to", theUser.getEmail());
            body.put("template", "RESTORE");
            body.put("pin", resetCode);
            body.put("subject", "Restablecer contraseña");


            String urlNotification = "http://127.0.0.1:5000/send_email";
            HttpService httpService  = new HttpService(urlNotification, body.toString());

            try{
                httpService.consumePostService();
                message = "Se ha enviado un correo con el código de restablecimiento.";
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "no se puede enviar el correo de restablcimiento de contraseña");
                e.printStackTrace();
                return null;
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "El usuario no existe");
            return null;
        }
        return message; 
    }

    @PostMapping("2FA")
    public String twoFactorAuth(@RequestBody Session session, final HttpServletResponse response)throws IOException{
        Session theCurrentSession = this.theSessionRepository.findBy_id(new ObjectId(session.get_id()));
        String token = "";

        if(theCurrentSession != null) {
            if (
                theCurrentSession.getCode().equals(session.getCode()) &&
                theCurrentSession.getExpirationDateTime().isAfter(LocalDateTime.now())
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theCurrentSession.getUser().getEmail()
                );
                token = this.theJwtService.generateToken(theCurrentUser);
                theCurrentSession.setUse(true);
                theCurrentSession.setToken(token);
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
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
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
        boolean success = this.theValidatorsService.validateRolePermission(
            request,
            thePermission.getUrl(),
            thePermission.getMethod()
        );
        return success;
    }


    //crear token aleatorio
    private String generarRandom (int length) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder cadena = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (caracteres.length() * Math.random());
            cadena.append(caracteres.charAt(index));
        }
        return cadena.toString();
    }
}