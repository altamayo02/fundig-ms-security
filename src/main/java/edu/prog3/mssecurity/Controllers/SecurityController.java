package edu.prog3.mssecurity.Controllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private HttpService theHttpService;
    @Autowired
    private ValidatorsService theValidatorsService;
    @Autowired
    private SessionRepository theSessionRepository;
    @Autowired
    private ErrorStatisticRepository theErrorStatisticRepository;

	
    @PostMapping("login")
    public ResponseEntity<String> login(
        @RequestBody User theUser,
        final HttpServletResponse servletResponse
    ) throws IOException {
        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());
        ResponseEntity<String> securityResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        if (theCurrentUser != null) {
            if (theCurrentUser.getPassword().equals(
                this.theSecurityService.convertSHA256(theUser.getPassword())
            )) {
                int code = new Random().nextInt(1000000);
                Session theSession = new Session(String.valueOf(code), theCurrentUser);
                this.theSessionRepository.save(theSession);
    
                JSONObject body = new JSONObject();
                body.put("to", theUser.getEmail());
                body.put("template", "TWOFACTOR");
                body.put("pin", code);

                ResponseEntity<String> notificationResponse = this.theHttpService.postNotification("/send_email", body);
                JSONObject json = new JSONObject(
					notificationResponse.getBody()).put("session_id", theSession.get_id()
				);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
                securityResponse = new ResponseEntity<String>(
					json.toString(),
					headers,
					notificationResponse.getStatusCode()
				);
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

                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        return securityResponse;
    }

    @PostMapping("2FA")
    public ResponseEntity<String> twoFactorAuth(
		@RequestBody Session theIncomingSession,
		final HttpServletResponse servletResponse
	) throws IOException {
        Session theActiveSession = this.theSessionRepository.findBy_id(
			new ObjectId(theIncomingSession.get_id())
		);
        String token = "";

        if(theActiveSession != null) {
            if (
                theActiveSession.getCode().equals(theIncomingSession.getCode()) &&
                theActiveSession.getExpirationDateTime().isAfter(LocalDateTime.now()) &&
                !theActiveSession.isUsed()
            ) {
                User theCurrentUser = this.theUserRepository.getUserByEmail(
                    theActiveSession.getUser().getEmail()
                );

                token = this.theJwtService.generateToken(theCurrentUser);
                theActiveSession.setUsed(true);
                this.theSessionRepository.save(theActiveSession);
            } else {
                ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                    	.getErrorStatisticByUser(theActiveSession.getUser().get_id());
                
                if (theErrorStatistic != null) {
                    theErrorStatistic.setNumAuthErrors(
                        theErrorStatistic.getNumAuthErrors() + 1
                    );
                } else {
					theErrorStatistic = new ErrorStatistic(0, 1, theActiveSession.getUser());
				}
                this.theErrorStatisticRepository.save(theErrorStatistic);

                servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> response = new ResponseEntity<>(
			new JSONObject().put("token", token).toString(), headers, HttpStatus.OK
		);
        return response;
    }

    @PostMapping("pw-reset")
    public ResponseEntity<String> passwordReset(
		@RequestBody User theUser,
		final HttpServletResponse response
	) throws IOException, URISyntaxException {
        String message = "Si el correo ingresado está asociado a una cuenta, " +
		"pronto recibirá un mensaje para restablecer su contraseña.";
		ResponseEntity<String> securityResponse = new ResponseEntity<>(HttpStatus.BAD_REQUEST);;

        User theCurrentUser = this.theUserRepository.getUserByEmail(theUser.getEmail());

        if (theCurrentUser != null) {
            String code = this.theSecurityService.getRandomAlphanumerical(6);

            Session theSession = new Session(code, theCurrentUser);
            this.theSessionRepository.save(theSession);

            JSONObject body = new JSONObject();
            body.put("to", theUser.getEmail());
            body.put("template", "PWRESET");
            body.put("url", code);
			
            ResponseEntity<String> notificationResponse = this.theHttpService.postNotification("/send_email", body);
			JSONObject json = new JSONObject(notificationResponse.getBody())
					.put("session_id", theSession.get_id())
					.put("message", message);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			securityResponse = new ResponseEntity<String>(
				json.toString(),
				headers,
				notificationResponse.getStatusCode()
			);
        }

        return securityResponse;
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
    public ResponseEntity<String> validatePermissions(
        final HttpServletRequest request,
        @RequestBody Permission thePermission
    ) {
        boolean success = this.theValidatorsService.validateRolePermission(
            request,
            thePermission.getUrl(),
            thePermission.getMethod()
        );

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> response = new ResponseEntity<>(
			new JSONObject().put("success", success).toString(), headers, HttpStatus.OK
		);
        return response;
    }
}