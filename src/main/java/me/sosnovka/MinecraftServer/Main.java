package me.sosnovka.MinecraftServer;

import io.github.togar2.pvp.MinestomPvP;
import io.github.togar2.pvp.feature.CombatFeatures;
import me.sosnovka.MinecraftServer.commands.GamemodeCommand;
import me.sosnovka.MinecraftServer.instances.DuelsInstance;
import me.sosnovka.MinecraftServer.instances.LobbyInstance;
import me.sosnovka.MinecraftServer.managers.DuelsQueueManager;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.instance.InstanceManager;


public class Main {
    public static void main(String[] args) {
        // Инициализация сервера
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Инициализация MinestomPvP до всего
        MinestomPvP.init(true, true);

        // Регистрация команды /gamemode
        MinecraftServer.getCommandManager().register(new GamemodeCommand());

        // Менеджер инстансов
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Создание инстансов
        LobbyInstance lobby = new LobbyInstance(instanceManager);
        DuelsQueueManager duelManager = new DuelsQueueManager(instanceManager, lobby);
        lobby.setDuelManager(duelManager);

        // Правильная остановка сервера
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown");
            MinecraftServer.stopCleanly();
        }));

        // Запуск сервера
        minecraftServer.start("0.0.0.0", 25565);
        System.out.println("The server is running on port 25565");
    }
}