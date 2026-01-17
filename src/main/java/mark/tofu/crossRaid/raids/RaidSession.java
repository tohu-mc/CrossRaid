package mark.tofu.crossRaid.raids;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RaidSession {
    // 参加しているメンバー全員のUUID
    private final Set<UUID> members = new HashSet<>();
    private final UUID leaderUuid; // リーダー（管理用）

    private int currentLevel;
    private UUID currentBossUuid;

    public RaidSession(UUID leaderUuid, Set<UUID> initialMembers) {
        this.leaderUuid = leaderUuid;
        this.members.addAll(initialMembers);
        this.currentLevel = 1;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    // 生存している（オンラインの）メンバーを取得するヘルパー
    public Set<Player> getOnlinePlayers() {
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .collect(Collectors.toSet());
    }

    public void removeMember(UUID uuid) {
        members.remove(uuid);
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public int getCurrentLevel() { return currentLevel; }
    public void nextLevel() { this.currentLevel++; }

    public UUID getCurrentBossUuid() { return currentBossUuid; }
    public void setCurrentBossUuid(UUID currentBossUuid) { this.currentBossUuid = currentBossUuid; }
}