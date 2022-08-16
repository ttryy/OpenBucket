package xyz.ttryy.openwb.utils;

import org.bukkit.ChatColor;
import xyz.ttryy.openwb.BucketPlugin;

public enum Text {

    BUCKET_EMPTY("BUCKET_EMPTY"),
    BUCKET_WATER_AMOUNT("BUCKET_WATER_AMOUNT"),
    BUCKET_LAVA_AMOUNT("BUCKET_LAVA_AMOUNT"),
    DEFAULT_NAME("DEFAULT_NAME"),
    BUCKET_DEACTIVATED("BUCKET_DEACTIVATED"),
    BUCKET_STACKED("BUCKET_STACKED"),
    BUCKET_LIMIT("BUCKET_LIMIT");

    private String text;

    Text(String configName) {
        try {
            text = ChatColor.translateAlternateColorCodes('&', BucketPlugin.getInstance().getConfig().getString("texts." + configName));
        } catch (Exception e){
            text = ChatColor.RED + "Could not load text \"" + configName + "\" from the config.";
            e.printStackTrace();
        }
    }

    public String getText() {
        return text;
    }
}
