package edu.prog3.mssecurity.Repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import edu.prog3.mssecurity.Models.RolePermission;

public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {
    @Query("{'role.$id': ObjectId(?0)}")
    List<RolePermission> getPermissionsByRole(String roleId);

    @Query("{'role.$id': ObjectId(?0), 'permission.$id': ObjectId(?1)}")
    RolePermission getRolePermission(String roleId, String permissionId);
}
