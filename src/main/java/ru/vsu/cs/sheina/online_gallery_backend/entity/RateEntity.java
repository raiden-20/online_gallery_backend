package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "rate")
public class RateEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "auction_id")
    Integer auctionId;

    @Column(name = "customer_id")
    UUID customerId;

    @Column(name = "is_anonymous")
    Boolean isAnonymous;

    @Column(name = "rate")
    BigInteger rate;

    @Column(name = "create_date")
    Timestamp createDate;

}
