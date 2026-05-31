package com.crud.cache;

import com.crud.dto.UserResponse;

import java.util.Optional;

public interface UserCachePort {

    void put(UserCacheView view);

    void putResponse(UserResponse response);

    Optional<UserResponse> findResponseById(Long id);

    void evict(Long id);
}
