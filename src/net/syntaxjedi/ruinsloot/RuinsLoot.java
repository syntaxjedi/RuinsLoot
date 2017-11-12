package net.syntaxjedi.ruinsloot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RuinsLoot extends JavaPlugin implements Listener {
	
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Inventory inv;
	public static Block bl;
	public int time;
	public Long timel;
	public boolean waitRef = false;
	@Override
	public void onEnable(){
		//loadConfiguration();
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		this.getServer().getPluginManager().registerEvents(this, this);
		
		time = getConfig().getInt("time.ticks");
		timel = (long)time;
		 BukkitScheduler scheduler = getServer().getScheduler();
	        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
	            @Override
	            public void run() {
	                waitRef = true;
	                findChest();
	                log.info("waited");
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
		if(command.getLabel().equalsIgnoreCase("lootchest")){
			
			if(args.length > 0 && args.length < 2){
				if(args[0].equalsIgnoreCase("common") || args[0].equalsIgnoreCase("uncommon") || args[0].equalsIgnoreCase("legendary")){
					int emptySlot = p.getInventory().firstEmpty();
					String cap = args[0].substring(0, 1).toUpperCase() + args[0].substring(1).toLowerCase();
					String low = args[0].toLowerCase();
					p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, cap + " LootChest", low));
					p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + cap + " LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
					return true;
				}
			}else if(args.length > 0 && args.length < 2 && args[0].equalsIgnoreCase("fill")){
				
				p.sendMessage(ChatColor.GOLD + "Filling Chests");
				waitRef = true;
				findChest();
				return true;
				
			}else if(args.length > 1 && args.length < 3 && args[0].equalsIgnoreCase("common") || args[0].equalsIgnoreCase("uncommon") || args[0].equalsIgnoreCase("legendary")){
				
				int emptySlot = p.getInventory().firstEmpty();
				String low = args[0].toLowerCase();
				p.getInventory().setItem(emptySlot, createItem(Material.CHEST, 1, args[1], low));
				p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + low +" LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
				return true;
				
			}else if(args.length > 0 && args.length < 3 && !args[0].equalsIgnoreCase("common") || !args[0].equalsIgnoreCase("uncommon") || !args[0].equalsIgnoreCase("legendary")){
				
				p.sendMessage(ChatColor.RED + "Invalid chest type.");
				return false;
				
			}else if(args.length > 3){
				
				p.sendMessage(ChatColor.RED + "Too many arguments.");
				return false;
				
			}else if(args.equals(null)){
				
				p.sendMessage(ChatColor.BLUE + "============================================" + 
						ChatColor.GOLD + "\nCurrent Version: " + ChatColor.LIGHT_PURPLE + getDescription().getVersion() + ChatColor.GOLD + "\nAuthor: " 
						+ ChatColor.GREEN + getDescription().getAuthors() + ChatColor.GOLD + "\nCommands:" +
						"\n- /lootchest <common | uncommon | legendary> :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary" +
						ChatColor.GOLD + "\n- /lootchest <common | uncommon | legendary> [name] :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary, with a custom name" 
						+ ChatColor.RED + " (not yet fillable)" + 
						ChatColor.GOLD + "\n- /lootchest fill :" + ChatColor.WHITE + " Fills all lootchests with their respective items from the config.");
					return true;
			}
			else{
				p.sendMessage(ChatColor.RED + "You must append the proper arguments");
				return false;
			}
		}
		else{
			return false;
		}
		return false;
	}
	
	@EventHandler
	public void onBlockPlaceEvent(BlockPlaceEvent e){
		Player p = (Player)e.getPlayer();
		World world = p.getWorld();
		ItemStack is = p.getInventory().getItemInMainHand();
		ItemMeta meta = is.getItemMeta();
		if(is.getType() == Material.CHEST && meta.getLore().contains("common") || meta.getLore().contains("uncommon") || meta.getLore().contains("legendary")){
			//e.setCancelled(true);
			Block loc = e.getBlockAgainst();
			Block lc = loc.getLocation().add(0, +1, 0 ).getBlock();
			lc.setType(Material.CHEST);
			lc.setMetadata(meta.getLore().get(0), new FixedMetadataValue(this, "lootChest"));
		}
	}
	
	@EventHandler
	public void onChunkLoadEvent(ChunkLoadEvent e){
		if(!e.isNewChunk()){
			findChest();
		}
	}
	
	public void findChest(){
		World world = Bukkit.getWorld("world");
		if(waitRef){
			for(Chunk c : world.getLoadedChunks()){
				for(BlockState b : c.getTileEntities()){
					if(b instanceof Chest){
						inv = ((Chest) b).getBlockInventory();
						//bl = (Block) b;
						
						if(b.hasMetadata("common")){
							inv.clear();
							ItemStack[] stacks = getLoot("common");
							for(int i = 0; i < stacks.length; i++){
								inv.setItem(i, stacks[i]);
							}
						}
						else if(b.hasMetadata("uncommon")){
							log.info("UnCommon!");
							inv.clear();
							ItemStack[] stacks = getLoot("uncommon");
							for(int i = 0; i < stacks.length; i++){
								inv.setItem(i, stacks[i]);
							}
						}
						else if(b.hasMetadata("legendary")){
							inv.clear();
							ItemStack[] stacks = getLoot("legendary");
							for(int i = 0; i < stacks.length; i++){
								inv.setItem(i, stacks[i]);
							}
						}
					}
					else{
						continue;
					}
				}
			}
		}else if(!waitRef){
			return;
		}
		waitRef = false;
		log.info("Filled Chests");
	}
	
	public ItemStack[] getLoot(String chest){
		ArrayList<?> stack = (ArrayList<?>) this.getConfig().getList("chests." + chest);
		ItemStack[] stacks = stack.toArray(new ItemStack[stack.size()]);
		return stacks;
	}
	
	public static ItemStack createItem(Material material, int amount, String name, String type){
		ItemStack item = new ItemStack(material, amount);
		ItemMeta meta = item.getItemMeta();
		if(name != null) meta.setDisplayName(name);
		if(type != null){
			List<String> chestType = new ArrayList<String>();
			chestType.add(type);
			meta.setLore(chestType);
		}
		item.setItemMeta(meta);
		return item;
	}
	
}
