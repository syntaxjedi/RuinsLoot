package net.syntaxjedi.ruinsloot;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.block.Chest;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RuinsLoot extends JavaPlugin{
	
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Inventory inv;
	public static Block bl;
	public int chestNumber;
	public int time;
	public Long timel;
	public static boolean waitRef = true;
	@Override
	public void onEnable(){

		this.getServer().getPluginManager().registerEvents(new BlockEvent(), this);
		
		//register commands file
		log.info("[RuinsLoot] Registering Commands");
		Commands commands = new Commands(this);
		this.getCommand("lootchest").setExecutor(commands);
		log.info("[RuinsLoot] Commands Registered, Checking Plugin Files");
		FileHandler.createFile();
		log.info("[RuinsLoot] File Check Successful");
		
		
		time = getConfig().getInt("time.ticks");
		timel = (long)time;
		 BukkitScheduler scheduler = getServer().getScheduler();
	        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
	            @Override
	            public void run() {
	                waitRef = true;
	                findChest();
	            }
	        }, 0L, timel);
	}
	
	@Override
	public void onDisable(){
		saveConfig();
	}
	
	@EventHandler
	public void onChunkLoadEvent(ChunkLoadEvent e){
		if(!e.isNewChunk()){
			findChest();
		}
	}
	
	public void findChest(){
		log.info("Filling Chests");
		World world = Bukkit.getWorld("world");
		if(waitRef){
			for(Chunk c : world.getLoadedChunks()){
				for(BlockState b : c.getTileEntities()){
					if(b instanceof Chest){
						inv = ((Chest) b).getBlockInventory();
						String type = FileHandler.getLoc(b.getX(), b.getY(), b.getZ(), b.getWorld().getName());
						if(type.equals("common") || type.equals("uncommon") || type.equals("legendary")){
							inv.clear();
							ItemStack[] stacks = getLoot(FileHandler.getLoc(b.getX(), b.getY(), b.getZ(), b.getWorld().getName()));
							for(int ic = 0; ic < stacks.length; ic++){
								inv.setItem(ic, stacks[ic]);
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
