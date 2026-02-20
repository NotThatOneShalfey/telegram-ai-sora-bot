package com.example.tgbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "referral_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReferralLinks {
    @Id
    @Column(name = "rl_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "rl_link")
    private String link;

    @Column(name = "rl_created_by")
    @OneToOne
    @JoinColumn(name = "id")
    private User created_by;


}
