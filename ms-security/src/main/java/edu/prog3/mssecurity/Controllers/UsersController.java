package edu.prog3.mssecurity.Controllers;

import edu.prog3.mssecurity.Models.Role;
import edu.prog3.mssecurity.Models.User;
import edu.prog3.mssecurity.Repositories.RoleRepository;
import edu.prog3.mssecurity.Repositories.UserRepository;
import edu.prog3.mssecurity.Services.EncryptionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RoleRepository theRoleRepository;
    @Autowired
    private EncryptionService theEncryptionService;

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

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public User create(@RequestBody User theNewUser) {
        theNewUser.setPassword(theEncryptionService.convertSHA256(theNewUser.getPassword()));
        return this.theUserRepository.save(theNewUser);
    }

    @PutMapping("{id}")
    public User update(@PathVariable String id, @RequestBody User theNewUser) {
        User theActualUser = this.theUserRepository
                .findById(id)
                .orElse(null);
        if (theActualUser != null) {
            theActualUser.setName(theNewUser.getName());
            theActualUser.setEmail(theNewUser.getEmail());
            theActualUser.setPassword(theEncryptionService.convertSHA256(theNewUser.getPassword()));
            return this.theUserRepository.save(theActualUser);
        } else {
            return null;
        }
    }

    @PutMapping("{userId}/role/{roleId}")
    public User matchRole(@PathVariable String userId,@PathVariable String roleId) {
        User theActualUser = this.theUserRepository
                .findById(userId)
                .orElse(null);
        Role theActualRole=this.theRoleRepository
                .findById(roleId)
                .orElse(null);

        if (theActualUser != null && theActualRole!=null) {
            theActualUser.setRole(theActualRole);
            return this.theUserRepository.save(theActualUser);
        } else {
            return null;
        }
    }

    @PutMapping("{userId}/unmatch-role/{roleId}")
    public User unMatchRole(@PathVariable String userId, @PathVariable String roleId) {
        User theActualUser = this.theUserRepository
                .findById(userId)
                .orElse(null);
        Role theActualRole = this.theRoleRepository
                .findById(roleId)
                .orElse(null);

        if (theActualUser != null
                && theActualRole!=null
                && theActualUser.getRole().get_id().equals(roleId)) {
            theActualUser.setRole(null);
            return this.theUserRepository.save(theActualUser);
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
