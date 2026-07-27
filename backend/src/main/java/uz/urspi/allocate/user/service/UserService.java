package uz.urspi.allocate.user.service;

import uz.urspi.allocate.user.dto.ProfileUpdateRequest;
import uz.urspi.allocate.user.dto.UserRequest;
import uz.urspi.allocate.user.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse create(UserRequest request);

    List<UserResponse> findAll();

    UserResponse findById(Long id);

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);

    UserResponse changeStatus(Long id);

    UserResponse getCurrentProfile();

    UserResponse updateCurrentProfile(ProfileUpdateRequest request);
}
