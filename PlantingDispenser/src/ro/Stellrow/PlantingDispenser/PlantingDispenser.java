package ro.Stellrow.PlantingDispenser;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PlantingDispenser extends JavaPlugin implements Listener {

    private List<Material> availableSeeds = new ArrayList<>();
    private Integer range;

    public void onEnable(){
        loadConfig();
        range = getConfig().getInt("DispenserConfig.range");
        loadCompatibleItems();
        getServer().getPluginManager().registerEvents(this,this);
    }

    private void loadConfig(){
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadCompatibleItems(){
        for(String s : getConfig().getStringList("Seeds")){
            try{
                availableSeeds.add(Material.valueOf(s));
            }catch (IllegalArgumentException ex){
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED+"PlantingDispenser - Found wrong material name at "+s);
            }
        }
    }


    //Handle dispenser fire
    @EventHandler
    public void onDispense(BlockDispenseEvent event){
        if(event.getBlock().getType()!=Material.DISPENSER){
            return;
        }
        if(availableSeeds.contains(event.getItem().getType())){
            Directional directional = (Directional) event.getBlock().getBlockData();
            BlockFace face = directional.getFacing();
            Block relative = event.getBlock().getRelative(face);
            event.setCancelled(true);
            for(int x = 0;x<range;x++){
                if(relative.getType()!=Material.AIR){
                    relative = relative.getRelative(face);
                    event.setCancelled(true);
                }else {
                    Dispenser dispenser = (Dispenser) event.getBlock().getState();
                    Bukkit.getScheduler().runTaskLater(this,()->{
                    for(ItemStack i : dispenser.getInventory().getContents()){
                        if(i!=null&&i.isSimilar(event.getItem())){
                            i.setAmount(i.getAmount()-1);
                        }
                    }
                    },5);

                    //set
                    try{
                        relative.setType(SeedToBlock.valueOf(event.getItem().getType().toString()).getValue());
                    }catch (IllegalArgumentException ex){

                    }
                    break;
                }

            }
        }

    }

    private enum SeedToBlock{
        WHEAT_SEEDS(Material.WHEAT),
        BEETROOT_SEEDS(Material.BEETROOTS),
        CARROT(Material.CARROTS),
        POTATO(Material.POTATOES),
        NETHER_WART(Material.NETHER_WART)
        ;
        private Material value;

        SeedToBlock(Material value){
            this.value=value;
        }

        public Material getValue() {
            return value;
        }
    }
}
