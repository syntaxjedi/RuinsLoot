package net.syntaxjedi.ruinsloot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.block.Chest;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class RuinsLoot extends JavaPlugin{
	
	private static final Logger log = Logger.getLogger("Minecraft");
	public static Inventory inv;
	public static Block bl;
	public int chestNumber;
	public int time;
	public Long timel;
	public int spawnTime;
	public Long spawnTimeL;
	public static boolean waitRef = true;
	@Override
	public void onEnable(){

		this.getServer().getPluginManager().registerEvents(new BlockEvent(), this);
		
		//register commands file
		log.info("[RuinsLoot] Registering Commands");
		Commands commands = new Commands(this);
		this.getCommand("lootchest").setExecutor(commands);
		log.info("[RuinsLoot] Commands Registered, Checking Plugin Files");
		this.saveDefaultConfig();
		FileHandler.createFile();
		log.info("[RuinsLoot] File Check Successful");
		
		
		time = getConfig().getInt("time.ticks");
		timel = (long)time;
		
		spawnTime = getConfig().getInt("random.ticks");
		spawnTimeL = (long)spawnTimeL;
		
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
	}
	
	@EventHandler
	public void onChunkLoadEvent(ChunkLoadEvent e){
		if(!e.isNewChunk()){
			findChest();
		}
	}
	
	public void findChest(){
		log.info("[RuinsLootXL] Filling Chests");
		World world = Bukkit.getWorld("world");
		Map<Integer, ArrayList<Object>> cLoc = FileHandler.getLoc();
		Location loc = new Location(world, 0, 0, 0);
		
		for (int i = 0; i < cLoc.size(); i++){
			loc.setX((double) cLoc.get(i).get(2));
			loc.setY((double) cLoc.get(i).get(3));
			loc.setZ((double) cLoc.get(i).get(4));
			
			Block lChest = loc.getBlock();
			Chunk chunk = world.getChunkAt(loc);
			if(!chunk.isLoaded()){
				world.loadChunk(chunk);
			}
			if(lChest.getType().CHEST != null){
				org.bukkit.block.Chest chest = (org.bukkit.block.Chest) lChest.getState();
				inv = chest.getBlockInventory();
				ItemStack[] stacks = getLoot(cLoc.get(i).get(1).toString());
				inv.setContents(stacks);
			}
		}
	}
	
	public ItemStack[] getLoot(String chest){
		this.reloadConfig();
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
