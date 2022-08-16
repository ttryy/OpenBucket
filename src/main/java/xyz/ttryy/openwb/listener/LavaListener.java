package xyz.ttryy.openwb.listener;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.ttryy.openwb.BucketPlugin;
import xyz.ttryy.openwb.utils.Text;

import java.util.List;

public class LavaListener implements Listener {
    private BucketPlugin plugin;

    public LavaListener(BucketPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void interactBlock(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_AIR){
            return;
        }

        if(event.getItem() == null || event.getItem().getType() != Material.LAVA_BUCKET) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(item.getType() == Material.LAVA_BUCKET){
            if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)){
                int lavaCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getLavaCountKey(), PersistentDataType.INTEGER);

                if(!plugin.isLavaBucket(player)){
                    if(lavaCount > 0){
                        player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                        changeLavaAmount(item, 0, player);
                    }
                    event.setCancelled(true);
                    return;
                }

                Block block = player.getTargetBlockExact(4, FluidCollisionMode.SOURCE_ONLY);
                if(block != null && block.getType() == Material.LAVA
                        && (block.getBlockData() instanceof Levelled && ((Levelled)block.getBlockData()).getLevel() == 0)){
                    item.setType(Material.BUCKET);
                    player.playSound(block.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1, 1);
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        if(event.getBucket() != Material.LAVA_BUCKET){
            return;
        }
        if(event.isCancelled()){
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.LAVA_BUCKET) item = player.getInventory().getItemInOffHand();
        if(item.getType() == Material.LAVA_BUCKET){
            if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)){
                int lavaCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getLavaCountKey(), PersistentDataType.INTEGER);

                if(!plugin.isLavaBucket(player)){
                    if(lavaCount > 0){
                        player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                        changeLavaAmount(item, 0, player);
                    }
                    event.setCancelled(true);
                    return;
                }

                if(item.getAmount() > 1){
                    player.sendMessage(Text.BUCKET_STACKED.getText());
                    event.setCancelled(true);
                }

                Block block  = event.getBlockClicked().getRelative(event.getBlockFace());
                if(block.getType() == Material.LAVA
                        && (block.getBlockData() instanceof Levelled && ((Levelled)block.getBlockData()).getLevel() == 0)){
                    changeLavaAmount(item, ++lavaCount, player);
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    return;
                }

                if(lavaCount > 0){
                    changeLavaAmount(item, --lavaCount, player);
                    event.setItemStack(item);
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketFill(PlayerBucketFillEvent event){
        if(event.getBucket() != Material.BUCKET){
            return;
        }
        if(event.getBlockClicked().getType() != Material.LAVA){
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.BUCKET) item = player.getInventory().getItemInOffHand();
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)){
            int lavaCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getLavaCountKey(), PersistentDataType.INTEGER);

            if(!plugin.isLavaBucket(player)){
                if(lavaCount > 0){
                    player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                    changeLavaAmount(item, 0, player);
                }
                event.setCancelled(true);
                return;
            }

            if(item.getAmount() > 1){
                player.sendMessage(Text.BUCKET_STACKED.getText());
                event.setCancelled(true);
            }

            if(event.isCancelled()){
                changeLavaAmount(item, lavaCount, player);
            } else {
                changeLavaAmount(item, ++lavaCount, player);
                event.getBlockClicked().setType(Material.AIR);
                event.setCancelled(true);
            }
            event.setItemStack(item);
        }
    }

    @EventHandler
    public void onCauldron(CauldronLevelChangeEvent event){
        if(event.getReason() == CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL || event.getReason() == CauldronLevelChangeEvent.ChangeReason.BUCKET_EMPTY){
            Player player = (Player)event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();
            if(event.getBlock().getType() == Material.LAVA_CAULDRON){
                if(item.getType() != Material.BUCKET && item.getType() != Material.LAVA_BUCKET) item = player.getInventory().getItemInOffHand();
                    if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)){
                        int lavaCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getLavaCountKey(), PersistentDataType.INTEGER);

                        if(!plugin.isLavaBucket(player)){
                            if(lavaCount > 0){
                                player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                                changeLavaAmount(item, 0, player);
                            }
                            event.setCancelled(true);
                            return;
                        }

                        if(item.getAmount() > 1){
                            player.sendMessage(Text.BUCKET_STACKED.getText());
                            event.setCancelled(true);
                        }

                        if(event.isCancelled()){
                            changeLavaAmount(item, lavaCount, player);
                        } else {
                            changeLavaAmount(item, ++lavaCount, player);
                            event.getBlock().setType(Material.CAULDRON);
                            player.playSound(event.getBlock().getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, SoundCategory.BLOCKS, 1, 1);
                            event.setCancelled(true);
                        }
                    }
            } else if(event.getBlock().getType() != Material.LAVA_CAULDRON) {
                if (item.getType() != Material.BUCKET && item.getType() != Material.LAVA_BUCKET)
                    item = player.getInventory().getItemInOffHand();
                if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)
                        && item.getType() == Material.LAVA_BUCKET) {
                    int lavaCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getLavaCountKey(), PersistentDataType.INTEGER);

                    if (!plugin.isLavaBucket(player)) {
                        if (lavaCount > 0) {
                            player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                            changeLavaAmount(item, 0, player);
                        }
                        event.setCancelled(true);
                        return;
                    }

                    if (item.getAmount() > 1) {
                        player.sendMessage(Text.BUCKET_STACKED.getText());
                        event.setCancelled(true);
                    }

                    if (lavaCount > 0) {
                        changeLavaAmount(item, --lavaCount, player);
                        event.getBlock().setType(Material.LAVA_CAULDRON);
                        player.playSound(event.getBlock().getLocation(), Sound.ITEM_BUCKET_EMPTY_LAVA, SoundCategory.BLOCKS, 1, 1);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFurnaceBurn(FurnaceBurnEvent event){
        if(event.getFuel().getType() == Material.LAVA_BUCKET){
            ItemStack item = event.getFuel();
            if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER)){
                event.setCancelled(true);
            }
        }
    }

    private void changeLavaAmount(ItemStack lavaBucket, int lavaCount, Player player){
        int limit = Integer.MAX_VALUE;
        if(plugin.getLavaBucketLimit() > 0) limit = plugin.getLavaBucketLimit() + 1;

        if(lavaCount >= limit){
            lavaCount -= 1;
            player.sendMessage(Text.BUCKET_LIMIT.getText());
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, SoundCategory.BLOCKS, (float)1, (float)0);
        }

        List<String> loreList = Lists.newArrayList();

        if(lavaCount == 0){
            loreList.add(Text.BUCKET_EMPTY.getText());
            lavaBucket.setType(Material.BUCKET);
        } else {
            loreList.add(Text.BUCKET_LAVA_AMOUNT.getText().replace("%", String.valueOf(lavaCount)));
            lavaBucket.setType(Material.LAVA_BUCKET);
        }

        ItemMeta meta = lavaBucket.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.getLavaCountKey(), PersistentDataType.INTEGER, lavaCount);
        meta.setLore(loreList);
        lavaBucket.setItemMeta(meta);
    }

}

