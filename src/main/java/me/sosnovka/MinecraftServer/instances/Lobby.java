package me.sosnovka.MinecraftServer.instances;

import me.sosnovka.MinecraftServer.menus.LobbyMenu;
import me.sosnovka.MinecraftServer.menus.NewModeMenu;
import me.sosnovka.MinecraftServer.static_items.StaticMenuCompass;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Lobby extends PublicInstance {

    private NewMode newMode;
    private final Set<UUID> playersWithOpenMenu = new HashSet<>();

    public Lobby(InstanceManager instanceManager) {
        super("world_lobby", instanceManager, new Pos(0, 40, 0), "lobby");
        setupLobbyRules();
    }

    private void setupLobbyRules() {
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(spawnPoint);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setEquipment(EquipmentSlot.MAIN_HAND, player.getHeldSlot(), StaticMenuCompass.create());
        });

        eventNode.addListener(net.minestom.server.event.inventory.InventoryPreClickEvent.class, event -> {
            Click click = event.getClick();
            if (click instanceof Click.RightDrag ||
                    click instanceof Click.LeftDrag ||
                    click instanceof Click.LeftShift ||
                    click instanceof Click.RightShift ||
                    click instanceof Click.OffhandSwap ||
                    click instanceof Click.HotbarSwap ||
                    click instanceof Click.Left ||
                    click instanceof Click.Right) {
                event.setCancelled(true);
            }

            Player player = event.getPlayer();
            if (playersWithOpenMenu.contains(player.getUuid())) {
                event.setCancelled(true);

                ItemStack clicked = event.getClickedItem();
                if (clicked != null && !clicked.isAir()) {
                    String action = clicked.getTag(Tag.String("menu_action"));

                    if (action != null) {
                        Player finalPlayer = player;
                        String finalAction = action;
                        net.minestom.server.MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                            handleMenuButton(finalPlayer, finalAction);
                        });
                    }
                }
            }
        });

        eventNode.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });

        eventNode.addListener(PlayerSwapItemEvent.class, event -> {
            event.setCancelled(true);
        });

        eventNode.addListener(PlayerUseItemEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (StaticMenuCompass.isMenuCompass(item)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                openLobbyMenu(player);
            }
        });

        eventNode.addListener(net.minestom.server.event.inventory.InventoryCloseEvent.class, event -> {
            Player player = (Player) event.getPlayer();
            playersWithOpenMenu.remove(player.getUuid());
        });
    }

    private void openLobbyMenu(Player player) {
        Inventory menu = LobbyMenu.createMenu();
        player.openInventory(menu);
        playersWithOpenMenu.add(player.getUuid());
    }

    private void openNewModeMenu(Player player) {
        Inventory menu = NewModeMenu.createMenu();
        player.openInventory(menu);
        playersWithOpenMenu.add(player.getUuid());
    }

    private void handleMenuButton(Player player, String action) {
        if ("select_newmode".equals(action)) {
            player.closeInventory();
            openNewModeMenu(player);
        } else if ("start_newmode".equals(action) && newMode != null) {
            player.closeInventory();
            playersWithOpenMenu.remove(player.getUuid());
            newMode.teleportPlayer(player);
        }
    }

    public void setNewMode(NewMode newMode) {
        this.newMode = newMode;
    }
}