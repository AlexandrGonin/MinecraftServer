package me.sosnovka.MinecraftServer.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentEnum;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.command.builder.condition.Conditions;
public class GamemodeCommand extends Command{
    public GamemodeCommand(){
        super("gamemode", "gm");

        setDefaultExecutor((sender, context) -> {
            sender.sendMessage("Неверное исполнение, попробуйте /gamemode <режим игры>");
        });

        var gamemodeArg = ArgumentType.Integer("gamemode");

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            final int gamemodeNumber = context.get(gamemodeArg);
            player.setGameMode(switch (gamemodeNumber) {
                case 0 -> GameMode.SURVIVAL;
                case 1 -> GameMode.CREATIVE;
                case 2 -> GameMode.ADVENTURE;
                case 3 -> GameMode.SPECTATOR;
                default -> GameMode.ADVENTURE;
            });
        }, gamemodeArg);
    }
}
