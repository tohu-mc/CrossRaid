package mark.tofu.crossRaid.bosses;

public class BossData {
    private final String id;
    private final String name;
    private final long dropGold;
    private final int dropExp;

    public BossData(String id, String name, long dropGold, int dropExp) {
        this.id = id;
        this.name = name;
        this.dropGold = dropGold;
        this.dropExp = dropExp;
    }

    // ゲッター
    public String getId() { return id; }
    public String getName() { return name; }
    public long getDropGold() { return dropGold; }
    public int getDropExp() { return dropExp; }
}
