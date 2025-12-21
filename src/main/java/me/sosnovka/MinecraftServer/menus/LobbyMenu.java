package me.sosnovka.MinecraftServer.menus;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.text.Component;

public class LobbyMenu {
    public static Inventory createMenu() {
        // Создание инвентаря как меню
        Inventory menu = new Inventory(InventoryType.CHEST_3_ROW,
                Component.text("Меню")
        );

        // Заполняем все слоты стеклянными панелями
        ItemStack glassPane = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
                .customName(Component.text(""))
                .build();

        ItemStack NewGame = ItemStack.builder(Material.EMERALD)
                .customName(Component.text("NewGame"))
                .build();

        for (int i = 0; i < 27; i++) {
            menu.setItemStack(i, glassPane);
        }
        menu.setItemStack(13, NewGame);
        return menu;
    }
}
