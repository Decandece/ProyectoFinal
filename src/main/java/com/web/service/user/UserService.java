package com.web.service.user;

import com.web.dto.auth.User.UserResponse;
import com.web.dto.auth.User.UserUpdateRequest;
import com.web.entity.User;

import java.util.List;

public interface UserService {
    
    List<UserResponse> getAll();
    
    UserResponse getById(Long id);
    
    UserResponse getByEmail(String email);
    
    UserResponse updateUser(Long id, UserUpdateRequest request);
    
    void deleteUser(Long id);
    
    List<UserResponse> getUsersByRole(User.Role role);
}

