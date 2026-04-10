package me.sosnovka.MinecraftServer.managers;

import me.sosnovka.MinecraftServer.instances.DuelsInstance;
import me.sosnovka.MinecraftServer.instances.LobbyInstance;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.timer.TaskSchedule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DuelsQueueManager {
    private final Queue<Player> queue = new LinkedList<>();
    private final Map<UUID, DuelsInstance> activeDuels = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> playerToDuelId = new ConcurrentHashMap<>();
    private final InstanceManager instanceManager;
    private final LobbyInstance lobby;
    private int duelCounter = 0;

    public DuelsQueueManager(InstanceManager instanceManager, LobbyInstance lobby) {
        this.instanceManager = instanceManager;
        this.lobby = lobby;
        startQueueChecker();
    }

    public void joinQueue(Player player) {
        if (playerToDuelId.containsKey(player.getUuid())) {
            player.sendMessage("§cВы уже в дуэли!");
            return;
        }

        if (queue.contains(player)) {
            player.sendMessage("§cВы уже в очереди!");
            return;
        }

        queue.add(player);
        player.sendMessage("§aВы в очереди на дуэль...");
        checkQueue();
    }

    public void leaveQueue(Player player) {
        queue.remove(player);
        player.sendMessage("§cВы покинули очередь");
    }

    private void checkQueue() {
        if (queue.size() >= 2) {
            Player p1 = queue.poll();
            Player p2 = queue.poll();

            if (p1 != null && p2 != null && p1.isOnline() && p2.isOnline()) {
                createDuel(p1, p2);
            } else {
                if (p1 != null && p1.isOnline()) queue.add(p1);
                if (p2 != null && p2.isOnline()) queue.add(p2);
            }
        }
    }

    private void createDuel(Player p1, Player p2) {
        duelCounter++;
        String duelId = "duel_" + duelCounter;

        DuelsInstance duel = new DuelsInstance(instanceManager, duelId, lobby);

        duel.setOnDuelEndCallback(() -> {
            UUID duelUuid = null;
            for (Map.Entry<UUID, DuelsInstance> entry : activeDuels.entrySet()) {
                if (entry.getValue() == duel) {
                    duelUuid = entry.getKey();
                    break;
                }
            }
            if (duelUuid != null) {
                activeDuels.remove(duelUuid);
                playerToDuelId.remove(p1.getUuid());
                playerToDuelId.remove(p2.getUuid());
            }
            checkQueue();
        });

        UUID duelUuid = UUID.randomUUID();
        activeDuels.put(duelUuid, duel);
        playerToDuelId.put(p1.getUuid(), duelUuid);
        playerToDuelId.put(p2.getUuid(), duelUuid);

        duel.teleportPlayers(p1, p2);

        p1.sendMessage("§aДуэль найдена! Соперник: " + p2.getUsername());
        p2.sendMessage("§aДуэль найдена! Соперник: " + p1.getUsername());

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (duel.isActive()) {
                duel.sendMessageToAll("§cДуэль завершена по таймауту!");
                duel.endDuel(null, null);
            }
        }).delay(TaskSchedule.minutes(5)).schedule();
    }

    public void endDuel(UUID duelId, Player winner, Player loser) {
        DuelsInstance duel = activeDuels.get(duelId);
        if (duel != null) {
            duel.endDuel(winner, loser);
        }
    }

    public boolean isInDuel(Player player) {
        return playerToDuelId.containsKey(player.getUuid());
    }

    public boolean isInQueue(Player player) {
        return queue.contains(player);
    }

    private void startQueueChecker() {
        MinecraftServer.getSchedulerManager()
                .buildTask(this::checkQueue)
                .repeat(TaskSchedule.tick(20))
                .schedule();
    }
}