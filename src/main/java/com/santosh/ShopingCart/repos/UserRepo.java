package com.santosh.ShopingCart.repos;

import com.santosh.ShopingCart.entites.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<Users,Integer> {
    public Users findByEmailAndPassword(String email, String password);
    public Users findByEmail(String email);
}
