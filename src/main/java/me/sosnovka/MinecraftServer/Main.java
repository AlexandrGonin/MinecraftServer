package me.sosnovka.MinecraftServer;

import me.sosnovka.MinecraftServer.commands.GamemodeCommand;
import me.sosnovka.MinecraftServer.instances.Lobby;
import me.sosnovka.MinecraftServer.menus.LobbyMenu;
import me.sosnovka.MinecraftServer.static_items.StaticMenuCompass;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSkinInitEvent;
import net.minestom.server.event.player.PlayerSwapItemEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Создаем инстанс
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();

        // Инициализация команд
        MinecraftServer.getCommandManager().register(new GamemodeCommand());

        // Сохранение мира - AnvilLoader из пакета anvil
        try {
            AnvilLoader anvilLoader = new AnvilLoader("world");
            instanceContainer.setChunkLoader(anvilLoader);
            System.out.println("✅ AnvilLoader настроен для сохранения мира");
        } catch (Exception e) {
            System.out.println("❌ Ошибка AnvilLoader: " + e.getMessage());
        }

        // Освещение
        instanceContainer.setChunkSupplier(LightingChunk::new);

        // Предзагрузка чанков
        CompletableFuture.runAsync(() -> {
            var chunks = new ArrayList<CompletableFuture<Chunk>>();
            int radius = 32;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    chunks.add(instanceContainer.loadChunk(x, z));
                }
            }

            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            System.out.println("✅ Чанки загружены");
        });

        // Генератор мира (только для новых чанков)
        instanceContainer.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        });

        // Бесконечный день
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            instanceContainer.setTime(6000);
            instanceContainer.setTimeRate(0);
        }).repeat(TaskSchedule.tick(20)).schedule();

        // Автосохранение каждые 5 минут
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            instanceContainer.saveChunksToStorage();
            System.out.println("💾 Автосохранение мира");
        }).repeat(TaskSchedule.tick(5000)).schedule();

        new Lobby(instanceContainer);

//        // Обработчик игроков
//        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
//        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
//            final Player player = event.getPlayer();
//            event.setSpawningInstance(instanceContainer);
//            player.setRespawnPoint(new Pos(0.5, 42, 0.5));
//            player.setGameMode(GameMode.ADVENTURE);
//            player.getInventory().setItemInHand(Player.Hand.MAIN, StaticMenuCompass.create());
//        });
//
//        // отображение оригинального скина игрока
//        globalEventHandler.addListener(PlayerSkinInitEvent.class, event -> {
//            final Player player = event.getPlayer();
//            String username = player.getUsername();
//            PlayerSkin skin = PlayerSkin.fromUsername(username);
//            event.setSkin(skin);
//        });
//
//        globalEventHandler.addListener(ItemDropEvent.class, event -> {
//            event.setCancelled(true);
//        });
//
//        globalEventHandler.addListener(InventoryPreClickEvent.class, event -> {
//            if (StaticMenuCompass.isMenuCompass(event.getClickedItem()) || event.getClickType() == ClickType.CHANGE_HELD) {
//                event.setCancelled(true);
//            }
//            System.out.println(event.getClickType());
//            System.out.println(event.getClickedItem());
//            });
//
//        globalEventHandler.addListener(PlayerSwapItemEvent.class, event -> {
//            event.setCancelled(true);
//        });
//
//        globalEventHandler.addListener(PlayerUseItemEvent.class, event -> {
//            ItemStack item = event.getItemStack();
//            if (StaticMenuCompass.isMenuCompass(item)) {
//                String menuTag = item.getTag(Tag.String("menu_compass"));
//                if ("true".equals(menuTag)) {
//                    event.setCancelled(true); // Отменяем стандартное действие
//
//                    // Открываем меню из LobbyMenu
//                    Inventory menu = LobbyMenu.createMenu();
//                    event.getPlayer().openInventory(menu);
//                    return;
//                }
//            }
//        });

        // Shutdown hook для сохранения при выключении
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("💾 Сохранение мира при выключении...");
            instanceContainer.saveChunksToStorage();
            MinecraftServer.stopCleanly();
        }));

        // Запуск сервера
        minecraftServer.start("0.0.0.0", 25565);
        System.out.println("🚀 Сервер запущен на порту 25565");
    }
}