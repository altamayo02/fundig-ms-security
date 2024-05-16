package edu.prog3.mssecurity.Controllers;

import edu.prog3.mssecurity.Models.ErrorStatistic;
import edu.prog3.mssecurity.Models.Role;
import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Repositories.RoleRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UsersController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RoleRepository theRoleRepository;
    @Autowired
    private SecurityService theSecurityService;
    @Autowired
    private SecurityController theSecurityController;

	
    @GetMapping
    public List<User> findAll() {
        return this.theUserRepository.findAll();
    }

    @GetMapping("{id}")
    public User findById(@PathVariable String id) {
        User theUser = this.theUserRepository
                .findById(id)
                .orElse(null);
        return theUser;
    }

    @GetMapping("most-errors")
    public User findByMostErrors(@PathVariable String id) {
        ErrorStatistic theErrorStatistic = this.theSecurityController.getHighestSecurityErrors();
        return theErrorStatistic.getUser();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("create")
    public User create(@RequestBody User theNewUser) {
        theNewUser.setPassword(theSecurityService.convertSHA256(theNewUser.getPassword()));
        return this.theUserRepository.save(theNewUser);
    }

    @PutMapping("{id}")
    public User update(@PathVariable String id, @RequestBody User theNewUser) {
        User theCurrentUser = this.theUserRepository
                .findById(id)
                .orElse(null);
        if (theCurrentUser != null) {
            theCurrentUser.setName(theNewUser.getName());
            theCurrentUser.setEmail(theNewUser.getEmail());
            theCurrentUser.setPassword(theSecurityService.convertSHA256(theNewUser.getPassword()));
            return this.theUserRepository.save(theCurrentUser);
        } else return null;
    }

    @PutMapping("{userId}/role/{roleId}")
    public User matchRole(@PathVariable String userId,@PathVariable String roleId) {
        User theCurrentUser = this.theUserRepository
                .findById(userId)
                .orElse(null);
        Role theCurrentRole = this.theRoleRepository
                .findById(roleId)
                .orElse(null);

        if (theCurrentUser != null && theCurrentRole != null) {
            theCurrentUser.setRole(theCurrentRole);
            return this.theUserRepository.save(theCurrentUser);
        } else return null;
    }

    @PutMapping("{userId}/unmatch-role/{roleId}")
    public User unMatchRole(@PathVariable String userId, @PathVariable String roleId) {
        User theCurrentUser = this.theUserRepository
                .findById(userId)
                .orElse(null);
        Role theCurrentRole = this.theRoleRepository
                .findById(roleId)
                .orElse(null);

        if (
			theCurrentUser != null && theCurrentRole != null &&
			theCurrentUser.getRole().get_id().equals(roleId)
		) {
            theCurrentUser.setRole(null);
            return this.theUserRepository.save(theCurrentUser);
        } else return null;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        User theUser = this.theUserRepository
                .findById(id)
                .orElse(null);
        if (theUser != null) {
            this.theUserRepository.delete(theUser);
        }
    }
}
