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
import edu.prog3.mssecurity.Services.SecurityService;
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
    private SecurityService theSecurityService;
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
                this.theSecurityService.convertSHA256(theUser.getPassword())
            )) {
                int code = new Random().nextInt(1000000);
                Session session = new Session(String.valueOf(code), theCurrentUser);
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
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    	.getErrorStatisticByUser(theCurrentUser.get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theCurrentUser);
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return message;
    }

    @PostMapping("pw-reset")
    public String passwordReset(
		@RequestBody User theUser,
		final HttpServletResponse response
	) throws IOException, URISyntaxException {
        String message = "Si el correo ingresado está asociado a una cuenta, " +
			"pronto recibirá un mensaje para restablecer su contraseña.";
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());

        if (theCurrentUser != null) {
            String resetCode = theSecurityService.getRandomAlphanumerical(6);

            Session theSession = new Session(resetCode, theCurrentUser);
            this.theSessionRepository.save(theSession);

            JSONObject json = new JSONObject();
            json.put("to", theUser.getEmail());
            json.put("template", "PWRESET");
            json.put("pin", resetCode);
            json.put("subject", "Restablecer contraseña");

            String urlNotification = "http://127.0.0.1:5000/send_email";
            HttpService httpService  = new HttpService(urlNotification, json.toString());

            try {
                httpService.consumePostService();
            } catch (Exception e) {
                response.sendError(
					HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
					"No se pudo enviar el correo de restablecimiento de contraseña. " +
					"Intente de nuevo más tarde."
				);
                e.printStackTrace();
            }
        }

        return message;
    }

    @PostMapping("2FA")
    public String twoFactorAuth(
		@RequestBody Session theIncomingSession,
		final HttpServletResponse response
	) throws IOException {
        Session theCurrentSession = this.theSessionRepository.findBy_id(
			new ObjectId(theIncomingSession.get_id())
		);
        String token = "";

        if (theCurrentSession != null) {
            if (
                theCurrentSession.getCode().equals(theIncomingSession.getCode()) &&
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
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theCurrentSession.getUser());
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return token;
    }

    public ErrorStatistic getHighestSecurityErrors() {
        List<ErrorStatistic> theErrorStatistics = this.theErrorStatisticRepository.findAll();
        ErrorStatistic highest = new ErrorStatistic();
        for (ErrorStatistic es : theErrorStatistics) {
            if (es.getNumSecurityErrors() > highest.getNumSecurityErrors()) {
                highest = es;
            }
        }
        return highest;
    }

    public boolean validatePermissions(
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
}