package com.crud.cache;

public interface UserCachePort {

    void put(UserCacheView view);

    void evict(Long id);
}
