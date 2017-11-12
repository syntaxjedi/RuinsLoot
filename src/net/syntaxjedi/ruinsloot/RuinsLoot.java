package net.syntaxjedi.ruinsloot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.plugin.RegisteredServiceProvider;

public class RuinsLoot extends JavaPlugin {
	
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Inventory inv;
	public int time;
	public Long timel;
	@Override
	public void onEnable(){
		//loadConfiguration();
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		time = getConfig().getInt("time.ticks");
		timel = (long)time;
		 BukkitScheduler scheduler = getServer().getScheduler();
	        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
	            @Override
	            public void run() {
	                findChest();
	            }
	        }, 0L, timel);
	}
	
	@Override
	public void onDisable(){
		loadConfiguration(new File(getDataFolder(), "config.yml"), "config.yml");
	}
	
	public void loadConfiguration(File filepath, String conf){
		File configFile = filepath;
		FileConfiguration lootConf = YamlConfiguration.loadConfiguration(configFile);
		
		if(!configFile.exists()){
			log.info("No Default found, creating \"" + conf + "\"");
			
			InputStream configStream = getResource(conf);
			
			if(configStream != null){
				YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(configStream));
				lootConf.setDefaults(defConfig);
				
				try{
					lootConf.save(configFile);
					log.info("Default config file \"" + conf + "\" written.");
				}catch (IOException ex){
					log.severe("Could not write config file: " + conf);
				}
			}else{
				log.warning("Could not find default \"" + conf + "\" file.");
			}
		}
	}
	
	
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		
		Player p = (Player) sender;
		if(sender != p){
			log.info("This command can only be run by players currently");
			return false;
		}
		if(command.getLabel().equalsIgnoreCase("lootchest") && sender == p){
			

			if(args.length > 0 && args.length < 2){
				if(args[0].equalsIgnoreCase("common")){
					
					int emptySlot = p.getInventory().firstEmpty();
					p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, "Common LootChest"));
					p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "Common LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
					return true;
					
				}
				
				if(args[0].equalsIgnoreCase("uncommon")){
					
					int emptySlot = p.getInventory().firstEmpty();
					p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, "Uncommon LootChest"));
					p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.BLUE + "Common LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
					return true;
					
				}
				
				if(args[0].equalsIgnoreCase("legendary")){
					
					int emptySlot = p.getInventory().firstEmpty();
					p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, "Legendary LootChest"));
					p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.LIGHT_PURPLE + "Common LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
					return true;
					
				}
				
				if(args[0].equalsIgnoreCase("fill")){
					
					findChest();
					p.sendMessage(ChatColor.GOLD + "Filling Chests");
					return true;
					
				}
				
				else{
					p.sendMessage(ChatColor.RED + "You must append the proper arguments.");
					return false;
				}
			}else if(args.length > 1 && args.length < 3){
				int emptySlot = p.getInventory().firstEmpty();
				p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, args[1]));
				return true;
			}
			if(args.length > 3){
				p.sendMessage(ChatColor.RED + "Too many arguments");
			}
			p.sendMessage(ChatColor.BLUE + "============================================" + 
			ChatColor.GOLD + "\nCurrent Version: " + ChatColor.LIGHT_PURPLE + getDescription().getVersion() + ChatColor.GOLD + "\nAuthor: " + ChatColor.GREEN + getDescription().getAuthors() + ChatColor.GOLD + "\nCommands:" +
			"\n- /lootchest <common | uncommon | legendary> :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary" +
			ChatColor.GOLD + "\n- /lootchest <common | uncommon | legendary> [name] :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary, with a custom name" 
			+ ChatColor.RED + " (not yet fillable)" + 
			ChatColor.GOLD + "\n- /lootchest fill :" + ChatColor.WHITE + " Fills all lootchests with their respective items from the config.");
			return true;
		}
		else{
			return false;
		}
	}
	
	public void findChest(){
		log.info("Refilling Chests");
		World world = Bukkit.getWorld("world");
		for(Chunk c : world.getLoadedChunks()){
			for(BlockState b : c.getTileEntities()){
				if(b instanceof Chest){
					inv = ((Chest) b).getBlockInventory();
					if(inv.getName().equalsIgnoreCase("common lootchest")){
						inv.clear();
						ItemStack[] stacks = getLoot("common");
						for(int i = 0; i < stacks.length; i++){
							inv.setItem(i, stacks[i]);
						}
					}
					else if(inv.getName().equalsIgnoreCase("uncommon lootchest")){
						inv.clear();
						ItemStack[] stacks = getLoot("uncommon");
						for(int i = 0; i < stacks.length; i++){
							inv.setItem(i, stacks[i]);
						}
					}
					else if(inv.getName().equalsIgnoreCase("legendary lootchest")){
						inv.clear();
						ItemStack[] stacks = getLoot("legendary");
						for(int i = 0; i < stacks.length; i++){
							inv.setItem(i, stacks[i]);
						}
					}
				}
			}
		}
		log.info("done");
	}
	
	public ItemStack[] getLoot(String chest){
		ArrayList<?> stack = (ArrayList<?>) this.getConfig().getList("chests." + chest);
		ItemStack[] stacks = stack.toArray(new ItemStack[stack.size()]);
		return stacks;
	}
	
	public static ItemStack createItem(Material material, int amount, String name){
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		if(name != null) meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	
}
