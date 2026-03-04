package io.github.jnicog.discord.spanner.bot.repository.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ten_man_date_option")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TenManDateOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poll_id", nullable = false)
    private TenManPollEntity poll;

    @Column(name = "option_date", nullable = false)
    private LocalDate optionDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "dateOption", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TenManSignupEntity> signups = new ArrayList<>();

    public TenManDateOptionEntity(TenManPollEntity poll, LocalDate optionDate) {
        this.poll = poll;
        this.optionDate = optionDate;
        this.createdAt = OffsetDateTime.now();
    }
}
