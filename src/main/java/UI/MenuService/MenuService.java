package main.java.UI.MenuService;

import main.java.core.enums.NavigationResultEnum;
import main.java.core.models.Link;
import main.java.core.models.User;
import main.java.core.services.LinksService;
import main.java.core.services.UsersService;
import main.java.infra.config.Config;

import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

public class MenuService {
    private final Scanner scanner;
    private final UsersService usersService;
    private final LinksService linksService;

    public MenuService(UsersService usersService, LinksService linksService) {
        this.scanner = new Scanner(System.in);
        this.usersService = usersService;
        this.linksService = linksService;
    }

    public void startMenu() {
        System.out.println("\nВы используете сервис коротких ссылок\n");
        chooseUser();

        boolean isRunning = true;
        while (isRunning) {
            menu();
            String option = scanNextLine();

            switch (option) {
                case "1":
                    createLink();
                    break;
                case "2":
                    getLinkInfo();
                    break;
                case "3":
                    openLink();
                    break;
                case "4":
                    deleteLink();
                    break;
                case "5":
                    changeMaxConversionsForLink();
                    break;
                case "6":
                    getAllShortUrls();
                    break;
                case "7":
                    chooseUser();
                    break;
                case "8":
                    isRunning = false;
                    break;
                default:
                    menu();
            }
        }

        linksService.stopScheduler();
    }

    private String scanNextLine() {
        if (usersService.getCurrentUser() == null) {
            return scanner.nextLine().trim();
        }

        System.out.print(usersService.getCurrentUser().getUserUUIDStr() + " > ");
        return scanner.nextLine().trim();
    }

    private Link getLinkByReadInput() {
        String idStr = scanNextLine();

        if (idStr.startsWith(Config.getBaseUrl())) idStr = idStr.substring(Config.getBaseUrl().length());

        Link link = linksService.getLinkById(idStr);

        if (link == null) {
            System.out.println("Ссылка не найдена");
            System.out.println("\n");
        }

        return link;
    }

    private void menu() {
        System.out.println("\n");
        System.out.println("Выберите действие:");
        System.out.println("1. Создать ссылку");
        System.out.println("2. Вывести информацию о ссылке");
        System.out.println("3. Открыть ссылку");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Изменить количество переходов по ссылке");
        System.out.println("6. Вывести список доступных коротких ссылок");
        System.out.println("7. Сменить пользователя");
        System.out.println("8. Завершить программу");
        System.out.println("\n");
    }

    private void createLink() {
        System.out.println("\n");
        String conditionStr = "Url должен начинаться с 'http://' либо с 'https://'";
        System.out.println("Введите url, для которого хотите сделать короткую ссылку");
        System.out.println(conditionStr);

        if (usersService.getCurrentUser() == null) {
            chooseUser();
        }

        String url = scanNextLine();
        url = url.toLowerCase();

        if (url.isEmpty() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
            System.out.println("Не удалось создать ссылку, " + conditionStr);

            return;
        }

        System.out.println("Введите количество переходов для генерируемой ссылки");
        int maxConversionsAmount;

        try {
            maxConversionsAmount = scanner.nextInt();
        } catch (Exception e) {
            System.out.printf("Некорректный ввод. Было установлено значение по умолчанию %s", Config.getLinkMaxConversionAmount());
            maxConversionsAmount = Config.getLinkMaxConversionAmount();
        } finally {
            scanner.nextLine();
        }

        Link link = linksService.generateLink(usersService.getCurrentUser(), url, maxConversionsAmount, Config.getLinkLifetimeMinutes());

        System.out.println("Ссылка создана");
        printLinkInfo(link);
    }

    private void printLinkInfo(Link link) {
        System.out.println("\n");
        System.out.printf("ID ссылки: %s; ID владельца:%s \n", link.getLinkId(), link.getLinkOwnerUUID());
        System.out.printf("Дата создания: %s; Дата окончания: %s\n", link.getCreatedAt(), link.getExpiredAt());
        System.out.printf("Короткий url: %s; Полный url: %s\n", link.getShortUrl(), link.getFullUrl());
        System.out.printf("Количество переходов: %s/%s\n", link.getCurrentConversionsAmount(), link.getMaxConversionsAmount());
        System.out.println("\n");
    }

    private void getLinkInfo() {
        System.out.println("\n");
        System.out.println("Введите id/короткий url ссылки, чтобы получить о ней информацию");

        Link link = getLinkByReadInput();

        if  (link == null) return;

        printLinkInfo(link);
    }

    private void openLink () {
        System.out.println("\n");
        System.out.println("Введите id/короткий url ссылки, чтобы открыть ее");

        Link link = getLinkByReadInput();

        if (link == null) {
            System.out.println("\n");

            return;
        };

        NavigationResultEnum navResult = linksService.navigateToLink(link);

        switch (navResult) {
            case INVALID_LINK:
                break;
            case ERROR:
                System.out.println("Не удалось осуществить переход по ссылке \n");
                break;
            case EXPIRED:
                System.out.printf("Переход по ссылке невозможен: ссылка протухла %s" + "...%n", link.getExpiredAt());
                break;
            case MAX_CONVERSION:
                System.out.printf("Переход по ссылке невозможен: количество переходов %s/%s" + "...%n", link.getCurrentConversionsAmount(), link.getMaxConversionsAmount());
                break;
            case SUCCESS:
                System.out.printf("Осуществляется переход по ссылке %s(%s)" + "...%n", link.getShortUrl(), link.getFullUrl());
                break;
        }

    }

    private void deleteLink () {
        System.out.println("\n");
        System.out.println("Данное действие возможно только если вы являетесь владельцем ссылки\n");
        System.out.println("Введите id/короткий url ссылки, чтобы удалить ее");

        Link link = getLinkByReadInput();

        if (link == null) {
            System.out.println("\n");

            return;
        };

        boolean isDeleted = linksService.removeLinkByLinkIDWithPermissionCheck(usersService.getCurrentUser(), link);

        if (isDeleted) System.out.println("Ссылка успешно удалена");
        else System.out.println("Отказано в доступе");

        System.out.println("\n");
    }

    private void changeMaxConversionsForLink () {
        System.out.println("\n");
        System.out.println("Данное действие возможно только если вы являетесь владельцем ссылки\n");
        System.out.println("Введите id/короткий url ссылки, чтобы изменить ее максимальное количество переходов");

        Link link = getLinkByReadInput();

        if (link == null) {
            System.out.println("\n");
            return;
        };

        System.out.println("Введите желаемое количество переходов");
        int newMaxConversionsAmount;

        try {
            newMaxConversionsAmount = scanner.nextInt();
        } catch (Exception e) {
            System.out.printf("Некорректный ввод. Сохраняем текущее значение (=%s)%n", link.getMaxConversionsAmount());
            newMaxConversionsAmount = link.getMaxConversionsAmount();
        } finally {
            scanner.nextLine();
        }

        boolean isChanged = linksService.changeMaxConversionsAmountWithPermissionCheck(usersService.getCurrentUser(), link, newMaxConversionsAmount);

        if (isChanged) System.out.printf("Количество переходов по ссылке успешно изменено, сейчас: %s/%s", link.getCurrentConversionsAmount(), newMaxConversionsAmount);
        else System.out.println("Отказано в доступе");

        System.out.println("\n");
    }

    private void getAllShortUrls () {
        System.out.println("\n");
        System.out.println("Список ID существующих ссылок");
        System.out.println(Arrays.toString(linksService.getCurrentLinksIdList()));
        System.out.println("\n");
    }

    private void chooseUser() {
        System.out.println("\n");
        User chosenUser = null;

        while (chosenUser == null) {
            System.out.println("Введите UUID пользователя (например: 123e4567-e89b-12d3-a456-426614174000) или нажмите Enter оставив ввод пустым для создания нового пользователя\n");
            String userUUIDStr = scanNextLine();

            if (userUUIDStr.isEmpty()) {
                chosenUser = usersService.createUser();
            } else {
                try {
                    chosenUser = usersService.getUserByUUID(UUID.fromString(userUUIDStr));

                    if (chosenUser == null) {
                        System.out.println("Введенный пользователь не найден\n");
                    }
                } catch (Exception e) {
                    System.out.println("Неверный формат UUID\n");
                }
            }
        }

        System.out.println("Вы используете пользователя: " + chosenUser.getUserUUIDStr() +"\n");
        usersService.setCurrentUser(chosenUser);
    }
}
