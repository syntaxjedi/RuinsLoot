package net.syntaxjedi.ruinsloot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.metadata.FixedMetadataValue;

public class BlockEvent implements Listener{
	
	private static final Logger log = Logger.getLogger("Minecraft");
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent e){
		Player p = (Player)e.getPlayer();
		World world = p.getWorld();
		ItemStack is = p.getInventory().getItemInMainHand();
		ItemMeta meta = is.getItemMeta();
		if(!is.hasItemMeta() || !is.getItemMeta().hasLore()){
			return;
		}
		if(is.getType() == Material.CHEST && meta.getLore().contains("common") || meta.getLore().contains("uncommon") || meta.getLore().contains("legendary")){
			
			int x = e.getBlock().getLocation().getBlockX();
			int y = e.getBlock().getLocation().getBlockY();
			int z = e.getBlock().getLocation().getBlockZ();
			log.info("Block Placed: " + x + " " + y + " " + z + " " + world);
			FileHandler.addLoc(x, y, z, meta.getLore().get(0), world.getName());
		}
	}
	
	@EventHandler
	public void onBlockBreakEvent(BlockBreakEvent e) throws IOException{
		Player p = (Player)e.getPlayer();
		Location loc = new Location(p.getWorld(), 0, 0, 0);
		if(e.getBlock().getType() == Material.CHEST){
			Map<Integer, ArrayList<Object>> cLoc = FileHandler.getLoc();
			for(int i = 0; i < cLoc.size(); i++){
				loc.setX((double) cLoc.get(i).get(2));
				loc.setY((double) cLoc.get(i).get(3));
				loc.setZ((double) cLoc.get(i).get(4));
				if(p.hasPermission("ruinsloot.chest") && e.getBlock().getLocation().equals(loc)){
					e.setCancelled(true);
					FileHandler.removeLoc(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ(), e.getBlock().getWorld().getName());
					e.getBlock().getDrops().clear();
					e.getBlock().setType(Material.AIR);
					p.sendMessage("Broke The Chest!");
				}else if(!p.hasPermission("ruinsloot.chest") && e.getBlock().getLocation().equals(loc)){
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You don't have permission to do that!");
				}
			}
		}
	}
}
