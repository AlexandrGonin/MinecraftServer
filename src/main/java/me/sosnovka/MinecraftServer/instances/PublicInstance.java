package me.sosnovka.MinecraftServer.instances;

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
import net.minestom.server.instance.*;
import net.minestom.server.instance.block.Block;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.inventory.click.ClickType;
import net.minestom.server.timer.TaskSchedule;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.util.concurrent.CompletableFuture;

public class PublicInstance {

    protected final InstanceContainer instanceContainer;
    protected final EventNode<Event> eventNode;
    protected final Pos spawnPoint;
    protected final String worldName;

    public PublicInstance(String worldName, InstanceManager instanceManager, Pos spawnPoint, String nodeName) {
        this.worldName = worldName;
        this.spawnPoint = spawnPoint;

        this.instanceContainer = instanceManager.createInstanceContainer();
        this.eventNode = EventNode.all(nodeName);

        setupInstance();
        setupCommonRules();

        // Подключаем к серверу
        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    private void setupInstance() {
        // Сохранение мира
        try {
            instanceContainer.setChunkLoader(new AnvilLoader(worldName));
            System.out.println("✅ Мир '" + worldName + "' настроен");
        } catch (Exception e) {
            System.out.println("❌ Ошибка загрузки мира '" + worldName + "': " + e.getMessage());
        }

        // ОСВЕЩЕНИЕ - ВАЖНО!
        instanceContainer.setChunkSupplier(LightingChunk::new);

        // Одинаковый генератор для всех (трава)
        instanceContainer.setGenerator(unit -> {
            unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
        });

        // ПРЕДЗАГРУЗКА БОЛЬШЕ ЧАНКОВ ДЛЯ ОСВЕЩЕНИЯ
        CompletableFuture.runAsync(() -> {
            int radius = 8; // УВЕЛИЧИЛ РАДИУС ДО 8 (было 5)
            int loaded = 0;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    instanceContainer.loadChunk(x, z);
                    loaded++;
                }
            }
            System.out.println("✅ Загружено " + loaded + " чанков в мире '" + worldName + "'");
        });

        // Бесконечный день
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            instanceContainer.setTime(6000);
            instanceContainer.setTimeRate(0);
        }).delay(TaskSchedule.tick(60)).schedule(); // УВЕЛИЧИЛ ЗАДЕРЖКУ ДО 3 СЕКУНД

        // Автосохранение
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            instanceContainer.saveChunksToStorage();
        }).repeat(TaskSchedule.tick(5000)).schedule();
    }

    protected void setupCommonRules() {
        // Запрет выброса предметов
        eventNode.addListener(ItemDropEvent.class, event -> {
            event.setCancelled(true);
        });

        // Запрет свапа предметов
        eventNode.addListener(PlayerSwapItemEvent.class, event -> {
            event.setCancelled(true);
        });

        // Скины игроков
        eventNode.addListener(PlayerSkinInitEvent.class, event -> {
            Player player = event.getPlayer();
            PlayerSkin skin = PlayerSkin.fromUsername(player.getUsername());
            event.setSkin(skin);
        });
    }

    public InstanceContainer getInstanceContainer() {
        return instanceContainer;
    }

    public EventNode<Event> getEventNode() {
        return eventNode;
    }

    public Pos getSpawnPoint() {
        return spawnPoint;
    }
}