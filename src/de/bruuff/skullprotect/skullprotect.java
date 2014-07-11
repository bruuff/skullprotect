package de.bruuff.skullprotect;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
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
		loadPlayers();
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
			
	
	            		for (Group g : groups.values() ) {
	
							String taxstring = tax.replace("plots", g.getSize()+"").replace("money", Double.toString(economy.bankBalance(g.getName()).balance) );
							double tax_ = Math.floor(calculate( taxstring) );
							
							if(tax_ >= 0){
								EconomyResponse econ_test = economy.bankWithdraw(g.getName(), tax_ );
	            				
	            				
	            				if(econ_test.transactionSuccess()){
	            					for (Player p : Arrays.asList(Bukkit.getServer().getOnlinePlayers()) ){
	        							if(g.isass(p.getUniqueId())){
	        								p.sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " taxes have been collected for group " + ChatColor.BLUE + g.getName() + ChatColor.WHITE + ".");
	        							}
	        						}
	            				}else{
	            					for (Player p : Arrays.asList(Bukkit.getServer().getOnlinePlayers()) ){
	        							if(g.isass(p.getUniqueId())){
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
	    							if(g.isass(p.getUniqueId())){
	    								p.sendMessage( ChatColor.GREEN +  Double.toString(tax_) + ChatColor.AQUA + " " + economy.currencyNamePlural() + " has been given to your group " + ChatColor.BLUE + g.getName() + ChatColor.WHITE + " for owning plots.");
	    							}
	    						}
	            			}		
	            		}
	            		
	            		for (OfflinePlayer op : Arrays.asList(Bukkit.getServer().getOfflinePlayers()) ){
	
	            			String taxstring = tax.replace("plots", splayer.get( op.getUniqueId() ).getSize()+"").replace("money", Double.toString(economy.getBalance(op)) );
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
	String g_head = "";
	Integer lm_radius = 1;
	ArrayList<Material> interact_mats = new ArrayList<Material>();
	ArrayList<EntityType> interact_ents = new ArrayList<EntityType>();
	boolean usingeconomy = false;
	String newdaymessage = "";
	String tax = "";
	String landmarkprice = "";
	String claimprice = "";
	String maxclaim = "";
	PotionEffect punishment = null;
	
	boolean dropall = false;
	
	public static Economy economy = null;
	HashMap<Integer,Group> groups = new HashMap<Integer,Group>();
	HashMap<UUID,SPlayer> splayer = new HashMap<UUID,SPlayer>();

	private void initConfig(){
		this.saveDefaultConfig();
		this.reloadConfig();
		u_head = this.getConfig().getString(this.getDescription().getName() + ".unowned.head");
		u_name = this.getConfig().getString(this.getDescription().getName() + ".unowned.name");
		
		g_head = this.getConfig().getString(this.getDescription().getName() + ".groups.head");
		
		if(this.getConfig().getString(this.getDescription().getName() + ".interaction.blocks").length() > 0)
			for(String mat : this.getConfig().getString(this.getDescription().getName() + ".interaction.blocks").split(",") ){
				try{
					interact_mats.add(Material.getMaterial(mat));
				}catch(IllegalArgumentException e) {
					System.out.print("[" + this.getDescription().getName() + "] No such material: " + mat);
				}
			}
		
		if(this.getConfig().getString(this.getDescription().getName() + ".interaction.entities").length() > 0)
			for(String ent : this.getConfig().getString(this.getDescription().getName() + ".interaction.entities").split(",") ){
				try{
					interact_ents.add(EntityType.valueOf(ent));
				}catch(IllegalArgumentException e) {
					System.out.print("[" + this.getDescription().getName() + "] No such entity: " + ent);
				}
			}

		lm_radius = this.getConfig().getInt(this.getDescription().getName() + ".landmark.radius");
		
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
		landmarkprice = this.getConfig().getString(this.getDescription().getName() + ".economy.landmarkprice");
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
		
		dropall = this.getConfig().getBoolean(this.getDescription().getName() + ".compatibility.dropall");

		System.out.print("[" + this.getDescription().getName() + "] Config loaded.");
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
	
	private void loadPlayers(){
		File[] files = new File("plugins/skullprotect/players/").listFiles();
		if (files != null) { // Erforderliche Berechtigungen etc. sind vorhanden
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					loadPlayer(UUID.fromString(files[i].getName().substring(0,files[i].getName().length() - 4)) );
				}
			}
		}
		System.out.print("[" + this.getDescription().getName() + "] " + splayer.size() + " player data loaded.");
	}
	
	private void loadPlayer(UUID playeruuid){

		File dir = new File("plugins/skullprotect/players/");
		dir.mkdirs();
		File file = new File(dir, playeruuid + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);
		
		ArrayList<Landmark> landmarks = new ArrayList<Landmark>();
		for(String x : yamlFile.getStringList("landmarks")){
			landmarks.add(new Landmark(
							Bukkit.getWorld(x.split(",")[0]),
							Integer.parseInt(x.split(",")[1]),
							Integer.parseInt(x.split(",")[2]),
							Integer.parseInt(x.split(",")[3]) 
						  )
						);
			
		}
		Perm permissions = new Perm(yamlFile.getBoolean("permission.pvp"), yamlFile.getBoolean("permission.explosion"), yamlFile.getBoolean("permission.firespread"), yamlFile.getBoolean("permission.mobspawn"));

		splayer.put(playeruuid , new SPlayer(Bukkit.getOfflinePlayer(playeruuid ), yamlFile.getInt("size"), landmarks, permissions) );

	}
		
	private void loadGroups(){
		File[] files = new File("plugins/skullprotect/groups/").listFiles();
		if (files != null) { // Erforderliche Berechtigungen etc. sind vorhanden
			for (int i = 0; i < files.length; i++) {
				if (!files[i].isDirectory()) {
					int groupid = -1; 
					try{  
						groupid = Integer.parseInt(files[i].getName().substring(0, files[i].getName().length() - 4));  
					}catch(NumberFormatException nfe) {  
				    	System.out.print("[skullprotect] Found invalid group file : " + files[i].getName()); 
					}
					if(groupid != -1){
						loadGroup(groupid);
					}			
				}
			}
		}
		System.out.print("[" + this.getDescription().getName() + "] " + groups.size() + " groups loaded.");
	}

	private void loadGroup(int groupid){
		File dir = new File("plugins/skullprotect/groups/");
		dir.mkdirs();
		File file = new File(dir, groupid + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);
		ArrayList<UUID> ops = new ArrayList<UUID>();
		for(String x : yamlFile.getStringList("operators")){
			ops.add(UUID.fromString(x));	
		}
		ArrayList<UUID> ass = new ArrayList<UUID>();
		for(String x : yamlFile.getStringList("assistants")){
			ass.add(UUID.fromString(x));	
		}
		ArrayList<UUID> mem = new ArrayList<UUID>();
		for(String x : yamlFile.getStringList("members")){
			mem.add(UUID.fromString(x));	
		}
		
		
		ArrayList<Landmark> landmarks = new ArrayList<Landmark>();
		for(String x : yamlFile.getStringList("landmarks")){
			landmarks.add(new Landmark(
							Bukkit.getWorld(x.split(",")[0]),
							Integer.parseInt(x.split(",")[1]),
							Integer.parseInt(x.split(",")[2]),
							Integer.parseInt(x.split(",")[3]) 
						  )
						);
			
		}
		Perm permissions = new Perm(yamlFile.getBoolean("permission.pvp"), yamlFile.getBoolean("permission.explosion"), yamlFile.getBoolean("permission.firespread"), yamlFile.getBoolean("permission.mobspawn"));

		groups.put(groupid, new Group(groupid, yamlFile.getString("name"), yamlFile.getInt("size"), ops, ass, mem, landmarks, permissions) );	
	}

	
	
	public void newGroup(String groupname, Player player){
		ArrayList<UUID> ops = new ArrayList<UUID>();
		ArrayList<UUID> empty = new ArrayList<UUID>();
		ops.add(player.getUniqueId());
		int id = getnextGId();
		groups.put(getnextGId(), new Group(id, groupname, 0, ops, empty, empty) );
	    saveGroup(groups.get(id));
	    if(usingeconomy){
	    	economy.createBank("%" + id, player);
	    }
	}
	public void saveGroup(Group g){

			File dir = new File("plugins/skullprotect/groups/");
			dir.mkdirs();
			File file = new File(dir, g.getId() + ".yml");
			
			if(g.getNumMem() > 0 || g.getSize() > 0){
				
				YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);
				yamlFile.createSection("name");
				yamlFile.createSection("size");
				yamlFile.createSection("operators");
				yamlFile.createSection("assistants");
				yamlFile.createSection("members");
				yamlFile.createSection("permission.pvp");
				yamlFile.createSection("permission.explosion");
				yamlFile.createSection("permission.firespread");
				yamlFile.createSection("permission.mobspawn");
				yamlFile.createSection("landmarks");
				
				yamlFile.set("name", g.getName());
				yamlFile.set("size", g.getSize());
				List<String> ops = new ArrayList<String>();	
				for (UUID uuid_ : g.getOps()){
					ops.add(uuid_.toString());
				}
				yamlFile.set("operators", ops);
				List<String> ass = new ArrayList<String>();	
				for (UUID uuid_ : g.getAss()){
					ass.add(uuid_.toString());
				}
				yamlFile.set("assistants", ass);
				List<String> mem = new ArrayList<String>();		
				for (UUID uuid_ : g.getMem()){
					mem.add(uuid_.toString());
				}
				yamlFile.set("members", mem);

				yamlFile.set("permission.pvp", g.getPermission().getPvP());
				yamlFile.set("permission.explosion", g.getPermission().getExplosion());
				yamlFile.set("permission.firespread", g.getPermission().getFirespread());
				yamlFile.set("permission.mobspawn", g.getPermission().getMobspawn());
				
				List<String> lms = new ArrayList<String>();	
				for (Landmark lm : g.getLandmarks()){
					lms.add(lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ());
				}
				yamlFile.set("landmarks", lms);

				try {
					yamlFile.save(file);
				} catch(IOException e) {e.printStackTrace();}

			}else{
				groups.remove(g.getId());
				if(file.exists()){  
		            boolean fOk = file.delete();
		            if (!fOk) {
		            	System.out.print("[skullprotect] group deletion failed for group " + g.getName());
		            }
		        }
		        if(usingeconomy){
			    	economy.deleteBank("%" + g.getId());
			    }

			}
			
		
	    
	}
	
	public void savePlayer(SPlayer sp){
		
		File dir = new File("plugins/skullprotect/players/");
		dir.mkdirs();
		File file = new File(dir, sp.getPlayer().getUniqueId() + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);
		yamlFile.createSection("id");
		yamlFile.createSection("size");
		yamlFile.createSection("permission");
		yamlFile.createSection("permission.pvp");
		yamlFile.createSection("permission.explosion");
		yamlFile.createSection("permission.firespread");
		yamlFile.createSection("permission.mobspawn");
		yamlFile.createSection("landmarks");
		
		//yamlFile.set("id", sp.getId());
		yamlFile.set("size", sp.getSize());
		yamlFile.set("permission.pvp", sp.getPermission().getPvP());
		yamlFile.set("permission.explosion", sp.getPermission().getExplosion());
		yamlFile.set("permission.firespread", sp.getPermission().getFirespread());
		yamlFile.set("permission.mobspawn", sp.getPermission().getMobspawn());
		
		List<String> lms = new ArrayList<String>();	
		for (Landmark lm : sp.getLandmarks()){
			lms.add(lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ());
		}
		yamlFile.set("landmarks", lms);

		try {
			yamlFile.save(file);
		} catch(IOException e) {e.printStackTrace();}

	}

	public int getnextGId(){
		int i = 0;
		for(Group group : groups.values()){
			if(group.getId() > i){
				i = group.getId();
			}
		}
		i++;
		return i;
	}
	/*
	public int getnextPId(){
		int i = 0;
		for(SPlayer sp : splayer.values()){
			if(sp.getId() > i){
				i = sp.getId();
			}
		}
		i++;
		return i;
	}
	 */
	private HashMap<Integer,Group> sortedGroups() { 
		List<Group> list = new LinkedList<Group>(groups.values());
		Collections.sort(list, new Comparator<Group>() {
			public int compare(Group g1, Group g2) {	
				int result = new Integer(g2.getNumMem()).compareTo( new Integer(g1.getNumMem()) );		
				if (result == 0) {
					result = g1.getName().compareTo( g2.getName() );
				}
				return result;
			}
	    });

		HashMap<Integer,Group> sortedHashMap = new LinkedHashMap<Integer,Group>();
		for (Iterator<Group> it = list.iterator(); it.hasNext();) {
			Group entry = it.next();
			sortedHashMap.put(entry.getId(), entry);
		} 
		return sortedHashMap;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	
		Player p = null;
		if(sender instanceof Player){
			p=(Player) sender;
		}
		
		if(cmd.getName().equalsIgnoreCase("sp")){	
			if(p != null){
				boolean didyousayhelp = false;
				if(args.length == 1){
					if(args[0].equalsIgnoreCase("help")){
						didyousayhelp = true;
					}
				}

				if(args.length < 1 || didyousayhelp ){
					didyousayhelp = true;	
				}else{

					if(args[0].equalsIgnoreCase("all")){
						
						ItemStack[] contents = p.getInventory().getContents();
						for(ItemStack istack_ : contents){
							if(istack_ != null)
								if(istack_.hasItemMeta())
									if(istack_.getItemMeta() instanceof SkullMeta)
										if(istack_.getItemMeta().getDisplayName().startsWith("Marker: "))
											p.getInventory().remove(istack_);					
						}
						
						ArrayList<String> markers = new ArrayList<String>();
						
						if(p.hasPermission("skullprotect.get.marker.own")){
							markers.add(p.getName());
						}
						if(p.hasPermission("skullprotect.get.marker.unowned")){
							markers.add(this.getConfig().getString(this.getDescription().getName() + ".unowned.name"));
						}
						
						if(p.hasPermission("skullprotect.get.marker.group")){
						
							for (Group g : groups.values()){
								if(g.ismem(p.getUniqueId())){
									markers.add("%" + g.getId());
								}
							}
						
						}
						PlayerInventory inventory = p.getInventory();
						for (String m : markers){

							ItemStack skull = new ItemStack(Material.SKULL_ITEM);
							skull.setDurability((short)3);
							SkullMeta sm = (SkullMeta) skull.getItemMeta();					
							if(m.startsWith("%")){
								sm.setDisplayName("Marker: " + ChatColor.BLUE + groups.get(Integer.parseInt(m.substring(1)) ).getName() );
								sm.setOwner(g_head);	
							}else if(m.equals(p.getName())){
								sm.setDisplayName("Marker: " + ChatColor.YELLOW + m);
								sm.setOwner(p.getName());	
							}else if(m.equals(u_name)){
								sm.setDisplayName("Marker: " + ChatColor.GREEN + m);
								sm.setOwner(u_head);	
							}else{
								continue;
							}
							skull.setAmount(-1);
							skull.setItemMeta(sm); 
							if(!dropall){
								for(ItemStack is : inventory.addItem(skull).values()) {
									p.getWorld().dropItemNaturally(p.getLocation(), is);
						    	}
							}else{
								p.getWorld().dropItemNaturally(p.getLocation(), skull);
							}
						}	
						p.sendMessage("Here you go.");
					}else if(args[0].equalsIgnoreCase("clear")){

						ItemStack[] contents = p.getInventory().getContents();
						for(ItemStack istack_ : contents){
							if(istack_ != null)
								if(istack_.hasItemMeta())
									if(istack_.getItemMeta() instanceof SkullMeta)
										if(istack_.getItemMeta().getDisplayName().startsWith("Marker: "))
											p.getInventory().remove(istack_);					
						}

					}else if(args[0].equalsIgnoreCase("get")){
						ArrayList<String> markers = new ArrayList<String>();
						boolean noperm = false;
						boolean nomember = false;
						String type ="Marker";
						String ptype ="marker";
						
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
								for (Group g : groups.values()){
									if(args[1].equals(g.getName()) ){
										if(g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.get." + ptype + ".foreign") ){
											markers.add("%" + g.getId());
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
								sm.setDisplayName("Marker: " + ChatColor.BLUE + groups.get(Integer.parseInt(m.substring(1)) ).getName() );
								sm.setOwner(m);	
							}else if(m.equals(p.getName())){
								sm.setDisplayName(type + ": " + ChatColor.YELLOW + m);
								sm.setOwner(m);
							}else{
								continue;
							}
							
							skull.setItemMeta(sm); 
							//inventory.addItem(skull);
							if(!dropall){
								for(ItemStack is : inventory.addItem(skull).values()) {
									p.getWorld().dropItemNaturally(p.getLocation(), is);
						    	}
							}else{
								p.getWorld().dropItemNaturally(p.getLocation(), skull);
							}
							p.sendMessage("Here you go.");
						}		
					}else{
						didyousayhelp = true;
					}
				}
				
				if(didyousayhelp){
					p.sendMessage("You can get a detailed command overview with " + ChatColor.GOLD + "/help" + ChatColor.WHITE );	
				}
				return true;
			} else {
				sender.sendMessage("[" + this.getDescription().getName() + "] console support will be added soon.");
				return true;
			}
						
		}
		if(cmd.getName().equalsIgnoreCase("private") || cmd.getName().equalsIgnoreCase("p")){
			if(p != null){	
				if(args.length < 1){
					SPlayer sp = splayer.get(p.getUniqueId());
					p.sendMessage(ChatColor.YELLOW + p.getName() + ChatColor.WHITE +  ":");
					
					p.sendMessage("  Land claimed: " + sp.getSize() + " sq m" );
					
					String perms = "";
					if(sp.getPermission().getPvP()){perms+= ChatColor.GREEN + "PvP";}else{perms+= ChatColor.RED + "PvP";}
					perms+= ChatColor.WHITE + ", ";
					if(sp.getPermission().getExplosion()){perms+= ChatColor.GREEN + "Explosion";}else{perms+= ChatColor.RED + "Explosion";}
					perms+= ChatColor.WHITE + ", ";
					if(sp.getPermission().getFirespread()){perms+= ChatColor.GREEN + "Firespread";}else{perms+= ChatColor.RED + "Firespread";}
					perms+= ChatColor.WHITE + ", ";
					if(sp.getPermission().getMobspawn()){perms+= ChatColor.GREEN + "Mobspawn";}else{perms+= ChatColor.RED + "Mobspawn";}
					perms+= ChatColor.WHITE;
					p.sendMessage("  Permissions: " + perms);
				}else{
					if(args[0].equalsIgnoreCase("toggle")){
						if(args.length == 2){
							SPlayer sp = splayer.get(p.getUniqueId());				
							if(args[1].equalsIgnoreCase("pvp")){
								if(sp.getPermission().getPvP()){
									sp.getPermission().setPvP(false);
									p.sendMessage("PvP is now disabled.");
								}else{
									sp.getPermission().setPvP(true);
									p.sendMessage("PvP is now enabled.");
								}
							}else if(args[1].equalsIgnoreCase("exp") || args[1].equalsIgnoreCase("explosion")){
								if(sp.getPermission().getExplosion()){
									sp.getPermission().setExplosion(false);
									p.sendMessage("Explosions are now disabled.");
								}else{
									sp.getPermission().setExplosion(true);
									p.sendMessage("Explosions are now enabled.");
								}
							}else if(args[1].equalsIgnoreCase("fire") || args[1].equalsIgnoreCase("firespread")){
								if(sp.getPermission().getFirespread()){
									sp.getPermission().setFirespread(false);
									p.sendMessage("Firespread is now disabled.");
								}else{
									sp.getPermission().setFirespread(true);
									p.sendMessage("Firespread is now enabled.");
								}
							}else if(args[1].equalsIgnoreCase("mobs") || args[1].equalsIgnoreCase("mobspawn")){
								if(sp.getPermission().getMobspawn()){
									sp.getPermission().setMobspawn(false);
									p.sendMessage("Mobspawn is now disabled.");
								}else{
									sp.getPermission().setMobspawn(true);
									p.sendMessage("Mobspawn is now enabled.");
								}
							}else{
								p.sendMessage("Valid permissions: pvp, exp/explosion, fire/firespread, mobs/mobspawn");
							}
							savePlayer(sp);
						}else{
							p.sendMessage("Pleace specify a permission.");
						}
					}else if(args[0].equalsIgnoreCase("prices")){
						if(usingeconomy){
							String taxstring = tax.replace("plots", splayer.get( p.getUniqueId() ).getSize()+"").replace("money", Double.toString(economy.getBalance(p)) );
							int tax_ = (int)Math.floor(calculate( taxstring) );
							String landmarkpricestring = landmarkprice.replace("plots", splayer.get( p.getUniqueId() ).getSize()+"").replace("money", Double.toString(economy.getBalance(p)) );
							int landmarkprice_ = (int)Math.floor(calculate( landmarkpricestring) );
							String claimpricestring = claimprice.replace("plots", splayer.get( p.getUniqueId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.getBalance(p)) );
							int claimprice_ = (int)Math.floor(calculate( claimpricestring) );
							String maxclaimstring = maxclaim.replace("plots", splayer.get( p.getUniqueId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.getBalance(p)) );
							int maxclaim_ = (int) Math.floor(calculate( maxclaimstring) );
							
							p.sendMessage("Prices for " + ChatColor.YELLOW + p.getName() + ChatColor.WHITE + ":");
							p.sendMessage("Tax: " + ChatColor.GOLD + tax_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + tax + ChatColor.WHITE + ")");
							p.sendMessage("Landmark price: " + ChatColor.GOLD + landmarkprice_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + landmarkprice + ChatColor.WHITE + ")");
							p.sendMessage("Claim price: " + ChatColor.GOLD + claimprice_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + claimprice + ChatColor.WHITE + ")");
							p.sendMessage("Max claim: " + ChatColor.GOLD + maxclaim_ + ChatColor.WHITE + " plots (" + ChatColor.GREEN + maxclaim + ChatColor.WHITE + ")");
						}else{
							p.sendMessage(ChatColor.RED + "Economy support is disabled.");
						}
					}else{
						p.sendMessage("Valid subcommands: toggle, prices");
					}
				}
				return true;
			} else {
				sender.sendMessage("[" + this.getDescription().getName() + "] console support will be added soon.");
				return true;
			}
			
		}
		if(cmd.getName().equalsIgnoreCase("group") || cmd.getName().equalsIgnoreCase("g")){
			
			if(p != null){	
				if(args.length < 1){			
					p.sendMessage("Valid subcommands: list, new, leave, delete or a valid groupname");
				}else{
					if(args[0].equalsIgnoreCase("list")){
						if(p.hasPermission("skullprotect.group.info")){
							if(groups.size() >= 1){
								String list = "";						
								for(Group g : sortedGroups().values() ){
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
							p.sendMessage(ChatColor.RED + "You dont have the permission to see this information.");
						}
					}else if(args[0].equalsIgnoreCase("new")){
						if(args.length == 2){
							if(p.hasPermission("skullprotect.group.new")){
								boolean alreadytaken = false;
								//if someone tries to trick the system their group just wouldn't be accessible but we can try best to prevent this.
								if(args[1].equalsIgnoreCase("help") || args[1].equalsIgnoreCase("new") || args[1].equalsIgnoreCase("leave") || args[1].equalsIgnoreCase("list") || args[1].equalsIgnoreCase("delete") ){
									alreadytaken = true;
								}
								for(Group g : groups.values() ){
									if(g.getName().equals(args[1])){
										alreadytaken = true;
									}
								}
								if(!alreadytaken){
									newGroup(args[1], p);
									p.sendMessage("Group " + ChatColor.BLUE + args[1] + ChatColor.WHITE + " created.");
								}else{
									p.sendMessage(ChatColor.RED + "This group name has already been taken.");
								}	
							}else{
								p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
							}
						}else{
							p.sendMessage("Pleace specify a valid group name.");
						}
					}else if(args[0].equalsIgnoreCase("leave")){
						if(args.length == 2){
							Group g = null;
							String groupname = args[1] + " ";
							for (Group g_ : groups.values()){
								if(g_.getName().equals(args[1])){
									g = g_;
								}
							}
							if(g != null){
								if( p.hasPermission("skullprotect.group.own.member") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.member") ){
									if(g.ismem(p.getUniqueId())){
										g.remove(p.getUniqueId());
										saveGroup(g);
										p.sendMessage(ChatColor.GOLD + "You" + ChatColor.WHITE + " left the group " + ChatColor.BLUE + groupname);
									}else{
										p.sendMessage(ChatColor.GOLD + "You" + ChatColor.RED + " are not in group " + ChatColor.BLUE + groupname);
									}			
								}else{
									p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
								}	
							}
						}else{
							p.sendMessage("Pleace specify a valid group name.");
						}
					}else if(args[0].equalsIgnoreCase("delete")){
						p.sendMessage("If you want to delete groups, just destroy all group markers and remove all members.");
					}else{
						Group g = null;
						String groupname = args[0] + " ";
						for (Group g_ : groups.values()){
							if(g_.getName().equals(args[0])){
								g = g_;
							}
						}
						if(g != null){
							if(args.length < 2){
								if(p.hasPermission("skullprotect.group.info")){
									p.sendMessage(ChatColor.BLUE + g.getName() + ChatColor.WHITE +  ":");
									String l = "";
									for(UUID puid : g.getOps()){
										l+= ChatColor.GOLD + Bukkit.getPlayer(puid).getName() + ChatColor.WHITE + ", ";
									}
									for(UUID puid : g.getAss()){
										l+= ChatColor.GREEN + Bukkit.getPlayer(puid).getName() + ChatColor.WHITE + ", ";
									}
									for(UUID puid : g.getMem()){
										l+= Bukkit.getPlayer(puid).getName() + ChatColor.WHITE + ", ";
									}
									if(l.length() > 0){
										p.sendMessage("  Members: " + l.substring(0, l.length() - 2) );
									}else{
										p.sendMessage(ChatColor.RED + "  No members. This group is abandoned.");
									}
									
									p.sendMessage("  Land claimed: " + g.getSize() + " sq m" );
									
									String perms = "";
									if(g.getPermission().getPvP()){perms+= ChatColor.GREEN + "PvP";}else{perms+= ChatColor.RED + "PvP";}
									perms+= ChatColor.WHITE + ", ";
									if(g.getPermission().getExplosion()){perms+= ChatColor.GREEN + "Explosion";}else{perms+= ChatColor.RED + "Explosion";}
									perms+= ChatColor.WHITE + ", ";
									if(g.getPermission().getFirespread()){perms+= ChatColor.GREEN + "Firespread";}else{perms+= ChatColor.RED + "Firespread";}
									perms+= ChatColor.WHITE + ", ";
									if(g.getPermission().getMobspawn()){perms+= ChatColor.GREEN + "Mobspawn";}else{perms+= ChatColor.RED + "Mobspawn";}
									perms+= ChatColor.WHITE;
									p.sendMessage("  Permissions: " + perms);	
								}else{
									p.sendMessage(ChatColor.RED + "You dont have the permission to see this information.");
								}
							}else{
								if(args[1].equalsIgnoreCase("rename")){
									if( g.isop(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.operator") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.operator") ){
										if(args.length == 3){
											Group g2 = null;
											for(Group group: groups.values()){
												if(group.getName().equals(args[2])){
													g2 = group;
												}
											}
											if(g2 == null){
												p.sendMessage("Group " + ChatColor.BLUE + groupname + ChatColor.WHITE + " is now called " + ChatColor.BLUE + args[2] + ChatColor.WHITE + ".");
												g.setName(args[2]);
												saveGroup(g);
											}else{
												p.sendMessage("This name has already been taken.");
											}
										}else{
											p.sendMessage("Pleace enter a valid new name.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("add")){
									if( g.isass(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.assistant") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.assistant") ){
										if(args.length == 3){
											UUID rpid = null;
											String playername = args[2];
											for(SPlayer sp_: splayer.values()){
												if(sp_.getPlayer().getName().equals(args[2])){
													rpid = sp_.getPlayer().getUniqueId();	
												}
											}
											if(rpid != null){
												p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Member " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + groupname + ChatColor.WHITE + ".");
												g.addmem(rpid);
												saveGroup(g);
												if(Bukkit.getPlayer(rpid) != null){
													Bukkit.getPlayer(rpid).sendMessage("You have been added to group " + ChatColor.BLUE + groupname + ChatColor.WHITE + ".");
												}
											}else{
												p.sendMessage(ChatColor.RED + "Player " + playername + " not found.");
											}
										}else{
											p.sendMessage("Pleace specify a player name.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("rank")){
									if( g.isop(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.operator") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.operator") ){
										if(args.length == 4){
											UUID rpid = null;
											String playername = args[2];
											for(SPlayer sp_: splayer.values()){
												if(sp_.getPlayer().getName().equals(args[2])){
													rpid = sp_.getPlayer().getUniqueId();	
												}
											}
											if(rpid != null){
												if(g.ismem(rpid)){
													g.remove(rpid);
													if(args[3].equalsIgnoreCase("operator")){
														g.addop(rpid);
														saveGroup(g);
														p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Operator " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
													}else if(args[3].equalsIgnoreCase("assistant")){
														g.addass(rpid);
														saveGroup(g);
														p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Assistant " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
													}else if(args[3].equalsIgnoreCase("member")){
														g.addmem(rpid);
														saveGroup(g);
														p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + " is now " + ChatColor.LIGHT_PURPLE + "Member " + ChatColor.WHITE + "in Group " + ChatColor.BLUE + args[1]);
													}else{
														p.sendMessage(ChatColor.RED + "Invalid Rank");
													}
												}else{
													p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.RED + " is not in Group " + ChatColor.BLUE + args[1]);
												}
											}else{
												p.sendMessage(ChatColor.RED + "Player " + playername + " not found.");
											}
										}else{
											p.sendMessage("Pleace specify a player name and rank.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("remove")){
									if( g.isop(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.operator") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.operator") ){
										if(args.length == 3){
											UUID rpid = null;
											String playername = args[2];
											for(SPlayer sp_: splayer.values()){
												if(sp_.getPlayer().getName().equals(args[2])){
													rpid = sp_.getPlayer().getUniqueId();	
												}
											}
											if(rpid != null){
												if(g.ismem(rpid)){
													g.remove(rpid);
													saveGroup(g);
													p.sendMessage(ChatColor.GOLD + playername + ChatColor.WHITE + " was removed from group " + ChatColor.BLUE + groupname);
												}else{
													p.sendMessage(ChatColor.GOLD + playername + ChatColor.RED + " is not in group " + ChatColor.BLUE + groupname);
												}
											}else{
												p.sendMessage(ChatColor.RED + "Player " + playername + " not found.");
											}
										}else{
											p.sendMessage("Pleace specify a player name.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("toggle")){
									if( g.isop(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.operator") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.operator") ){
										if(args.length == 3){
											if(args[2].equalsIgnoreCase("pvp")){
												if(g.getPermission().getPvP()){
													g.getPermission().setPvP(false);
													p.sendMessage("PvP is now disabled.");
												}else{
													g.getPermission().setPvP(true);
													p.sendMessage("PvP is now enabled.");
												}
											}else if(args[2].equalsIgnoreCase("exp") || args[2].equalsIgnoreCase("explosion")){
												if(g.getPermission().getExplosion()){
													g.getPermission().setExplosion(false);
													p.sendMessage("Explosions are now disabled.");
												}else{
													g.getPermission().setExplosion(true);
													p.sendMessage("Explosions are now enabled.");
												}
											}else if(args[2].equalsIgnoreCase("fire") || args[2].equalsIgnoreCase("firespread")){
												if(g.getPermission().getFirespread()){
													g.getPermission().setFirespread(false);
													p.sendMessage("Firespread is now disabled.");
												}else{
													g.getPermission().setFirespread(true);
													p.sendMessage("Firespread is now enabled.");
												}
											}else if(args[2].equalsIgnoreCase("mobs") || args[2].equalsIgnoreCase("mobspawn")){
												if(g.getPermission().getMobspawn()){
													g.getPermission().setMobspawn(false);
													p.sendMessage("Mobspawn is now disabled.");
												}else{
													g.getPermission().setMobspawn(true);
													p.sendMessage("Mobspawn is now enabled.");
												}
											}else{
												p.sendMessage("Valid permissions: pvp, exp/explosion, fire/firespread, mobs/mobspawn");
											}
											saveGroup(g);
										}else{
											p.sendMessage("Pleace specify a permission.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("prices")){
									if( p.hasPermission("skullprotect.group.own.member") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.member") ){
										if(usingeconomy){				
											String taxstring = tax.replace("plots", groups.get( g.getId() ).getSize()+"").replace("money", Double.toString(economy.bankBalance("%" + g.getId() ).balance) );
											int tax_ = (int) Math.floor(calculate( taxstring) );
											String landmarkpricestring = landmarkprice.replace("plots", groups.get( g.getId() ).getSize()+"").replace("money", Double.toString(economy.bankBalance("%" + g.getId() ).balance) );
											int landmarkprice_ = (int) Math.floor(calculate( landmarkpricestring) );
											String claimpricestring = claimprice.replace("plots", groups.get( g.getId() ).getSize()+"").replace("money", Double.toString(economy.bankBalance("%" + g.getId() ).balance) );
											int claimprice_ = (int) Math.floor(calculate( claimpricestring) );
											String maxclaimstring = maxclaim.replace("plots", groups.get( g.getId() ).getSize()+"").replace("money", Double.toString(economy.bankBalance("%" + g.getId() ).balance) );
											int maxclaim_ = (int) Math.floor(calculate( maxclaimstring) );
											
											p.sendMessage("Prices for " + ChatColor.BLUE + g.getName() + ChatColor.WHITE + ":");
											p.sendMessage("Tax: " + ChatColor.GOLD + tax_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + tax + ChatColor.WHITE + ")");
											p.sendMessage("Landmark price: " + ChatColor.GOLD + landmarkprice_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + landmarkprice + ChatColor.WHITE + ")");
											p.sendMessage("Claim price: " + ChatColor.GOLD + claimprice_ + ChatColor.WHITE + " "+ economy.currencyNamePlural() + " (" + ChatColor.GREEN + claimprice + ChatColor.WHITE + ")");
											p.sendMessage("Max claim: " + ChatColor.GOLD + maxclaim_ + ChatColor.WHITE + " plots (" + ChatColor.GREEN + maxclaim + ChatColor.WHITE + ")");	
										}else{
											p.sendMessage(ChatColor.RED + "Economy support is disabled.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("deposit")){
									
										if(args.length == 3){
											if(usingeconomy){
												try{  
													Double.parseDouble(args[2]);  
												}catch(NumberFormatException nfe){  
													args[2] = "-1"; 
												}
												if(Double.parseDouble(args[2]) >= 0){
													EconomyResponse deposit = economy.withdrawPlayer(p, Double.parseDouble(args[2]) );
													if(deposit.transactionSuccess()){
														economy.bankDeposit("%" + g.getId(), Double.parseDouble(args[2]) );
														p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + economy.currencyNamePlural() + ChatColor.GREEN + " have been transferred to " + ChatColor.BLUE + args[1] + ChatColor.GREEN + ".");
													}else{
														p.sendMessage(ChatColor.RED + "You dont have enough money.");
													}
												}else{	
													p.sendMessage(ChatColor.RED + "Invalid value.");
												}
											}else{
												p.sendMessage(ChatColor.RED + "Economy support is disabled.");
											}
										}else{
											p.sendMessage("Pleace specify a valid amount.");
										}
									
								}else if(args[1].equalsIgnoreCase("withdraw")){
									if( g.isop(p.getUniqueId()) || p.hasPermission("skullprotect.group.own.operator") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.operator") ){
										if(args.length == 3){
											if(usingeconomy){
												try{  
													Double.parseDouble(args[2]);  
												}catch(NumberFormatException nfe){  
													args[2] = "-1"; 
												}
												if(Double.parseDouble(args[2]) >= 0){		
													EconomyResponse withdraw = economy.bankWithdraw("%" + g.getId(), Double.parseDouble(args[2]) );
													if(withdraw.transactionSuccess()){
														economy.depositPlayer(p, Double.parseDouble(args[2]) );
														 p.sendMessage(ChatColor.GOLD + args[2] + ChatColor.WHITE + economy.currencyNamePlural() + ChatColor.GREEN + " from group " + ChatColor.BLUE + args[1] + ChatColor.GREEN + " have been transferred to you.");
													}else{
														p.sendMessage(ChatColor.RED + "This group has not enough money.");
													}
												}else{	
													p.sendMessage(ChatColor.RED + "Invalid value.");
												}
											}else{
												p.sendMessage(ChatColor.RED + "Economy support is disabled.");
											}
										}else{
											p.sendMessage("Pleace specify a valid amount.");
										}
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("leave")){
									if( p.hasPermission("skullprotect.group.own.member") && g.ismem(p.getUniqueId()) || p.hasPermission("skullprotect.group.foreign.member") ){
										if(g.ismem(p.getUniqueId())){
											g.remove(p.getUniqueId());
											saveGroup(g);
											p.sendMessage(ChatColor.GOLD + "You" + ChatColor.WHITE + " left the group " + ChatColor.BLUE + groupname);
										}else{
											p.sendMessage(ChatColor.GOLD + "You" + ChatColor.RED + " are not in group " + ChatColor.BLUE + groupname);
										}			
									}else{
										p.sendMessage(ChatColor.RED + "You dont have the permission to do this.");
									}
								}else if(args[1].equalsIgnoreCase("delete")){
									p.sendMessage("If you want to delete groups, just destroy all group markers and remove all members.");
								}else{
									p.sendMessage("Valid subcommands: add, deposit, leave, prices, rank, remove, rename, toggle, withdraw");
								}
							}
						}else{
							p.sendMessage(ChatColor.RED + "Group " + groupname + "not found.");
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
