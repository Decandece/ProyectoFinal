package com.web.service.user;

import com.web.dto.auth.User.UserResponse;
import com.web.dto.auth.User.UserUpdateRequest;
import com.web.dto.auth.User.mapper.UserMapper;
import com.web.entity.User;
import com.web.exception.ResourceNotFoundException;
import com.web.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con email: " + email));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        userMapper.updateEntityFromRequest(request, user);

        User updatedUser = userRepository.save(user);



        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        user.setStatus(User.Status.INACTIVE);
        userRepository.save(user);


    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.Role role) {
        List<User> users = userRepository.findByRoleAndStatus(role, User.Status.ACTIVE);
        return userMapper.toResponseList(users);
    }
}
