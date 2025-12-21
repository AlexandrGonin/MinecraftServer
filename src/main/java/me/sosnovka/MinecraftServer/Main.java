package me.sosnovka.MinecraftServer;

import me.sosnovka.MinecraftServer.commands.GamemodeCommand;
import me.sosnovka.MinecraftServer.instances.Lobby;
import me.sosnovka.MinecraftServer.instances.NewMode;
import net.minestom.server.MinecraftServer;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        // Регистрация команд
        MinecraftServer.getCommandManager().register(new GamemodeCommand());

        // Получаем менеджер инстансов
        net.minestom.server.instance.InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        // Создаем зоны - каждая сама настроит свой мир
        NewMode newMode = new NewMode(instanceManager);
        Lobby lobby = new Lobby(instanceManager);
        lobby.setNewMode(newMode);

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("💾 Сохранение миров...");
            MinecraftServer.stopCleanly();
        }));

        // Запуск сервера
        minecraftServer.start("0.0.0.0", 25565);
        System.out.println("🚀 Сервер запущен на порту 25565");
    }
}