package me.sosnovka.MinecraftServer.instances;

import me.sosnovka.MinecraftServer.menus.LobbyMenu;
import me.sosnovka.MinecraftServer.static_items.StaticMenuCompass;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSkinInitEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;

public class Lobby {

    // Простой конструктор, как в Main.java
    public Lobby(InstanceContainer instanceContainer) {
        // Создаем свою EventNode для этого лобби
        EventNode<Event> lobbyEvents = EventNode.all("lobby");


        // 1. Подключение игрока
        lobbyEvents.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(new Pos(0.5, 42, 0.5));
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setItemInMainHand(StaticMenuCompass.create());
        });

        // 2. Скины игроков
        lobbyEvents.addListener(PlayerSkinInitEvent.class, event -> {
            final Player player = event.getPlayer();
            String username = player.getUsername();
            PlayerSkin skin = PlayerSkin.fromUsername(username);
            event.setSkin(skin);
        });

        // 3. Запрет выброса предметов
        lobbyEvents.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });

        // 4. Защита инвентаря
        lobbyEvents.addListener(InventoryPreClickEvent.class, event -> {
            if (StaticMenuCompass.isMenuCompass(event.getClickedItem()) ||
                    event.getClickType() == ClickType.CHANGE_HELD) {
                event.setCancelled(true);
            }
        });

        // 5. Запрет свапа предметов
        lobbyEvents.addListener(PlayerSwapItemEvent.class, event -> {
            event.setCancelled(true);
        });

        // 6. Меню по ПКМ на компас
        lobbyEvents.addListener(PlayerUseItemEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (StaticMenuCompass.isMenuCompass(item)) {
                String menuTag = item.getTag(Tag.String("menu_compass"));
                if ("true".equals(menuTag)) {
                    event.setCancelled(true);
                    Inventory menu = LobbyMenu.createMenu();
                    event.getPlayer().openInventory(menu);
                }
            }
        });

        // Подключаем события этого лобби к серверу
        MinecraftServer.getGlobalEventHandler().addChild(lobbyEvents);
    }
}