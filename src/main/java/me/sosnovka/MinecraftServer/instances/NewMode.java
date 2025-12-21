package me.sosnovka.MinecraftServer.instances;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;

public class NewMode extends PublicInstance {

    public NewMode(InstanceManager instanceManager) {
        super("world_newmode", instanceManager, new Pos(0.5, 40, 0.5), "newmode"); // ТАКАЯ ЖЕ ТОЧКА
        setupNewModeRules();
    }

    private void setupNewModeRules() {
        // Подключение игрока
        eventNode.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
            player.setRespawnPoint(spawnPoint);
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
        });
    }

    // Метод для телепортации с обработкой освещения
    public void teleportPlayer(Player player) {
        player.setInstance(instanceContainer, spawnPoint);
        player.setGameMode(GameMode.SURVIVAL);
        player.getInventory().clear();
        player.sendMessage("§aВы вошли в NewMode!");

    }
}