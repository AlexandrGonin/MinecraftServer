package me.sosnovka.MinecraftServer.static_items;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.kyori.adventure.text.Component;

public class StaticMenuCompass {
    public static ItemStack create() {
        return ItemStack.of(Material.COMPASS, 1)
                .withTag(Tag.String("menu_compass"), "true")
                .withCustomName(Component.text("Меню"));
    }

    public static boolean isMenuCompass(ItemStack item) {
        return item != null &&
                !item.isAir() &&
                "true".equals(item.getTag(Tag.String("menu_compass")));
    }
}