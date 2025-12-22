package me.sosnovka.MinecraftServer;

import me.sosnovka.MinecraftServer.commands.GamemodeCommand;
import me.sosnovka.MinecraftServer.instances.Lobby;
import me.sosnovka.MinecraftServer.instances.NewMode;
import net.minestom.server.MinecraftServer;

public class Main {
    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();

        MinecraftServer.getCommandManager().register(new GamemodeCommand());

        net.minestom.server.instance.InstanceManager instanceManager = MinecraftServer.getInstanceManager();

        NewMode newMode = new NewMode(instanceManager);
        Lobby lobby = new Lobby(instanceManager);
        lobby.setNewMode(newMode);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Выключение сервера");
            MinecraftServer.stopCleanly();
        }));

        minecraftServer.start("0.0.0.0", 25565);
        System.out.println("Сервер запущен на порту 25565");
    }
}