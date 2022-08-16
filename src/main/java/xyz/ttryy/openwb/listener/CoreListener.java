package xyz.ttryy.openwb.listener;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.ttryy.openwb.BucketPlugin;

public class CoreListener implements Listener {

    private BucketPlugin plugin;

    public CoreListener(BucketPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event){
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK){
            if(event.getItem() != null && event.getItem().getType() == Material.BUCKET){
                if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.POWDER_SNOW){
                    event.setCancelled(isItemOpenBucket(event.getItem()));
                }
            }
        }
    }

    @EventHandler
    public void interactEntity(PlayerInteractEntityEvent event){
        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        if(item.getType() == Material.BUCKET &&
                (event.getRightClicked().getType() == EntityType.COW || event.getRightClicked().getType() == EntityType.MUSHROOM_COW
                || event.getRightClicked().getType() == EntityType.GOAT)){
            event.setCancelled(isItemOpenBucket(item));
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent event){
        if(event.getItem().getType() == Material.WATER_BUCKET || event.getItem().getType() == Material.LAVA_BUCKET || event.getItem().getType() == Material.BUCKET){
            ItemStack item = event.getItem();
            event.setCancelled(isItemOpenBucket(item));
        }
    }

    @EventHandler
    public void onCatch(PlayerBucketEntityEvent event){
        ItemStack item = event.getOriginalBucket();
        event.setCancelled(isItemOpenBucket(item));
    }

    @EventHandler
    public void onCauldron(CauldronLevelChangeEvent event){
        if(event.getReason() == CauldronLevelChangeEvent.ChangeReason.BUCKET_FILL){
            Player player = (Player)event.getEntity();
            ItemStack item = player.getInventory().getItemInMainHand();
            if(item.getType() != Material.BUCKET && item.getType() != Material.WATER_BUCKET && item.getType() != Material.LAVA_BUCKET){
                item = player.getInventory().getItemInOffHand();
            }

            if(event.getBlock().getType() == Material.POWDER_SNOW_CAULDRON){
                event.setCancelled(isItemOpenBucket(item));
            }
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event){
        if(event.getResult().getType() == Material.SPONGE){
            Furnace furnace = ((Furnace)event.getBlock().getState());
            ItemStack item = furnace.getInventory().getFuel();
            if(item.getType() == Material.BUCKET){
                if(isItemOpenBucket(item)){
                    event.setCancelled(true);
                    furnace.getInventory().getSmelting().setAmount(furnace.getInventory().getSmelting().getAmount()-1);
                    if(furnace.getInventory().getResult() != null){
                        furnace.getInventory().getResult().setAmount(furnace.getInventory().getResult().getAmount()+1);
                    } else furnace.getInventory().setResult(new ItemStack(Material.SPONGE));
                }
            }
        }
    }

    private boolean isItemOpenBucket(ItemStack item){
        return item.hasItemMeta() && (item.getItemMeta().getPersistentDataContainer().has(plugin.getWaterCountKey(), PersistentDataType.INTEGER) || item.getItemMeta().getPersistentDataContainer().has(plugin.getLavaCountKey(), PersistentDataType.INTEGER));
    }

}
