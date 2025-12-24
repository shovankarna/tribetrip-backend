package com.triptribe.tripservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "trip_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "trip_id", "user_id" })
}, indexes = {
        @Index(name = "idx_trip_members_user", columnList = "user_id"),
        @Index(name = "idx_trip_members_trip", columnList = "trip_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripMember {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "trip_id", nullable = false)
    private String tripId;

    @Column(name = "user_id", nullable = false)
    private String userId; // Keycloak `sub`

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripRole role;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (this.joinedAt == null) {
            this.joinedAt = Instant.now();
        }
    }
}
