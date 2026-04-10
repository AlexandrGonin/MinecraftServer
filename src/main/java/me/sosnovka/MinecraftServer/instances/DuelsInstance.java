package me.sosnovka.MinecraftServer.instances;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDeathEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import io.github.togar2.pvp.feature.CombatFeatures;

public class DuelsInstance extends PublicInstance {

    private final String duelId;
    private Player player1;
    private Player player2;
    private boolean active = true;
    private boolean fightStarted = false;
    private LobbyInstance lobby;
    private final EventNode<Event> duelEventNode; // СВОЙ узел для дуэли

    private final Pos SPAWN_1 = new Pos(0.5, 41, 5.5);
    private final Pos SPAWN_2 = new Pos(0.5, 41, -4.5);

    private Runnable onDuelEndCallback;

    public DuelsInstance(InstanceManager instanceManager, String duelId, LobbyInstance lobby) {
        super("world_duels", instanceManager, new Pos(0.5, 41, 0.5), "duel-" + duelId);
        this.duelId = duelId;
        this.lobby = lobby;
        this.duelEventNode = EventNode.all("duel-custom-events-" + duelId);

        // Включаем PvP для дуэли в её собственном узле
        var pvpNode = CombatFeatures.modernVanilla().createNode();
        duelEventNode.addChild(pvpNode);

        setupDuelRules();
        // Регистрируем узел дуэли в глобальный обработчик
        MinecraftServer.getGlobalEventHandler().addChild(duelEventNode);
    }

    public void setOnDuelEndCallback(Runnable callback) {
        this.onDuelEndCallback = callback;
    }

    private void setupDuelRules() {
        duelEventNode.addListener(PlayerDeathEvent.class, deathEvent -> {
            deathEvent.setChatMessage(null);
            deathEvent.setDeathText(null);

            if (!fightStarted) return;

            Player dead = deathEvent.getPlayer();
            if (dead == player1) {
                endDuel(player2, player1);
            } else if (dead == player2) {
                endDuel(player1, player2);
            }
        });

        duelEventNode.addListener(PlayerDisconnectEvent.class, disconnectEvent -> {
            Player disconnected = disconnectEvent.getPlayer();
            if (!active) return;

            if (disconnected == player1) {
                endDuel(player2, player1);
            } else if (disconnected == player2) {
                endDuel(player1, player2);
            }
        });
    }

    public void teleportPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;

        p1.setInstance(instanceContainer, SPAWN_1);
        p2.setInstance(instanceContainer, SPAWN_2);

        p1.addViewer(p2);
        p2.addViewer(p1);

        p1.setGameMode(GameMode.SURVIVAL);
        p2.setGameMode(GameMode.SURVIVAL);
        p1.setHealth(20);
        p2.setHealth(20);
        p1.setFood(20);
        p2.setFood(20);

        p1.getInventory().clear();
        p2.getInventory().clear();

        startCountdown();
    }

    private void startCountdown() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (player1 != null) player1.sendMessage("§aДуэль начнется через 3 секунды!");
            if (player2 != null) player2.sendMessage("§aДуэль начнется через 3 секунды!");
        }).delay(TaskSchedule.tick(20)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (player1 != null) player1.sendMessage("§a3");
            if (player2 != null) player2.sendMessage("§a3");
        }).delay(TaskSchedule.tick(40)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (player1 != null) player1.sendMessage("§a2");
            if (player2 != null) player2.sendMessage("§a2");
        }).delay(TaskSchedule.tick(60)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (player1 != null) player1.sendMessage("§a1");
            if (player2 != null) player2.sendMessage("§a1");
        }).delay(TaskSchedule.tick(80)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            fightStarted = true;
        }).delay(TaskSchedule.tick(100)).schedule();
    }

    public void endDuel(Player winner, Player loser) {
        if (!active) return;

        active = false;
        fightStarted = false;

        if (winner != null && loser != null) {
            winner.sendMessage("§aПобеда! §fВы победили " + loser.getUsername());
            loser.sendMessage("§cПоражение! §fВы проиграли " + winner.getUsername());
        } else if (winner != null) {
            winner.sendMessage("§aПобеда! §fСоперник покинул дуэль");
        }

        if (player1 != null && player1.isOnline()) {
            lobby.teleportToLobby(player1);
        }
        if (player2 != null && player2.isOnline()) {
            lobby.teleportToLobby(player2);
        }

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            cleanup();
            if (onDuelEndCallback != null) {
                onDuelEndCallback.run();
            }
        }).delay(TaskSchedule.tick(40)).schedule();
    }

    public void cleanup() {
        MinecraftServer.getGlobalEventHandler().removeChild(duelEventNode);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isFightStarted() {
        return fightStarted;
    }

    public void sendMessageToAll(String message) {
        if (player1 != null && player1.isOnline()) player1.sendMessage(message);
        if (player2 != null && player2.isOnline()) player2.sendMessage(message);
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }
}