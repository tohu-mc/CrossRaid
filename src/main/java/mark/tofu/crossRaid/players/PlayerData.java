package mark.tofu.crossRaid.players;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private double maxHealth;
    private double attack;
    private int level;
    private double exp;
    private long gold;

    public PlayerData(UUID uuid, double maxHealth, double attack, int level, double exp, long gold) {
        this.uuid = uuid;
        this.maxHealth = maxHealth;
        this.attack = attack;
        this.level = level;
        this.exp = exp;
        this.gold = gold;
    }

    // ゲーム内のステータスを実際のプレイヤーに適用するメソッド
    public void applyToPlayer(Player player) {
        // 最大体力の適用
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(this.maxHealth);
        // 体力を全回復（必要に応じて）
        player.setHealth(this.maxHealth);
    }

    // ゲッターとセッター
    public UUID getUuid() { return uuid; }
    public double getMaxHealth() { return maxHealth; }
    public void setMaxHealth(double maxHealth) { this.maxHealth = maxHealth; }
    public double getAttack() { return attack; }
    public void setAttack(double attack) { this.attack = attack; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getGold() { return gold; }
    public void setGold(long gold) { this.gold = gold; }
    public void addGold(long gold) {
        this.gold = this.gold + gold;
    }
    public void addExp(double exp) {
        this.exp = this.exp + exp;
    }

}
