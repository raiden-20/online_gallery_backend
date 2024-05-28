package ru.vsu.cs.sheina.online_gallery_backend.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class AuctionSSE<UUID, Integer> {

    private UUID userId;
    private Integer auctionId;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AuctionSSE<UUID, Integer> auctionSSE = (AuctionSSE<UUID, Integer>) o;
        return Objects.equals(userId, auctionSSE.userId) && Objects.equals(auctionId, auctionSSE.auctionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, auctionId);
    }
}
