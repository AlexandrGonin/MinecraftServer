package me.sosnovka.MinecraftServer.static_items;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.kyori.adventure.text.Component;

public class StaticMenuCompass {
    // Создание компаса
    public static ItemStack create() {
        final Tag<String> MENU_COMPASS_ITEM_TAG = Tag.String("menu_compass");
        return ItemStack.of(Material.COMPASS, 1).withTag(MENU_COMPASS_ITEM_TAG, "true");
    }

    // Проверка компаса
    public static boolean isMenuCompass(ItemStack item) {
        return item != null &&
                !item.isAir() &&
                "true".equals(item.getTag(Tag.String("menu_compass")));
    }
}
