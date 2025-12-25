package me.sosnovka.MinecraftServer.menus;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.kyori.adventure.text.Component;
import net.minestom.server.tag.Tag;

public class LobbyMenu {
    public static Inventory createMenu() {
        Inventory menu = new Inventory(InventoryType.CHEST_3_ROW,
                Component.text("Меню")
        );

        ItemStack glassPane = ItemStack.builder(Material.GRAY_STAINED_GLASS_PANE)
                .customName(Component.text(""))
                .build();

        ItemStack NewGame = ItemStack.builder(Material.EMERALD)
                .customName(Component.text("Новый режим")
                        .color(TextColor.color(0x55FF55))
                        .decorate(TextDecoration.BOLD))
                .build()
                .withTag(Tag.String("menu_action"), "select_newmode");

        for (int i = 0; i < 27; i++) {
            menu.setItemStack(i, glassPane);
        }
        menu.setItemStack(13, NewGame);
        return menu;
    }
}