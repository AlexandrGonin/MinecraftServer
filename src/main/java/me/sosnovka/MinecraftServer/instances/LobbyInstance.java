package me.sosnovka.MinecraftServer.instances;

import io.github.togar2.pvp.events.PrepareAttackEvent;
import me.sosnovka.MinecraftServer.menus.MainLobbyMenu;
import me.sosnovka.MinecraftServer.menus.DuelsLobbyMenu;
import me.sosnovka.MinecraftServer.static_items.StaticMenuCompass;
import me.sosnovka.MinecraftServer.managers.DuelsQueueManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.entity.EntityDamageEvent;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LobbyInstance extends PublicInstance {

    private DuelsQueueManager duelManager;
    private final Set<UUID> playersWithOpenMenu = new HashSet<>();
    private final EventNode<Event> lobbyEventNode;

    public LobbyInstance(InstanceManager instanceManager) {
        super("world_lobby", instanceManager, new Pos(0, 40, 0), "lobby");
        this.lobbyEventNode = EventNode.all("lobby-custom-events");
        setupLobbyRules();
        // Добавляем кастомные события лобби в глобальный обработчик
        MinecraftServer.getGlobalEventHandler().addChild(lobbyEventNode);
    }

    public void setDuelManager(DuelsQueueManager duelManager) {
        this.duelManager = duelManager;
    }

    public void teleportToLobby(Player player) {
        player.setInstance(instanceContainer, spawnPoint);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFood(20);
        player.getInventory().clear();
        player.getInventory().setEquipment(EquipmentSlot.MAIN_HAND, player.getHeldSlot(), StaticMenuCompass.create());
        player.sendMessage("§aВы вернулись в лобби!");
    }

    private void setupLobbyRules() {

        lobbyEventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(spawnPoint);
            player.setGameMode(GameMode.ADVENTURE);
            player.getInventory().setEquipment(EquipmentSlot.MAIN_HAND, player.getHeldSlot(), StaticMenuCompass.create());
        });

        // Отключаем урон в лобби
        lobbyEventNode.addListener(PrepareAttackEvent.class, damageEvent -> {
            if (damageEvent.getEntity().getInstance() == instanceContainer) {
                damageEvent.setCancelled(true);
            }
        });

        lobbyEventNode.addListener(net.minestom.server.event.inventory.InventoryPreClickEvent.class, event -> {
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
                        MinecraftServer.getSchedulerManager().scheduleNextTick(() -> {
                            handleMenuButton(finalPlayer, finalAction);
                        });
                    }
                }
            }
        });

        lobbyEventNode.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });

        lobbyEventNode.addListener(PlayerSwapItemEvent.class, event -> {
            event.setCancelled(true);
        });

        lobbyEventNode.addListener(PlayerUseItemEvent.class, event -> {
            ItemStack item = event.getItemStack();
            if (StaticMenuCompass.isMenuCompass(item)) {
                event.setCancelled(true);
                Player player = event.getPlayer();
                openLobbyMenu(player);
            }
        });

        lobbyEventNode.addListener(InventoryCloseEvent.class, event -> {
            Player player = (Player) event.getPlayer();
            playersWithOpenMenu.remove(player.getUuid());
        });
    }

    private void openLobbyMenu(Player player) {
        Inventory menu = MainLobbyMenu.createMenu();
        player.openInventory(menu);
        playersWithOpenMenu.add(player.getUuid());
    }

    private void openDuelsMenu(Player player) {
        Inventory menu = DuelsLobbyMenu.createMenu();
        player.openInventory(menu);
        playersWithOpenMenu.add(player.getUuid());
    }

    private void handleMenuButton(Player player, String action) {
        if ("select_duels".equals(action)) {
            player.closeInventory();
            openDuelsMenu(player);
        } else if ("start_duels".equals(action) && duelManager != null) {
            player.closeInventory();
            playersWithOpenMenu.remove(player.getUuid());
            duelManager.joinQueue(player);
        }
    }
}