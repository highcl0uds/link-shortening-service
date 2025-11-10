package main.java.core.models;

import main.java.infra.config.Config;
import main.java.util.ShortIDGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

public class Link {
    private final UUID linkOwnerUUID;
    private final String fullUrl;
    private final String linkId;
    private final String shortUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiredAt;
    private int maxConversionsAmount;
    private int currentConversionsAmount;
    private boolean isLinkAlive;

    public Link(UUID linkOwnerUUID, String fullUrl, int maxConversionsAmount, int linkLifetimeInMinutes) {
        this.linkOwnerUUID = linkOwnerUUID;
        this.fullUrl = fullUrl;
        this.linkId = ShortIDGenerator.generateShortID();
        this.shortUrl = Config.getBaseUrl() + linkId;
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusMinutes(linkLifetimeInMinutes);
        this.maxConversionsAmount = maxConversionsAmount;
        this.currentConversionsAmount = 0;
        this.isLinkAlive = true;
    }

    public UUID getLinkOwnerUUID() {
        return linkOwnerUUID;
    }

    public String getFullUrl() {
        return fullUrl;
    }

    public String getLinkId() {
        return linkId;
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public int getMaxConversionsAmount() {
        return maxConversionsAmount;
    }

    public int getCurrentConversionsAmount() {
        return currentConversionsAmount;
    }

    public boolean isLinkAlive() {
        return isLinkAlive;
    }

    public void increaseConversionsAmount() {
        currentConversionsAmount++;

        if (currentConversionsAmount >= maxConversionsAmount) {
            isLinkAlive = false;
        }
    }

    public boolean isExpired() {
        if (!LocalDateTime.now().isAfter(expiredAt)) return false;
        isLinkAlive = false;

        return true;
    }

    public boolean isMaxConversionAchieved() {
        return currentConversionsAmount >= maxConversionsAmount;
    }

    public void setMaxConversionsAmount(int inputMaxConversionsAmount) {
        this.maxConversionsAmount = inputMaxConversionsAmount;
        this.isLinkAlive = !isMaxConversionAchieved() && !isExpired();
    }
}
