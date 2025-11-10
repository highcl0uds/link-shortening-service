package main.java.core.services;

import main.java.core.enums.NavigationResultEnum;
import main.java.core.models.Link;
import main.java.core.models.User;
import main.java.infra.config.Config;

import java.awt.*;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LinksService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<String, Link> links = new ConcurrentHashMap<>();
    private boolean isFirstMessageForDeleted = true;

    public void startScheduler() {
        scheduler.scheduleAtFixedRate(this::removeDeadLinks, 0, Config.getLinksClearIntervalMinutes(), TimeUnit.MINUTES);
    }

    public void stopScheduler() {
        scheduler.shutdown();
    }

    public Map<String, Link> getLinks() {
        return links;
    }

    public Link generateLink(User user, String inputUrl, int maxConversionsAmount, int linkLifetimeInMinutes) {
        Link link;

        // Создаем ссылку с проверкой на коллизию
        do {
            link = new Link(user.getUserUUID(), inputUrl, maxConversionsAmount, linkLifetimeInMinutes);
        } while (links.containsKey(link.getLinkId()));

        links.put(link.getLinkId(), link);

        return link;
    }

    public NavigationResultEnum navigateToLink(Link link) {
        if (link == null) return NavigationResultEnum.INVALID_LINK;
        if (link.isMaxConversionAchieved()) return NavigationResultEnum.MAX_CONVERSION;
        if (link.isExpired()) return NavigationResultEnum.EXPIRED;

        try {
            Desktop.getDesktop().browse(new URI(link.getFullUrl()));
            link.increaseConversionsAmount();

            return NavigationResultEnum.SUCCESS;
        } catch (Exception e) {
            return NavigationResultEnum.ERROR;
        }
    }

    public Link getLinkById(String searchLinkId) {
        return getLinks().get(searchLinkId);
    }

    public boolean removeLinkByLinkIDWithPermissionCheck(User user, Link link) {
        if (!user.getUserUUID().equals(link.getLinkOwnerUUID())) return false;

        removeLink(link);

        return true;
    }

    public boolean changeMaxConversionsAmountWithPermissionCheck(User user, Link link, int maxConversionsAmount) {
        if (!user.getUserUUID().equals(link.getLinkOwnerUUID())) return false;

        link.setMaxConversionsAmount(maxConversionsAmount);

        return true;
    }

    public String[] getCurrentLinksIdList() {
        return getLinks().values().stream().map(Link::getLinkId).toArray(String[]::new);
    }

    private void removeDeadLinks() {
        List<String> deletedLinks = new ArrayList<>();

        for (Link link : links.values()) {
            link.isExpired();

            if (!link.isLinkAlive()) deletedLinks.add(removeLink(link).getLinkId());
        }

        if (isFirstMessageForDeleted) {
            isFirstMessageForDeleted = false;

            return;
        }

        // Системный вывод для уведомления пользователя об удаленных системой ссылках
        System.out.println("---SYSTEM-INFO: deleted links list by interval---");
        System.out.println(deletedLinks.isEmpty() ? "[]" : deletedLinks);
        System.out.println("---SYSTEM-INFO: deleted links list by interval---");
    }

    private Link removeLink(Link link) {
        return links.remove(link.getLinkId());
    }
}
