package me.gonzalociocca.survivalcreative;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SCListener implements Listener {
    public SurvivalCreative main;
    public Logger logger;
    public SCListener(SurvivalCreative sc){
        main=sc;
        logger=sc.getLogger();
    }

    HashMap<String, Integer> slots = new HashMap();
    HashMap<String, Integer> tries = new HashMap();

    @EventHandler
    public void onCreative(InventoryCreativeEvent event){
        if(event.isCancelled()){
            return;
        }

        if(event.getClickedInventory()==null){
            if(buy(event.getWhoClicked(),event.getCursor())){}
            else{/*
                Integer slot = slots.get(event.getWhoClicked().getName());
                Integer tried = tries.get(event.getWhoClicked().getName());
                if(slot==event.getSlot()){
                    tried++;
                }
                if(tried)*/
                event.getWhoClicked().sendMessage("$$$ Insuficiente, tienes $"+main.getEconomy().getBalance(event.getWhoClicked().getName()));
                event.setCancelled(true);
            }
        }else if(sell(event.getWhoClicked(),event.getCurrentItem())){
            event.setCurrentItem(new ItemStack(Material.AIR));
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onMiddleClick(InventoryClickEvent event) {
        /* Disable creative duping by middle click on non-creative inventory*/
        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE && !event.isRightClick() && !event.isLeftClick() && !event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChange(PlayerInteractEvent event){
        if(event.getPlayer().getGameMode()==GameMode.CREATIVE){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnChange2(EntityDamageByEntityEvent event){
        if(event.getEntity()!=null&&event.getEntity()instanceof Player){
            Player p = (Player)event.getEntity();
            if(p.getGameMode()==GameMode.CREATIVE){
                event.setCancelled(true);
            }
        }
        if(event.getDamager()!=null&&event.getDamager()instanceof Player){
            Player p = (Player)event.getDamager();
            if(p.getGameMode()==GameMode.CREATIVE){
                event.setCancelled(true);
            }
        }
    }

    public boolean buy(HumanEntity ent, ItemStack is){
        if(is.getType()==Material.AIR){
            return false;
        }
        if(main.getEconomy().has(ent.getName(),getCost(is))) {
//            ent.sendMessage("$Buy " + main.getEconomy().getBalance(ent.getName()) + " -$" + getCost(is) + " " + is.getType().name() + " x" + is.getAmount());
            ent.sendMessage("Compraste "+is.getType().name()+" x"+is.getAmount()+" por $"+getCost(is));

            main.getEconomy().withdrawPlayer(ent.getName(), getCost(is));
            return true;
        }
        return false;
    }

    public boolean sell(HumanEntity ent, ItemStack is){
        if(is.getType()==Material.AIR){return false;}
//            ent.sendMessage("$Sell " + main.getEconomy().getBalance(ent.getName()) + " +$" + getCost(is) + " " + is.getType().name() + " x" + is.getAmount());
        ent.sendMessage("Vendiste "+is.getType().name()+" x"+is.getAmount()+" por $"+getCost(is));
            main.getEconomy().depositPlayer(ent.getName(), getCost(is));
            return true;
    }
    public double getCost(ItemStack is){
        double cost = main.getConfig().getDouble("item."+is.getType().name());
        /*for(Enchantment enc : Enchantment.values()){
            for(int a = 0; a < enc.getMaxLevel();a++){
            main.getConfig().set("enchant."+enc.getName()+".L"+a, 100.0D);
            }
        }
        main.saveConfig();*/

        cost+=enchantCost(is.getEnchantments().entrySet());
        if(is.getType()==Material.ENCHANTED_BOOK){
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) is.getItemMeta();
            cost+=enchantCost(esm.getStoredEnchants().entrySet());
        }
        return cost*is.getAmount();
    }

    public double enchantCost(Set<Map.Entry<Enchantment,Integer>> enchants){
        double cost = 0;
        for(Map.Entry<Enchantment,Integer> enc : enchants){
            int val = enc.getValue()-1;
            double add = main.getConfig().getDouble("enchant."+enc.getKey().getName()+".L"+val);

            //main.getServer().broadcastMessage("enchanting "+cost+" add"+add+" enc"+enc.getKey().getName()+" lvl"+enc.getValue());
            cost+=add;
        }
        return cost;
    }


}
