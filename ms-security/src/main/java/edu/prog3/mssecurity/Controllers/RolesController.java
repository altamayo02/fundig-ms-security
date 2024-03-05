package edu.prog3.mssecurity.Controllers;

import edu.prog3.mssecurity.Models.Role;
import edu.prog3.mssecurity.Repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/roles")
public class RolesController {
    @Autowired
    private RoleRepository theRoleRepository;

    
    @GetMapping
    public List<Role> findAll(){
        return this.theRoleRepository.findAll();
    }
    
    @GetMapping("{id}")
    public Role findById(@PathVariable String id) {
        Role theRole = this.theRoleRepository
                .findById(id)
                .orElse(null);
        return theRole;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Role create(@RequestBody Role theNewRole) {
        return this.theRoleRepository.save(theNewRole);
    }

    @PutMapping("{id}")
    public Role update(@PathVariable String id, @RequestBody Role theNewRole) {
        Role theActualRole = this.theRoleRepository
                .findById(id)
                .orElse(null);
        if (theActualRole != null) {
            theActualRole.setName(theNewRole.getName());
            theActualRole.setDescription(theNewRole.getDescription());

            return this.theRoleRepository.save(theActualRole);
        } else {
            return null;
        }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        Role theRole = this.theRoleRepository
                .findById(id)
                .orElse(null);
        if (theRole != null) {
            this.theRoleRepository.delete(theRole);
        }
    }
}
