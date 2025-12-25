package me.sosnovka.MinecraftServer.menus;

import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.tag.Tag;

public class NewModeMenu {
    public static Inventory createMenu() {
        Inventory menu = new Inventory(InventoryType.CHEST_3_ROW,
                Component.text("Подтверждение")
                        .color(TextColor.color(0xFFAA00))
                        .decorate(TextDecoration.BOLD)
        );

        ItemStack glassPane = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
                .customName(Component.text(""))
                .build();

        // Звезда незера по центру
        ItemStack startGameStar = ItemStack.builder(Material.NETHER_STAR)
                .customName(Component.text("Начать игру")
                        .color(TextColor.color(0x55FF55))
                        .decorate(TextDecoration.BOLD))
                .build()
                .withTag(Tag.String("menu_action"), "start_newmode"); // Тег для старта

        // Заполняем все слоты стеклом
        for (int i = 0; i < 27; i++) {
            menu.setItemStack(i, glassPane);
        }

        // Звезда в центре
        menu.setItemStack(13, startGameStar);

        return menu;
    }
}