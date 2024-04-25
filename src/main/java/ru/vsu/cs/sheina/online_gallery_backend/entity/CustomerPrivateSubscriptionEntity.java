package ru.vsu.cs.sheina.online_gallery_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "customer_private_subscription")
public class CustomerPrivateSubscriptionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "private_subscription_id")
    Integer privateSubscriptionId;

    @Column(name = "customer_id")
    UUID customerId;

    @Column(name = "create_date")
    Timestamp createDate;
}
