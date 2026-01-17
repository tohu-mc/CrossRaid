package mark.tofu.crossRaid.bosses;

public class BossData {
    private final String mythicMobId;
    private final String displayName;
    private final long rewardGold;
    private final double rewardExp;

    public BossData(String mythicMobId, String displayName, long rewardGold, double rewardExp) {
        this.mythicMobId = mythicMobId;
        this.displayName = displayName;
        this.rewardGold = rewardGold;
        this.rewardExp = rewardExp;
    }

    public String getMythicMobId() { return mythicMobId; }
    public String getDisplayName() { return displayName; }
    public long getRewardGold() { return rewardGold; }
    public double getRewardExp() { return rewardExp; }
}