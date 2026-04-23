package com.karaoke.manager;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import java.util.Date;

@Getter
public class Manager {
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private ManagerType type;


    @Column(nullable = true)
    private Date premium_last_payment;
}
