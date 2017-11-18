package net.syntaxjedi.ruinsloot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Commands implements CommandExecutor{
	
	RuinsLoot plugin;
	public Commands(RuinsLoot instance){
		this.plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
		if(!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "This plugin does not have console support.");
			return true;
		}else{
			Player p = (Player) sender;
			if(command.getName().equalsIgnoreCase("lootchest")){
				int emptySlot = p.getInventory().firstEmpty();
				if(args.length == 0){
					
					p.sendMessage(ChatColor.RED + "Use /lootchest help to see all the commands");
					return true;
					
				}else if(args.length == 1){
					
					switch(args[0]){
					
					case "help":
						p.sendMessage(ChatColor.BLUE + "============================================" + 
								ChatColor.GOLD + "\nCurrent Version: " + ChatColor.LIGHT_PURPLE + plugin.getDescription().getVersion() + ChatColor.GOLD + "\nAuthor: " 
								+ ChatColor.GREEN + plugin.getDescription().getAuthors() + ChatColor.GOLD + "\nCommands:" + "\n- /lootchest help :" + ChatColor.WHITE + " Displays this screen" + ChatColor.GOLD + 
								"\n- /lootchest <common | uncommon | legendary> :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary" +
								ChatColor.GOLD + "\n- /lootchest <common | uncommon | legendary> [name] :" + ChatColor.WHITE + " Gives a lootchest of the type common, uncommon, or legendary, with a custom name. Limit 2 appending names." + 
								ChatColor.GOLD + "\n- /lootchest fill :" + ChatColor.WHITE + " Fills all lootchests with their respective items from the config.");
						return true;
					
					case "common":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, "Common LootChest", "common"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "Common LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
					
					case "uncommon":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, "Uncommon LootChest", "uncommon"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "Uncommon LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
					
					case "legendary":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, "Legendary LootChest", "legendary"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "L LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
						
					case "fill":
						p.sendMessage(ChatColor.GOLD + "Filling Chests");
						plugin.waitRef = true;
						plugin.findChest();
						return true;
						
					default:
						p.sendMessage(ChatColor.RED + "Unknown type " + "\"" + args[0] + "\"");
					}
				}else if(args.length >= 2 && args.length < 4){
					String name = "";
					for(int i = 1; i < args.length; i++){
						name += args[i] + " ";
						
					}
					switch(args[0]){
					
					case "common":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, name + "Lootchest", "common"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "Common LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
					
					case "uncommon":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, name + "Lootchest", "uncommon"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "Uncommon LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
						
					case "legendary":
						p.getInventory().setItem(emptySlot, RuinsLoot.createItem(Material.CHEST, 1, name + "Lootchest", "legendary"));
						p.sendMessage(ChatColor.GOLD + "Gave " + ChatColor.WHITE + "L LootChest " + ChatColor.GOLD + "to " + p.getDisplayName());
						return true;
						
					default:
						p.sendMessage(ChatColor.RED + "Unknown type " + "\"" + args[0] + "\"");
						
					}
				}else if(args.length >= 4){
					p.sendMessage(ChatColor.RED + "Too many arguments.");
					return false;
				}
			}
		}
		return false;
	}
}
