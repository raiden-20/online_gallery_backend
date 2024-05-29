package ru.vsu.cs.sheina.online_gallery_backend.entity;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "auction")
public class AuctionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(name = "name")
    String name;

    @Column(name = "type")
    String type;

    @Column(name = "start_price")
    BigInteger startPrice;

    @Column(name = "current_price")
    BigInteger currentPrice;

    @Column(name = "rate")
    BigInteger rate;

    @Column(name = "artist_id")
    UUID artistId;

    @Column(name = "owner_id")
    UUID ownerId;

    @Column(name = "status")
    String status;

    @Column(name = "description")
    String description;

    @Column(name = "size")
    String size;

    @Column(name = "create_date")
    Timestamp createDate;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "varchar[]")
    List<String> tags;

    @Type(ListArrayType.class)
    @Column(name = "materials", columnDefinition = "varchar[]")
    List<String> materials;

    @Column(name = "frame")
    Boolean frame;

    @Column(name = "publish_date")
    Timestamp publishDate;

    @Column(name = "views")
    Integer views;

    @Column(name = "start_date")
    Timestamp startDate;

    @Column(name = "end_date")
    Timestamp endDate;
}
