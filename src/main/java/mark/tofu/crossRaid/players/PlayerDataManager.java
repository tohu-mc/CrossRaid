package mark.tofu.crossRaid.players;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mark.tofu.crossRaid.CrossRaid;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final CrossRaid plugin;
    private final Gson gson;
    private final File folder;

    public final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerDataManager(CrossRaid plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.folder = new File(plugin.getDataFolder(),"playerdata");

        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public PlayerData getData(UUID uuid) {
        return cache.get(uuid);
    }

    public void load(UUID uuid, String name) {
        File file = new File(folder, uuid.toString() + ".json");

        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                PlayerData data = gson.fromJson(reader, PlayerData.class);
                cache.put(uuid, data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 新規プレイヤーならデータ作成
            PlayerData newData = new PlayerData(uuid, 20.0,1.0,1,0,0);
            cache.put(uuid, newData);
            save(uuid); // 即保存
        }
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;

        File file = new File(folder, uuid.toString() + ".json");

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // メモリ解放 (ログアウト時)
    public void unload(UUID uuid) {
        cache.remove(uuid);
    }

    public boolean hasData(UUID uuid) {
        File file = new File(folder, uuid.toString() + ".json");
        return file.exists();
    }
}
