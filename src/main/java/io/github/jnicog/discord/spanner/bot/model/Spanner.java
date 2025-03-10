/*
package io.github.jnicog.discord.spanner.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="Spanners")
public class Spanner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userId;

    @Column
    private int spannerCount;

    public Spanner() {
    }

    public Spanner(int userId) {
        this.userId = userId;
        this.spannerCount = 0;
    }

    public long getUserId() {
        return userId;
    }

    public int getSpannerCount () {
        return spannerCount;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSpannerCount(int spannerCount) {
        this.spannerCount = spannerCount;
    }

    public void incrementSpannerCount() {
        this.spannerCount++;
    }

}
*/
