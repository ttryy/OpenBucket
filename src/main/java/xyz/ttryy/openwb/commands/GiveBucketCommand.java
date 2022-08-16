package xyz.ttryy.openwb.commands;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.ttryy.openwb.BucketPlugin;
import xyz.ttryy.openwb.utils.Text;

import java.util.List;
import java.util.UUID;

public class GiveBucketCommand implements CommandExecutor {

    private BucketPlugin plugin;

    public GiveBucketCommand(BucketPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginCommand("openbucket").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)){
            sender.sendMessage(ChatColor.RED + "You're only allowed to execute this command as a player.");
            return false;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("openbucket.admin.give")){
            player.sendMessage(ChatColor.RED + "You don't have permission.");
            return false;
        }

        ItemStack bucket = new ItemStack(Material.BUCKET, 1);
        bucket.addUnsafeEnchantment(Enchantment.DURABILITY, 1);

        ItemMeta meta = bucket.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(Text.DEFAULT_NAME.getText());

        List<String> loreList = Lists.newArrayList();
        loreList.add(Text.BUCKET_EMPTY.getText());
        meta.setLore(loreList);

        meta.getPersistentDataContainer().set(plugin.getWaterCountKey(), PersistentDataType.INTEGER, 0);
        meta.getPersistentDataContainer().set(plugin.getLavaCountKey(), PersistentDataType.INTEGER, 0);

        bucket.setItemMeta(meta);

        player.getInventory().addItem(bucket);
        player.sendMessage(ChatColor.GREEN + "You received an OpenBucket.");

        return true;
    }
}
