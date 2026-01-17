package mark.tofu.crossRaid.raids;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 1つのレイド挑戦（パーティ）の状態を管理するクラス
 */
public class RaidSession {
    private final Set<UUID> members; // 参加メンバー
    private int currentStageIndex;   // 現在の階層 (1, 2, ...)
    private UUID currentBossUuid;    // 戦闘中のボスEntityのUUID

    public RaidSession(Set<UUID> members) {
        this.members = new HashSet<>(members);
        this.currentStageIndex = 1;
    }

    // メンバー管理
    public Set<UUID> getMemberIds() { return members; }

    public Set<Player> getOnlinePlayers() {
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.isOnline())
                .collect(Collectors.toSet());
    }

    public void removeMember(UUID uuid) { members.remove(uuid); }
    public boolean isEmpty() { return members.isEmpty(); }

    // 進行状況管理
    public int getCurrentStageIndex() { return currentStageIndex; }
    public void advanceStage() { this.currentStageIndex++; }

    // ボス管理
    public UUID getCurrentBossUuid() { return currentBossUuid; }
    public void setCurrentBossUuid(UUID uuid) { this.currentBossUuid = uuid; }
}