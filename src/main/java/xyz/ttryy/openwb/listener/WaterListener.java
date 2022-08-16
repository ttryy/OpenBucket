package xyz.ttryy.openwb.listener;

import com.google.common.collect.Lists;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.ttryy.openwb.BucketPlugin;
import xyz.ttryy.openwb.utils.Text;

import java.util.List;

public class WaterListener implements Listener {

    private BucketPlugin plugin;

    public WaterListener(BucketPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void interactBlock(PlayerInteractEvent event){
        if(event.getAction() != Action.RIGHT_CLICK_AIR){
            return;
        }

        if(event.getItem() == null || event.getItem().getType() != Material.WATER_BUCKET){
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if(item.getType() == Material.WATER_BUCKET){
            if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER)){
                int waterCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getWaterCountKey(), PersistentDataType.INTEGER);

                if(!plugin.isWaterBucket(player)){
                    if(waterCount > 0){
                        player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                        changeWaterAmount(item, 0, player);
                    }
                    event.setCancelled(true);
                    return;
                }

                Block block = player.getTargetBlockExact(4, FluidCollisionMode.SOURCE_ONLY);
                if(block != null && block.getType() == Material.WATER
                        && (block.getBlockData() instanceof Levelled && ((Levelled)block.getBlockData()).getLevel() == 0)){
                    item.setType(Material.BUCKET);
                    player.playSound(block.getLocation(), Sound.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                    event.setCancelled(false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event){
        if(event.getBucket() != Material.WATER_BUCKET){
            return;
        }
        if(event.isCancelled()){
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.WATER_BUCKET) item = player.getInventory().getItemInOffHand();
        if(item.getType() == Material.WATER_BUCKET){
            if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER)){
                int waterCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getWaterCountKey(), PersistentDataType.INTEGER);

                if(!plugin.isWaterBucket(player)){
                    if(waterCount > 0){
                        player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                        changeWaterAmount(item, 0, player);
                    }
                    event.setCancelled(true);
                    return;
                }

                if(item.getAmount() > 1){
                    player.sendMessage(Text.BUCKET_STACKED.getText());
                    event.setCancelled(true);
                }

                Block block = event.getBlockClicked();
                if (block.getBlockData() instanceof Waterlogged && ((Waterlogged)block.getBlockData()).isWaterlogged() && (!player.isSneaking())){
                    changeWaterAmount(item, ++waterCount, player);
                    event.setCancelled(true);
                    Waterlogged waterlogged = (Waterlogged) block.getBlockData();
                    waterlogged.setWaterlogged(false);
                    BlockState state = block.getState();
                    state.setBlockData(waterlogged);
                    state.update(true, true);
                    return;
                }

                block = event.getBlockClicked().getRelative(event.getBlockFace());
                if(block.getType() == Material.WATER
                        && (block.getBlockData() instanceof Levelled && ((Levelled)block.getBlockData()).getLevel() == 0)){
                    changeWaterAmount(item, ++waterCount, player);
                    event.setCancelled(true);
                    block.setType(Material.AIR);
                    return;
                }

                if(waterCount > 0){
                    changeWaterAmount(item, --waterCount, player);
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

        Block block = event.getBlockClicked();
        boolean isWaterlogged = (block.getBlockData() instanceof Waterlogged && ((Waterlogged)block.getBlockData()).isWaterlogged());
        if(block.getType() != Material.WATER
            && !isWaterlogged){
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if(item.getType() != Material.BUCKET) item = player.getInventory().getItemInOffHand();
        if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER)){
            int waterCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getWaterCountKey(), PersistentDataType.INTEGER);

            if(!plugin.isWaterBucket(player)){
                if(waterCount > 0){
                    player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                    changeWaterAmount(item, 0, player);
                }
                event.setCancelled(true);
                return;
            }

            if(item.getAmount() > 1){
                player.sendMessage(Text.BUCKET_STACKED.getText());
                event.setCancelled(true);
            }

            if(event.isCancelled()){
                changeWaterAmount(item, waterCount, player);
            } else {
                changeWaterAmount(item, ++waterCount, player);
                if(isWaterlogged){
                    Waterlogged waterlogged = (Waterlogged) block.getBlockData();
                    waterlogged.setWaterlogged(false);
                    BlockState state = block.getState();
                    state.setBlockData(waterlogged);
                    state.update(true, true);
                } else
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
            if(event.getBlock().getType() == Material.WATER_CAULDRON){
                if(item.getType() != Material.BUCKET && item.getType() != Material.WATER_BUCKET) item = player.getInventory().getItemInOffHand();
                if(item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER)){
                    int waterCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getWaterCountKey(), PersistentDataType.INTEGER);

                    if(!plugin.isWaterBucket(player)){
                        if(waterCount > 0){
                            player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                            changeWaterAmount(item, 0, player);
                        }
                        event.setCancelled(true);
                        return;
                    }

                    if(item.getAmount() > 1){
                        player.sendMessage(Text.BUCKET_STACKED.getText());
                        event.setCancelled(true);
                    }

                    if(event.isCancelled()){
                        changeWaterAmount(item, waterCount, player);
                    } else {
                        changeWaterAmount(item, ++waterCount, player);
                        event.getBlock().setType(Material.CAULDRON);
                        player.playSound(event.getBlock().getLocation(), Sound.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1, 1);
                        event.setCancelled(true);
                    }
                }
            } else if(event.getBlock().getType() != Material.WATER_CAULDRON) {
                if (item.getType() != Material.BUCKET && item.getType() != Material.WATER_BUCKET)
                    item = player.getInventory().getItemInOffHand();
                if (item.hasItemMeta() && item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER)
                        && item.getType() == Material.WATER_BUCKET) {
                    int waterCount = item.getItemMeta().getPersistentDataContainer().get(plugin.getWaterCountKey(), PersistentDataType.INTEGER);

                    if (!plugin.isWaterBucket(player)) {
                        if (waterCount > 0) {
                            player.sendMessage(Text.BUCKET_DEACTIVATED.getText());
                            changeWaterAmount(item, 0, player);
                        }
                        event.setCancelled(true);
                        return;
                    }

                    if (item.getAmount() > 1) {
                        player.sendMessage(Text.BUCKET_STACKED.getText());
                        event.setCancelled(true);
                    }

                    if (waterCount > 0) {
                        changeWaterAmount(item, --waterCount, player);
                        event.getBlock().setType(Material.WATER_CAULDRON);
                        Levelled level = (Levelled) event.getBlock().getBlockData();
                        level.setLevel(3);
                        event.getBlock().setBlockData(level);
                        player.playSound(event.getBlock().getLocation(), Sound.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1, 1);
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private void changeWaterAmount(ItemStack waterBucket, int waterCount, Player player){
        int limit = Integer.MAX_VALUE;
        if(plugin.getWaterBucketLimit() > 0) limit = plugin.getWaterBucketLimit() + 1;

        if(waterCount >= limit){
            waterCount -= 1;
            player.sendMessage(Text.BUCKET_LIMIT.getText());
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_FALL, SoundCategory.BLOCKS, (float)1, (float)0);
        }

        List<String> loreList = Lists.newArrayList();

        if(waterCount == 0){
            loreList.add(Text.BUCKET_EMPTY.getText());
            waterBucket.setType(Material.BUCKET);
        } else {
            loreList.add(Text.BUCKET_WATER_AMOUNT.getText().replace("%", String.valueOf(waterCount)));
            waterBucket.setType(Material.WATER_BUCKET);
        }

        ItemMeta meta = waterBucket.getItemMeta();
        meta.getPersistentDataContainer().set(plugin.getWaterCountKey(), PersistentDataType.INTEGER, waterCount);
        meta.setLore(loreList);
        waterBucket.setItemMeta(meta);
    }

}
