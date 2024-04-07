package edu.prog3.mssecurity.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import edu.prog3.mssecurity.Models.Role;
import edu.prog3.mssecurity.Models.Permission;
import edu.prog3.mssecurity.Models.RolePermission;
import edu.prog3.mssecurity.Repositories.PermissionRepository;
import edu.prog3.mssecurity.Repositories.RolePermissionRepository;
import edu.prog3.mssecurity.Repositories.RoleRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/role-permission")
public class RolePermissionController {
    @Autowired
    private RoleRepository theRoleRepository;
    @Autowired
    private PermissionRepository thePermissionRepository;
    @Autowired
    private RolePermissionRepository theRolePermissionRepository;

	
    @GetMapping("role/{roleId}")
    public List<RolePermission> findByRole(@PathVariable String roleId) {
        return theRolePermissionRepository.getPermissionsByRole(roleId);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("role/{roleId}/permission/{permissionId}")
    public RolePermission create(@PathVariable String roleId, @PathVariable String permissionId) {
        Role theRole = this.theRoleRepository
                .findById(roleId)
                .orElse(null);
        Permission thePermission = this.thePermissionRepository
                .findById(permissionId)
                .orElse(null);

        if (theRole != null && thePermission != null) {
            RolePermission newRolePermission = new RolePermission();
            newRolePermission.setRole(theRole);
            newRolePermission.setPermission(thePermission);
            return this.theRolePermissionRepository.save(newRolePermission);
		// Why does it still return 201 here?
        } else return null;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("{id}")
    public void delete(@PathVariable String id) {
        RolePermission theRolePermission = this.theRolePermissionRepository
                .findById(id)
                .orElse(null);
        if (theRolePermission != null) {
            this.theRolePermissionRepository.delete(theRolePermission);
        }
    }
}
