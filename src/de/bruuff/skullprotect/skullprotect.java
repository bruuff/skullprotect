package de.bruuff.skullprotect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class skullprotect extends JavaPlugin {

	@Override
	public void onEnable() {
		initConfig();
		loadGroups();
		new EventListener(this);

	}
	String u_head = "";
	String u_name = "";
	private void initConfig(){
		this.reloadConfig();
		this.getConfig().options().header("Change what you need to.");
		this.getConfig().addDefault("skullprotect.unowned.name", "Wilderness");
		this.getConfig().addDefault("skullprotect.unowned.head", "MHF_Cake");
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		System.out.print("[" + this.getDescription().getName() + "] Config loaded.");
		u_head = this.getConfig().getString(this.getDescription().getName() + ".unowned.head");
		u_name = this.getConfig().getString(this.getDescription().getName() + ".unowned.name");
	}
	
	ArrayList<group> groups = new ArrayList<group>();
	
	
	private void loadGroups(){
		File[] files = new File("plugins/skullprotect/groups/").listFiles();
		if (files != null) { // Erforderliche Berechtigungen etc. sind vorhanden
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					loadGroup(files[i].getName());
				}
			}
		}
		System.out.print("[" + this.getDescription().getName() + "] " + groups.size() + " groups loaded.");
	}
	
	
	
	private void loadGroup(String groupname){
		ArrayList<String> ops = new ArrayList<String>();
		ArrayList<String> assistants = new ArrayList<String>();
		ArrayList<String> members = new ArrayList<String>();

		    BufferedReader br = null;
			try {
				br = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/skullprotect/groups/" + groupname)));	
				String l = null;
		
				while((l=br.readLine()) != null){
					if(l.startsWith("Operators:")){
						ops.addAll(Arrays.asList((l.substring(10)).split(",")));
						ops.removeAll(Collections.singleton(""));
					}
					if(l.startsWith("Assistants:")){
						assistants.addAll(Arrays.asList((l.substring(11)).split(",")));
						assistants.removeAll(Collections.singleton(""));
					}
					if(l.startsWith("Members:")){
						members.addAll(Arrays.asList((l.substring(8)).split(",")));
						members.removeAll(Collections.singleton(""));
					}	
				}
				
				groups.add(new group(groupname.substring(0, groupname.length() - 4),ops, assistants, members));				
			} catch (IOException e) {e.printStackTrace();}
	
	}
	
	private void newGroup(String groupname, Player player){
		ArrayList<String> ops = new ArrayList<String>();
		ArrayList<String> empty = new ArrayList<String>();
		ops.add(player.getName());
	    groups.add(new group(groupname, ops, empty, empty));
	    saveGroup(new group(groupname, ops, empty, empty));
	}
	private void saveGroup(group g){
	    BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("plugins/skullprotect/groups/" + g.getName() + ".txt"));	
			String l1 = "Operators:";
			String l2 = "Assistants:";
			String l3 = "Members:";

			for (String op : g.getOps()){
				l1 = l1 + op + ",";
			}
			for (String ass : g.getAss()){
				l2 = l2 + ass + ",";
			}
			for (String mem : g.getMem()){
				l3 = l3 + mem + ",";
			}
			
			l1 = l1.substring(0, l1.length() - 1);
			l2 = l2.substring(0, l2.length() - 1);
			l3 = l3.substring(0, l3.length() - 1);

			bw.write(l1);
			bw.newLine();
			bw.write(l2);
			bw.newLine();
			bw.write(l3);
		    bw.close();	
	
		} catch (IOException e) {e.printStackTrace();}
	    
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Umwandlung Sender -> Player
		
		Player p = null;
		if(sender instanceof Player){
			p=(Player) sender;
		}
		
		if(cmd.getName().equalsIgnoreCase("sp")){
			
			if(p != null){	
				
				if(args.length < 1){
					p.sendMessage("Here you'll see the help page later");
					return false;
				}else{
					
				//NEW
					if(args[0].equalsIgnoreCase("newgroup") || args[0].equalsIgnoreCase("new")){
						if(p.hasPermission("skullprotect.group.new")){
							newGroup(args[1], p);
							p.sendMessage("Group " + ChatColor.BLUE + args[1] + ChatColor.WHITE + " created.");
						}else{
							p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
						}
					}
				//LIST
					if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("groups")){
						if(p.hasPermission("skullprotect.group.list")){
							String list = "";
							for (group gr : groups){			
								list = list + ChatColor.GREEN + gr.getName() + ChatColor.WHITE + "[" + gr.getNumMem() + "], ";
							}
							list = list.substring(0, list.length() - 2);
							p.sendMessage("Groups: " + list);
						}else{
							p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
						}
					}
				//ADD
					if(args[0].equalsIgnoreCase("add")){
						group g = null;
						for (group gr : groups){
							if(gr.getName().equals(args[1])){
								g = gr;
							}
						}
						if(g != null){
							
							if(!( g.isass(p.getName()) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.assistant") ) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.op") )
							|| p.hasPermission("skullprotect.group.foreign.assistant") 
							|| p.hasPermission("skullprotect.group.foreign.op")  )){
								p.sendMessage(ChatColor.RED + "You dont have the permission to edit " + ChatColor.BLUE + args[1]);
								return true;						
							}

							g.addmem(args[2]);
							saveGroup(g);
	
						}else{
							p.sendMessage(ChatColor.RED + "Group " + args[1] + "not found.");
						}					
						p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Member " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
					}
				//RANK
					if(args[0].equalsIgnoreCase("rank")){
						group g = null;
						
						for (group gr : groups){
							if(gr.getName().equals(args[1])){
								g = gr;
							}
						}
						if(g != null){
					
							if(!( g.isop(p.getName()) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.op") )
							|| p.hasPermission("skullprotect.group.foreign.op")  )){
								p.sendMessage(ChatColor.RED + "You dont have the permission to edit " + ChatColor.BLUE + args[1]);
								return true;						
							}
							
							if(g.ismem(args[2])){
								g.remove(args[2]);
								if(args[3].equalsIgnoreCase("operator")){
									g.addop(args[2]);
									saveGroup(g);
									p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Operator " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
								}else if(args[3].equalsIgnoreCase("assistant")){
									g.addass(args[2]);
									saveGroup(g);
									p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Assistant " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
								}else if(args[3].equalsIgnoreCase("member")){
									g.addmem(args[2]);
									saveGroup(g);
									p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Member " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
								}else{
									p.sendMessage(ChatColor.RED + "Invalid Rank");
								}
							}else{
								p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not in Group " + ChatColor.BLUE + args[1]);
							}
						}else{
							p.sendMessage(ChatColor.RED + "Group " + args[1] + "not found.");
						}	
					}
				//REMOVE
					if(args[0].equalsIgnoreCase("remove")){
						group g = null;
						
						for (group gr : groups){
							if(gr.getName().equals(args[1])){
								g = gr;
							}
						}
						if(g != null){
							if(!( g.isop(p.getName()) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.op") )
							|| p.hasPermission("skullprotect.group.foreign.op")  )){
								p.sendMessage(ChatColor.RED + "You dont have the permission to edit " + ChatColor.BLUE + args[1]);
								return true;						
							}
							if(g.ismem(args[2])){
								g.remove(args[2]);
								p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " was removed from Group " + ChatColor.BLUE + args[1]);
							}else{
								p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not in Group " + ChatColor.BLUE + args[1]);
							}
						}else{
							p.sendMessage(ChatColor.RED + "Group " + args[1] + "not found.");
						}	
					}
				//ALL
					if(args[0].equalsIgnoreCase("all")){
						
						ArrayList<String> markers = new ArrayList<String>();
						
						if(p.hasPermission("skullprotect.get.marker.own")){
							markers.add(p.getName());
						}
						if(p.hasPermission("skullprotect.get.marker.unowned")){
							markers.add(this.getConfig().getString(this.getDescription().getName() + ".unowned.name"));
						}
						
						if(p.hasPermission("skullprotect.get.marker.group")){
							for (group g : this.groups){
								if(g.ismem(p.getName())){
									markers.add("%" + g.getName());
								}
							}
						}
						PlayerInventory inventory = p.getInventory();
						for (String m : markers){
							
							ItemStack skull = new ItemStack(Material.SKULL_ITEM);
							skull.setDurability((short)3);
							SkullMeta sm = (SkullMeta) skull.getItemMeta();					
							if(m.startsWith("%")){
								sm.setDisplayName("Marker: " + ChatColor.BLUE + m.substring(1));
								sm.setOwner(m);	
							}else if(m.equals(p.getName())){
								sm.setDisplayName("Marker: " + ChatColor.YELLOW + m);
								sm.setOwner(m);	
							}else if(m.equals(u_name)){
								sm.setDisplayName("Marker: " + ChatColor.GREEN + m);
								sm.setOwner(u_head);	
							}else{
								continue;
							}
							skull.setItemMeta(sm); 
							inventory.removeItem(skull);
							inventory.addItem(skull);
						}	
					}
				//CLEAR
					if(args[0].equalsIgnoreCase("clear")){
					
						ArrayList<String> markers = new ArrayList<String>();
						markers.add(p.getName());
						markers.add(this.getConfig().getString(this.getDescription().getName() + ".unowned.name"));

						for (group g : this.groups){
							if(g.ismem(p.getName())){
								markers.add("%" + g.getName());
							}
						}
						
						PlayerInventory inventory = p.getInventory();
						for (String m : markers){
							
							ItemStack skull = new ItemStack(Material.SKULL_ITEM);
							skull.setDurability((short)3);
							SkullMeta sm = (SkullMeta) skull.getItemMeta();					
							if(m.startsWith("%")){
								sm.setDisplayName("Marker: " + ChatColor.BLUE + m.substring(1));
								sm.setOwner(m);	
							}else if(m.equals(p.getName())){
								sm.setDisplayName("Marker: " + ChatColor.YELLOW + m);
								sm.setOwner(m);	
							}else if(m.equals(u_name)){
								sm.setDisplayName("Marker: " + ChatColor.GREEN + m);
								sm.setOwner(u_head);	
							}else{
								continue;
							}
							skull.setItemMeta(sm); 
							inventory.removeItem(skull);
						}	
					}	
				//GET
					if(args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("start")){
						ArrayList<String> markers = new ArrayList<String>();
						boolean noperm = false;
						String type ="Marker";
						String ptype ="marker";
						if(args[0].equalsIgnoreCase("start")){
							type ="Starter";
							ptype ="starter";
						}
						
						if(args.length < 2){
							if(p.hasPermission("skullprotect.get." + ptype + ".own")){
								markers.add(p.getName());
							}else{
								noperm = true;
							}
						}else if(args[1].equals(p.getName())){
							if(p.hasPermission("skullprotect.get." + ptype + ".own")){
								markers.add(p.getName());
							}else{
								noperm = true;
							}
						}else{
							if(p.hasPermission("skullprotect.get." + ptype + ".group") || p.hasPermission("skullprotect.get." + ptype + ".foreign")){
								for (group g : this.groups){
									if(args[1].equals(g.getName()) ){
										if(g.ismem(p.getName()) || p.hasPermission("skullprotect.get." + ptype + ".foreign") ){
											markers.add("%" + g.getName());
										}else{
											p.sendMessage(ChatColor.RED + "Your are not a member in " + args[1]);
										}
									}
								}
							}else{
								noperm = true;
							}
							if(p.hasPermission("skullprotect.get." + ptype + ".foreign")){
								if(Arrays.asList(p.getServer().getOnlinePlayers()).contains(args[1])){
									markers.add(args[1]);
								}else if(markers.size() < 1){
									p.sendMessage(ChatColor.RED + "Player not registered.");
								}
							}else{
								noperm = true;
							}
						}	
						if(markers.size() < 1 && !noperm){
							p.sendMessage(ChatColor.RED +"Player/Group not found");
						}
						if(markers.size() >= 1 && noperm){
							p.sendMessage(ChatColor.RED +"You dont have to permission to do that.");
						}
						PlayerInventory inventory = p.getInventory();
						for (String m : markers){
							
							ItemStack skull = new ItemStack(Material.SKULL_ITEM);
							skull.setDurability((short)3);
							SkullMeta sm = (SkullMeta) skull.getItemMeta();					
							if(m.startsWith("%")){
								sm.setDisplayName(type + ": " + ChatColor.BLUE + m.substring(1));
								sm.setOwner(m);	
							}else if(m.equals(p.getName())){
								sm.setDisplayName(type + ": " + ChatColor.YELLOW + m);
								sm.setOwner(m);
							}else{
								continue;
							}
							skull.setItemMeta(sm); 
							inventory.addItem(skull);
						}	
						
					}
				
				}
				
				return true;
			} else {
				sender.sendMessage("[" + this.getDescription().getName() + "] console support will be added soon.");
				return true;
			}
						
		}	
		return false;
	}
	
	
}
