package io.github.jnicog.discord.spanner.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Spanners")
public class Spanner {

    @Id
    private long userId;

    @Column(nullable = false)
    private int spannerCount;

    public Spanner() {
    }

    public Spanner(long userId) {
        this.userId = userId;
        this.spannerCount = 0;
    }

    public long getUserId() {
        return userId;
    }

    public int getSpannerCount () {
        return spannerCount;
    }

    public void incrementSpannerCount() {
        this.spannerCount++;
    }

}
