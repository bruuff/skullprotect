package de.bruuff.skullprotect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitScheduler;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class skullprotect extends JavaPlugin {
	


	@Override
	public void onEnable() {
		initConfig();
		loadGroups();
		new EventListener(this);
		
		if(usingeconomy){
			setupEconomy();
			BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
	        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
	        	int time = 0;
	        	int time_ = 0;
	            public void run() {
	            	time_ = time;
	            	time = (int) Bukkit.getServer().getWorld("world").getTime();
	            	
	            	if(time_ > time && time_ > 23970){
	            		Bukkit.getServer().broadcastMessage(newdaymessage);	
			
	
	            		for (group g : groups.values() ) {
	
							String taxstring = tax.replace("plots", g.getSize()+"").replace("money", Double.toString(economy.bankBalance(g.getName()).balance) );
							double tax_ = Math.floor(calculate( taxstring) );
							
							if(tax_ >= 0){
								EconomyResponse econ_test = economy.bankWithdraw(g.getName(), tax_ );
	            				
	            				
	            				if(econ_test.transactionSuccess()){
	            					for (Player p : Arrays.asList(Bukkit.getServer().getOnlinePlayers()) ){
	        							if(g.isass(p.getName())){
	        								p.sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " taxes have been collected for group " + ChatColor.BLUE + g.getName() + ChatColor.WHITE + ".");
	        							}
	        						}
	            				}else{
	            					for (Player p : Arrays.asList(Bukkit.getServer().getOnlinePlayers()) ){
	        							if(g.isass(p.getName())){
	        								p.sendMessage( ChatColor.DARK_RED + "Unable to pay taxes for group "+ ChatColor.BLUE + g.getName() + ChatColor.RED + "!");
	        								if( punishment != null ){
	        									p.addPotionEffect( punishment );
	        								}
	        							}
	        						}
	        					}
	            			}else{
	            				economy.bankDeposit(g.getName(), -tax_ );
	            				for (Player p : Arrays.asList(Bukkit.getServer().getOnlinePlayers()) ){
	    							if(g.isass(p.getName())){
	    								p.sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " has been given to your group " + ChatColor.BLUE + g.getName() + ChatColor.WHITE + " for owning plots.");
	    							}
	    						}
	            			}		
	            		}
	            		
	            		for (OfflinePlayer op : Arrays.asList(Bukkit.getServer().getOfflinePlayers()) ){
	
	            			String taxstring = tax.replace("plots", landsize( op.getName() )+"").replace("money", Double.toString(economy.getBalance(op)) );
							double tax_ = Math.floor(calculate( taxstring) );
							
	            			if(tax_ >= 0){
	            				EconomyResponse econ_test = economy.withdrawPlayer(op, tax_ );
	            				if(op.isOnline() ){
	            					if(econ_test.transactionSuccess()){
	            						op.getPlayer().sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " taxes have been collected for your plots.");
	            					}else{
	            						op.getPlayer().sendMessage( ChatColor.DARK_RED + "Unable to pay taxes!");
	            						if( punishment != null ){
	            							op.getPlayer().addPotionEffect( punishment );
	            						}
	            					}
	                			}
	            			}else{
	            				economy.depositPlayer(op, -tax_ );
	            				if(op.isOnline() ){
	            					op.getPlayer().sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " has been given to you for owning plots.");
	                			}
	            			}
	
	            		}
	            	}
	            }
	        }, 0L, 20L);
		
		}
        
	}
	
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	String u_head = "";
	String u_name = "";
	
	boolean usingeconomy = false;
	String newdaymessage = "";
	String tax = "";
	String starterprice = "";
	String claimprice = "";
	String maxclaim = "";
	PotionEffect punishment = null;
	
	public static Economy economy = null;
	TreeMap<String,group> groups = new TreeMap<String,group>();
	
	private void initConfig(){
		this.saveDefaultConfig();
		this.reloadConfig();
		System.out.print("[" + this.getDescription().getName() + "] Config loaded.");
		u_head = this.getConfig().getString(this.getDescription().getName() + ".unowned.head");
		u_name = this.getConfig().getString(this.getDescription().getName() + ".unowned.name");
		
		usingeconomy = this.getConfig().getBoolean(this.getDescription().getName() + ".economy.useeconomy");
		
		if(! new File("plugins/Vault.jar").exists()){
			if(usingeconomy){
				System.out.print("[" + this.getDescription().getName() + "] Vault not found, but useeconomy is set to true, setting it to false");
				usingeconomy = false;
			}
		}
		if(usingeconomy){
			System.out.print("[" + this.getDescription().getName() + "] Vault hooked.");
		}
		
		newdaymessage = this.getConfig().getString(this.getDescription().getName() + ".economy.newdaymessage");
		tax = this.getConfig().getString(this.getDescription().getName() + ".economy.tax");
		starterprice = this.getConfig().getString(this.getDescription().getName() + ".economy.starterprice");
		claimprice = this.getConfig().getString(this.getDescription().getName() + ".economy.claimprice");
		maxclaim = this.getConfig().getString(this.getDescription().getName() + ".economy.maxclaim");
		
		if(this.getConfig().getBoolean(this.getDescription().getName() + ".economy.punishment.use") ){
			punishment = new PotionEffect(
								PotionEffectType.getByName(this.getConfig().getString(this.getDescription().getName() + ".economy.punishment.effect")), 
								this.getConfig().getInt(this.getDescription().getName() + ".economy.punishment.duration"), 
								this.getConfig().getInt(this.getDescription().getName() + ".economy.punishment.amplifier")
							 );
		}else{
			punishment = null;
		}

	}
	
	public int landsize(String pname){
		 BufferedReader br = null;
		try {
			File dir = new File("plugins/skullprotect/players/");
			dir.mkdirs();
			File file = new File(dir, pname + ".txt");
			if (!file.isFile() && !file.createNewFile()){
		        System.out.print("Neue datei erstellt.");
		    }
			br = new BufferedReader(new FileReader(file));
			
			//br = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/skullprotect/players/" + pname + ".txt")));			
			String l = null;
			int blocks = 0;		
			
			while((l=br.readLine()) != null){
				if(l.startsWith("Blocks:")){
					blocks = Integer.parseInt(l.substring(7));
				}
			}
			br.close();
			return blocks;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	public void setlandsize(Player p, int amount){
		BufferedWriter bw;
		try {
			File dir = new File("plugins/skullprotect/players/");
			dir.mkdirs();
			File file = new File(dir, p.getName() + ".txt");
			
			bw = new BufferedWriter(new FileWriter(file));
			//bw = new BufferedWriter(new FileWriter("plugins/skullprotect/players/" + p.getName() + ".txt"));
			String l = "Blocks:" + amount;
			bw.write(l);
		    bw.close();
		} catch (IOException e) {e.printStackTrace();}
	}
	
	public float calculate(String func){
		func = func.replaceAll("\\s+","");	
		func = func.replace("+", " + ");
		func = func.replace("-", " - ");
		func = func.replace("*", " * ");
		func = func.replace("/", " / ");
		func = func.replace("^", " ^ ");
		Boolean done = false;
		
		while(!done){
			if(func.contains("(")){
				int i = func.indexOf("(");
				int i2 = func.lastIndexOf(")");
				func = func.replace(func.substring(i, i2+1), calculate(func.substring(i+1, i2))+"" );
				continue;
			}
			done = true;
		}

		ArrayList<String> args = new ArrayList<String>(Arrays.asList(func.split(" ")));
		done = false;
		while(!done){

			if(args.contains("^")){
				int i = args.indexOf("^");
				args.set(i, Math.pow(Float.parseFloat(args.get(i-1)), Float.parseFloat(args.get(i+1))) + "" );
				args.remove(i-1);
				args.remove(i);
				continue;
			}
			
			if(args.contains("*")){
				int i = args.indexOf("*");
				args.set(i, Float.parseFloat(args.get(i-1))*Float.parseFloat(args.get(i+1)) + "" );
				args.remove(i-1);
				args.remove(i);
				continue;
			}
			if(args.contains("/")){
				int i = args.indexOf("/");
				args.set(i, Float.parseFloat(args.get(i-1))/Float.parseFloat(args.get(i+1)) + "" );
				args.remove(i-1);
				args.remove(i);
				continue;
			}
			if(args.contains("+")){
				int i = args.indexOf("+");
				args.set(i, Float.parseFloat(args.get(i-1))+Float.parseFloat(args.get(i+1)) + "" );
				args.remove(i-1);
				args.remove(i);
				continue;
			}
			if(args.contains("-")){
				int i = args.indexOf("-");
				args.set(i, Float.parseFloat(args.get(i-1))-Float.parseFloat(args.get(i+1)) + "" );
				args.remove(i-1);
				args.remove(i);
				continue;
			}
			done = true;
		}
		func = "";
		for (String arg : args){
			func += arg;
		}
		return Float.parseFloat(func);
	}
	
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
				File dir = new File("plugins/skullprotect/groups/");
				dir.mkdirs();
				File file = new File(dir, groupname);
				br = new BufferedReader(new FileReader(file));
				//br = new BufferedReader(new InputStreamReader(new FileInputStream("plugins/skullprotect/groups/" + groupname)));	
				String l = null;
				int size = -1;
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
					if(l.startsWith("Size:")){
						size = Integer.parseInt(l.substring(5));
					}
				}
				br.close();
				groups.put(groupname.substring(0, groupname.length() - 4), new group(groupname.substring(0, groupname.length() - 4),size,ops, assistants, members) );			
			} catch (IOException e) {e.printStackTrace();}
	
	}
	
	public void newGroup(String groupname, Player player){
		ArrayList<String> ops = new ArrayList<String>();
		ArrayList<String> empty = new ArrayList<String>();
		ops.add(player.getName());
		groups.put(groupname, new group(groupname, ops, empty, empty) );
	    saveGroup(new group(groupname, ops, empty, empty));
	    if(usingeconomy){
	    	economy.createBank(groupname, player);
	    }
	}
	public void deleteGroup(String groupname){
		groups.remove(groupname);
		File file = new File("plugins/skullprotect/groups/" + groupname + ".txt");
        if(file.exists()){
            file.delete();     
            boolean fOk = file.delete();
            if (!fOk) {
            	System.out.print("verdammt");
            }
        }
        if(usingeconomy){
	    	economy.deleteBank(groupname);
	    }
        System.out.print("ich habs doch gemacht: plugins/skullprotect/groups/" + groupname + ".txt");
	}
	public void saveGroup(group g){
	    BufferedWriter bw;
		try {
			File dir = new File("plugins/skullprotect/groups/");
			dir.mkdirs();
			File file = new File(dir, g.getName() + ".txt");
			
			bw = new BufferedWriter(new FileWriter(file));	
			String l1 = "Operators:";
			String l2 = "Assistants:";
			String l3 = "Members:";
			String l4 = "Size:" + g.getSize();
			
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
			bw.newLine();
			bw.write(l4);
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
							if(groups.size() >= 1){
								String list = "";
								
								SortedSet<String> keys = new TreeSet<String>(groups.keySet());
								for (String k : keys) { 
								   group g = groups.get(k);
								   if(g.getNumMem() >= 1){
										list = list + ChatColor.GREEN + g.getName() + ChatColor.WHITE + "[" + g.getNumMem() + "], ";
									}else{
										list = list + ChatColor.RED + g.getName() + ChatColor.WHITE + "[" + g.getNumMem() + "], ";	
									}
								}
								list = list.substring(0, list.length() - 2);
								p.sendMessage("Groups: " + list);
							}else{
								p.sendMessage("There are no groups yet.");
							}
						}else{
							p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
						}
					}
				//ADD
					if(args[0].equalsIgnoreCase("add")){
						group g = groups.get(args[1]);
						if(g != null){
							
							if(!( g.isass(p.getName()) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.assistant") ) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.op") )
							|| p.hasPermission("skullprotect.group.foreign.assistant") 
							|| p.hasPermission("skullprotect.group.foreign.op")  )){
								p.sendMessage(ChatColor.RED + "You dont have the permission to edit " + ChatColor.BLUE + args[1]);
								return true;						
							}
							p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Member " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
							g.addmem(args[2]);
							saveGroup(g);
	
						}else{
							p.sendMessage(ChatColor.RED + "Group " + args[1] + " not found.");
						}					
						p.sendMessage(groups.toString());
					}
				//RANK
					if(args[0].equalsIgnoreCase("rank")){

						group g = groups.get(args[1]);
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

						group g = groups.get(args[1]);

						if(g != null){
							if(!( g.isop(p.getName()) 
							|| (g.ismem(p.getName()) && p.hasPermission("skullprotect.group.own.op") )
							|| p.hasPermission("skullprotect.group.foreign.op")  )){
								p.sendMessage(ChatColor.RED + "You dont have the permission to edit " + ChatColor.BLUE + args[1]);
								return true;						
							}
							if(g.ismem(args[2])){
								g.remove(args[2]);
								saveGroup(g);
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
						
							for (group g : groups.values()){
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

						for (group g : groups.values()){
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
				//GET / START
					if(args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("start")){
						ArrayList<String> markers = new ArrayList<String>();
						boolean noperm = false;
						boolean nomember = false;
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
								for (group g : groups.values()){
									if(args[1].equals(g.getName()) ){
										if(g.ismem(p.getName()) || p.hasPermission("skullprotect.get." + ptype + ".foreign") ){
											markers.add("%" + g.getName());
										}else{
											p.sendMessage(ChatColor.RED + "Your are not a member in " + args[1]);
											nomember = true;
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
							}
						}	
						if(markers.size() < 1 && !nomember){
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
							
							
							if(sm.getDisplayName().startsWith("Starter") && usingeconomy){

								if(sm.getOwner().startsWith("%") && groups.get(sm.getOwner().substring(1)) != null){
									
									String starterpricestring = starterprice.replace("plots", groups.get(sm.getOwner().substring(1)).getSize()+"").replace("money", Double.toString(economy.bankBalance(sm.getOwner().substring(1)).balance) );
									double starterprice_ = Math.floor(calculate( starterpricestring) );
									System.out.print(starterpricestring + "|" + starterprice_);
									if(economy.bankBalance(sm.getOwner().substring(1)).balance >= starterprice_){
										economy.bankWithdraw(sm.getOwner().substring(1), starterprice_);
										p.sendMessage( starterprice_ + " " + economy.currencyNamePlural() + " have been paid by your group.");
									}else{
										p.sendMessage("Group " + sm.getOwner().substring(1) + " can't afford this.");
										continue;
									}
								}else{

									String starterpricestring = starterprice.replace("plots", landsize( sm.getOwner() )+"").replace("money", Double.toString(economy.getBalance(p)) );
									double starterprice_ = Math.floor(calculate( starterpricestring) );
									
									if(economy.has(p, starterprice_)){
										economy.withdrawPlayer(p, starterprice_);
										p.sendMessage( starterprice_ + " " + economy.currencyNamePlural() + " have been paid.");
									}else{
										p.sendMessage("You can't afford this.");
										continue;
									}
								}
							}
							
							skull.setItemMeta(sm); 
							inventory.addItem(skull);
						}
						
						
						
					}
				//PRICES
					if(args[0].equalsIgnoreCase("prices") || args[0].equalsIgnoreCase("price") ){
						if(usingeconomy){
							
							String taxstring = tax.replace("plots", landsize( p.getName() )+"").replace("money", Double.toString(economy.getBalance(p)) );
							double tax_ = Math.floor(calculate( taxstring) );
							String starterpricestring = starterprice.replace("plots", landsize( p.getName() )+"").replace("money", Double.toString(economy.getBalance(p)) );
							double starterprice_ = Math.floor(calculate( starterpricestring) );
							String claimpricestring = claimprice.replace("plots", landsize( p.getName() )+"").replace("money", Double.toString(skullprotect.economy.getBalance(p)) );
							double claimprice_ = Math.floor(calculate( claimpricestring) );
							String maxclaimstring = maxclaim.replace("plots", landsize( p.getName() )+"").replace("money", Double.toString(skullprotect.economy.getBalance(p)) );
							double maxclaim_ = Math.floor(calculate( maxclaimstring) );
							
							p.sendMessage("Prices:");
							p.sendMessage("Tax: " + ChatColor.GOLD + tax_ + ChatColor.WHITE + economy.currencyNamePlural() + " (" + ChatColor.GREEN + tax + ChatColor.WHITE + ")");
							p.sendMessage("Starterprice: " + ChatColor.GOLD + starterprice_ + ChatColor.WHITE + economy.currencyNamePlural() + " (" + ChatColor.GREEN + starterprice + ChatColor.WHITE + ")");
							p.sendMessage("Claim price: " + ChatColor.GOLD + claimprice_ + ChatColor.WHITE + economy.currencyNamePlural() + " (" + ChatColor.GREEN + claimprice + ChatColor.WHITE + ")");
							p.sendMessage("Max claim: " + ChatColor.GOLD + maxclaim_ + ChatColor.WHITE + " plots (" + ChatColor.GREEN + maxclaim + ChatColor.WHITE + ")");
							
						
						}else{
							p.sendMessage(ChatColor.RED + "Economy support is disabled.");
						}
					}
				//DEPOSIT
					if(args[0].equalsIgnoreCase("deposit") || args[0].equalsIgnoreCase("d") ){
						try{  
							Double.parseDouble(args[2]);  
						}catch(NumberFormatException nfe){  
							args[2] = "-1"; 
						}  
						if(usingeconomy){
							if(groups.containsKey(args[1])){
								if(Double.parseDouble(args[2]) >= 0){
									EconomyResponse deposit = economy.withdrawPlayer(p, Double.parseDouble(args[2]) );
									if(deposit.transactionSuccess()){
										 economy.bankDeposit(args[1], Double.parseDouble(args[2]) );
										 p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + economy.currencyNamePlural() + ChatColor.GREEN + " have been transferred to " + ChatColor.BLUE + args[1] + ChatColor.GREEN + ".");
									}else{
										p.sendMessage(ChatColor.RED + "You dont have enough money.");
									}
								}else{	
									p.sendMessage(ChatColor.RED + "Invalid value.");
								}
							}else{	
								p.sendMessage(ChatColor.RED + "Group not found.");
							}
						}else{
							p.sendMessage(ChatColor.RED + "Economy support is disabled.");
						}
					}
				//WiTHDRAW
					if(args[0].equalsIgnoreCase("withdraw") || args[0].equalsIgnoreCase("w") ){
						try{  
							Double.parseDouble(args[2]);  
						}catch(NumberFormatException nfe){  
							args[2] = "-1"; 
						} 
						if(usingeconomy){
							if(groups.containsKey(args[1])){
								if(Double.parseDouble(args[2]) >= 0){
									if( groups.get(args[1]).isop(p.getName()) ){
										EconomyResponse withdraw = economy.bankWithdraw(args[1], Double.parseDouble(args[2]) );
										if(withdraw.transactionSuccess()){
											economy.depositPlayer(p, Double.parseDouble(args[2]) );
											 p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + economy.currencyNamePlural() + ChatColor.GREEN + " from group " + ChatColor.BLUE + args[1] + ChatColor.GREEN + " have been transferred to you.");
										}else{
											p.sendMessage(ChatColor.RED + "This group has not enough money.");
										}
									}else{	
										p.sendMessage(ChatColor.RED + "You have to be operator in this group.");
									}
								}else{	
									p.sendMessage(ChatColor.RED + "Invalid value.");
								}
							}else{	
								p.sendMessage(ChatColor.RED + "Group not found.");
							}
						}else{
							p.sendMessage(ChatColor.RED + "Economy support is disabled.");
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
