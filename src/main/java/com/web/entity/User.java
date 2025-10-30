package com.web.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false , length = 150 , unique = true)
    private String email;

    @Column(nullable = false , length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 20)
    private Role role;

    @Column(name = "password",nullable = false , length = 20)
    private String passwordHash;

    @Column(nullable = false , length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at" ,  nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Role{
        PASSENGER,
        CLERK,
        DRIVER,
        DISPATCHER,
        ADMIN
    }


}
