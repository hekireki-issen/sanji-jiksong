package hekireki.sanjijiksong.global.security.entity;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refresh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private String expiration;
}
