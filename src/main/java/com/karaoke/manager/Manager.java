package com.karaoke.manager;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
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

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private ManagerType type;

    @Column(nullable = false, length = 60)
    private String passwordHash;

    @Column(nullable = true)
    private Date premium_last_payment;

    public void validateAccountLevel() {
        if (premium_last_payment == null) {
            type = ManagerType.FREE;
            return;
        }
        Date finishDate = type.getFinishDate(premium_last_payment);
        Date now = new Date();
        if (now.after(finishDate)) {
            type = ManagerType.FREE;
        }
    }

    public ManagerResponse toResponse() {
        return new ManagerResponse(email, id.toString(), type, premium_last_payment);
    }
}
