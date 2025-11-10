package main.java;

import main.java.UI.MenuService.MenuService;
import main.java.core.services.LinksService;
import main.java.core.services.UsersService;

public class Main {
    public static void main(String[] args) {
        LinksService linksService = new LinksService();
        UsersService usersService = new UsersService();
        MenuService menuService = new MenuService(usersService, linksService);

        linksService.startScheduler();
        menuService.startMenu();
    }
}
