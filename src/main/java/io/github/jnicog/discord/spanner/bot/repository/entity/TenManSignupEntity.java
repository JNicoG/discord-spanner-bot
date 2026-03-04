package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ten_man_signup")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenManSignupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "date_option_id", nullable = false)
    private TenManDateOptionEntity dateOption;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "signed_up_at", nullable = false, updatable = false)
    private OffsetDateTime signedUpAt;

    public TenManSignupEntity(TenManDateOptionEntity dateOption, Long userId) {
        this.dateOption = dateOption;
        this.userId = userId;
        this.signedUpAt = OffsetDateTime.now();
    }
}
