//package io.github.jnicog.discord.spanner.bot.spanner;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.IdClass;
//import jakarta.persistence.Table;
//
//@Entity
//@Table(name="spanners")
//@IdClass(SpannerId.class)
//public class Spanner {
//
//    @Id
//    private long userId;
//
//    @Id
//    private long channelId;
//
//    @Column
//    private int spannerCount;
//
//    public Spanner() {
//        // Empty constructor
//    }
//
//    public Spanner(long userId, long channelId) {
//        this.userId = userId;
//        this.channelId = channelId;
//        this.spannerCount = 0;
//    }
//
//    public long getUserId() {
//        return userId;
//    }
//
//    public long getChannelId() {
//        return channelId;
//    }
//
//    public int getSpannerCount () {
//        return spannerCount;
//    }
//
//    public void incrementSpannerCount() {
//        this.spannerCount++;
//    }
//
//}