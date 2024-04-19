package edu.prog3.mssecurity.Services;

import edu.prog3.mssecurity.Models.ErrorStatistic;
import edu.prog3.mssecurity.Models.Permission;
import edu.prog3.mssecurity.Models.Role;
import edu.prog3.mssecurity.Models.RolePermission;
import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Repositories.ErrorStatisticRepository;
import edu.prog3.mssecurity.Repositories.PermissionRepository;
import edu.prog3.mssecurity.Repositories.RolePermissionRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidatorsService {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RolePermissionRepository theRolePermissionRepository;
    @Autowired
    private ErrorStatisticRepository theErrorStatisticRepository;
    private static final String BEARER_PREFIX = "Bearer ";


    public boolean validateRolePermission(
		HttpServletRequest request,
		String url,
		String method
	) {
        boolean success = false;
        User theUser = this.getUser(request);
        if (theUser != null) {
            Role theRole = theUser.getRole();
			
            //System.out.println("URL antes de la expreg: " + url + " - método " + method);
            url = url.replaceAll("[0-9a-fA-F]{24}|\\d+", "?");
            //System.out.println("URL después de la expreg: " + url + " - método " + method);

            Permission thePermission = this.thePermissionRepository.getPermission(url,method);
            if(theRole != null && thePermission != null) {
                //System.out.println("Rol " + theRole.getName() + " Permission " + thePermission.getUrl());
                RolePermission theRolePermission = this.theRolePermissionRepository.getRolePermission(
					theRole.get_id(),
					thePermission.get_id()
				);
                if (theRolePermission != null) {
					success = true;
				} else {
                    ErrorStatistic theErrorStatistic = theErrorStatisticRepository
                        	.getErrorStatisticByUser(theUser.get_id());
                    
                    if (theErrorStatistic != null) {
                        theErrorStatistic.setNumValidationErrors(
                            theErrorStatistic.getNumValidationErrors() + 1
                        );
                    } else {
						theErrorStatistic = new ErrorStatistic(1, 0, theUser);
					}
                    this.theErrorStatisticRepository.save(theErrorStatistic);
                }
            } else {
				success = false;
			}
        }
        return success;
    }
	
    public User getUser(final HttpServletRequest request) {
        User theUser = null;
        String authorizationHeader = request.getHeader("Authorization");
        //System.out.println("Header " + authorizationHeader);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            //System.out.println("Bearer Token: " + token);

            User theUserFromToken = jwtService.getUserFromToken(token);
            if(theUserFromToken != null) {
                theUser = this.theUserRepository
					.findById(theUserFromToken.get_id())
					.orElse(null);
				// Keep the password hidden
                theUser.setPassword("");
            }
        }
        return theUser;
    }
}
