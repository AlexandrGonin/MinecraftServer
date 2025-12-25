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

import java.util.ArrayList;
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

        MinecraftServer.getGlobalEventHandler().addChild(eventNode);
    }

    private void setupInstance() {
        // Загрузка мира
        try {
            instanceContainer.setChunkLoader(new AnvilLoader(worldName));
            System.out.println("Мир " + worldName + " загружен");
        } catch (Exception e) {
            System.out.println("Мир " + worldName + " не найден, создается новый");
            instanceContainer.setGenerator(unit -> {
                unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK);
            });
        }

        // Освещение
        instanceContainer.setChunkSupplier(LightingChunk::new);

        // Предзангрузка и освещение
        CompletableFuture.runAsync(() -> {
            var chunks = new ArrayList<CompletableFuture<Chunk>>();
            int radius = 32;

            // Загружаем чанки в радиусе 32 от точки спавна
            int chunkX = (int) Math.floor(spawnPoint.x() / 16);
            int chunkZ = (int) Math.floor(spawnPoint.z() / 16);

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    chunks.add(instanceContainer.loadChunk(chunkX + x, chunkZ + z));
                }
            }

            CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).join();
            System.out.println("Загружено " + chunks.size() + " чанков для " + worldName);

            // Предварительный расчет освещения
            LightingChunk.relight(instanceContainer, instanceContainer.getChunks());
            System.out.println("Освещение рассчитано для " + worldName);
        });

        // Бесконечный день
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            instanceContainer.setTime(6000);
            instanceContainer.setTimeRate(0);
        }).delay(TaskSchedule.tick(20)).schedule();
    }

    protected void setupCommonRules() {
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