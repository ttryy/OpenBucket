package xyz.ttryy.openwb;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.ttryy.openwb.commands.GiveBucketCommand;
import xyz.ttryy.openwb.listener.CoreListener;
import xyz.ttryy.openwb.listener.LavaListener;
import xyz.ttryy.openwb.listener.WaterListener;

public class BucketPlugin extends JavaPlugin {

    private static BucketPlugin instance;

    private boolean waterBucket = true;
    private boolean lavaBucket = true;
    private int waterBucketLimit = -1;
    private int lavaBucketLimit = -1;

    private NamespacedKey waterCountKey;
    private NamespacedKey lavaCountKey;

    @Override
    public void onEnable() {
        instance = this;

        waterCountKey = new NamespacedKey(this, "waterCount");
        lavaCountKey = new NamespacedKey(this, "lavaCount");

        saveDefaultConfig();
        loadConfig();

        initCommands();
        initListener();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private void loadConfig(){
        lavaBucket = getConfig().getBoolean("bucket.lavabucket", true);
        waterBucket = getConfig().getBoolean("bucket.waterbucket", true);
        waterBucketLimit = getConfig().getInt("bucket.waterbucket_limit", -1);
        lavaBucketLimit = getConfig().getInt("bucket.lavabucket_limit", -1);
    }

    private void initCommands(){
        new GiveBucketCommand(this);
    }

    private void initListener(){
        new WaterListener(this);
        new LavaListener(this);
        new CoreListener(this);
    }

    public static BucketPlugin getInstance() {
        return instance;
    }

    public NamespacedKey getWaterCountKey() {
        return waterCountKey;
    }

    public NamespacedKey getLavaCountKey() {
        return lavaCountKey;
    }

    public boolean isWaterBucket(Player player) {
        return player.hasPermission("openbucket.use.waterbucket") && waterBucket;
    }

    public boolean isLavaBucket(Player player) {
        return player.hasPermission("openbucket.use.lavabucket") && lavaBucket;
    }

    public int getWaterBucketLimit() {
        return waterBucketLimit;
    }

    public int getLavaBucketLimit() {
        return lavaBucketLimit;
    }
}
