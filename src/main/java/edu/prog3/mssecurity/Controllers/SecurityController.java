package edu.prog3.mssecurity.Controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONException;
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
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private HttpService theHttpService;
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
    
                JSONObject body = new JSONObject();
                body.put("to", theUser.getEmail());
                body.put("template", "TWOFACTOR");
                body.put("pin", code);
                body.put("subject", "holi");

                String answer= this.theHttpService.postNotification("/send_email", body);
                System.out.println(answer);
                
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
            String code = this.theSecurityService.getRandomAlphanumerical(6);

            Session theSession = new Session(code, theCurrentUser);
            message += " el id de la sessión es"+ this.theSessionRepository.save(theSession).get_id();

            JSONObject body = new JSONObject();
            body.put("to", theUser.getEmail());
            body.put("template", "TWOFACTOR");
            body.put("pin", code);
            body.put("subject", "nonad");

            String answer= this.theHttpService.postNotification ("/send_email", body);
            System.out.println(answer);

        }

        return message;
    }


    @PostMapping("validator")
    public String validator(
        @RequestBody Map<String, Object> body,
        final HttpServletResponse response
    ) throws IOException {

    String message = "Rare type";
    System.out.println(body.toString()+ "ssssssssssssssssss");
    try {
        if (!body.containsKey("type")) {
            return "Invalid request: missing 'type' field";
        }

        String type = (String)body.get("type");

        switch (type) {
            case "two-factor":
                message = this.twoFactorAuth(body, response);
                break;
            case "reset-password":
                message = this.resetPassword(body, response);
                break;
            default:
                return "Invalid request: unknown type '" + type + "'";
        }
    } catch (JSONException e) {
        return "Invalid request: " + e.getMessage();
    }

    return message;
}


    public String twoFactorAuth( Map<String, Object> body, final HttpServletResponse response) throws IOException{

        Session theCurrentSession = this.theSessionRepository.findBy_id(
			new ObjectId((String)body.get("id"))
		);
        String token = "";

        if(theCurrentSession != null) {
            if (
                theCurrentSession.getCode().equals((String)body.get("code")) &&
                theCurrentSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theCurrentSession.isUse()
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theCurrentSession.getUser().getEmail()
                );

                token = this.theJwtService.generateToken(theCurrentUser);
                theCurrentSession.setUse(true);
                this.theSessionRepository.save(theCurrentSession);
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
                System.out.println(theCurrentSession.toString());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return token;
    }

    public String resetPassword(Map<String, Object> body, HttpServletResponse response) throws IOException{
        Session theCurrentSession = this.theSessionRepository.findBy_id(
			new ObjectId((String)body.get("id"))
		);

        String message = "";
        System.out.println(theCurrentSession);

        if(theCurrentSession != null) {
            if (
                theCurrentSession.getCode().equals((String)body.get("code")) &&
                theCurrentSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theCurrentSession.isUse()
            ) {

                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theCurrentSession.getUser().getEmail()
                );

                theCurrentUser.setPassword(theSecurityService.convertSHA256((String)body.get("password")));
                this.theUserRepository.save(theCurrentUser);

                theCurrentSession.setUse(true);
                this.theSessionRepository.save(theCurrentSession);
                message = "the password was update";
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

        return message;
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

	@PostMapping("validate-permissions")
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