package com.karaoke.manager;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Manager {
    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    private String email;
    private ManagerType type;


    @Column(nullable = true)
    private Date premium_last_payment;

    public void validateAccountLevel() {
        Date finishDate = type.getFinishDate(premium_last_payment);
        Date now = new Date();
        if (now.after(finishDate)) {
            type = ManagerType.FREE;
        }
    }
}
