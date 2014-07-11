package de.bruuff.skullprotect;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Skull;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class EventListener implements Listener {
	skullprotect main = null;
	String u_head = "";
	String u_name = "";
	String g_head = "";
	
	public EventListener(skullprotect plugin){
		main = plugin;
		u_head = main.getConfig().getString(main.getDescription().getName() + ".unowned.head");
		u_name = main.getConfig().getString(main.getDescription().getName() + ".unowned.name");
		g_head = main.getConfig().getString(main.getDescription().getName() + ".groups.head");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		Player player = event.getPlayer();
		
		if(main.splayer.get(player.getUniqueId()) == null){
			main.splayer.put(player.getUniqueId() , new SPlayer(Bukkit.getOfflinePlayer(player.getUniqueId() ), 0, new ArrayList<Landmark>(), new Perm(true,true,true,true) ) );
			main.savePlayer(main.splayer.get(player.getUniqueId()));
		}

	}
	
	private boolean validAction(Player player, Location location){
		Owner owner = blockOwner(location,false);
		boolean valid = false;
		if( owner.getType().equals("PLAYER") ){
			if(main.splayer.get(player.getUniqueId() ) != null ){
				if(player.getUniqueId().equals(owner.getUUID()) ){
					valid = true;
				}
			}
		}
		if( owner.getType().equals("GROUP") ){
			if(main.groups.get(owner.getOwnerId()) != null ){
				if(main.groups.get(owner.getOwnerId()).ismem(player.getUniqueId()) ){
					valid = true;
				}
			}		
		}
		if( owner.getType().equals("UNOWNED") ){
			valid = true;
		}
		return valid;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent event){
		Player player = event.getPlayer();
		if(event.hasBlock()){
			Block block = event.getClickedBlock();
			boolean valid = validAction(player,block.getLocation());
			if(!player.hasPermission("skullprotect.interact.own")){
				valid = false;		
			}
			if(player.hasPermission("skullprotect.interact.foreign")){
				valid = true;
			}
			//Cancel if no permission
			if(!valid){
				if(event.getAction().name().equals("RIGHT_CLICK_BLOCK")){
					if(Arrays.asList(new String[] {"NOTE_BLOCK", "BED_BLOCK", "TNT", "WOODEN_DOOR", "LEVER", "IRON_DOOR_BLOCK", "REDSTONE_ORE", "GLOWING_REDSTONE_ORE", "STONE_BUTTON", "JUKEBOX", "CAKE_BLOCK", "DIODE_BLOCK_OFF", "DIODE_BLOCK_ON", "LOCKED_CHEST", "TRAP_DOOR", "FENCE_GATE", "CAULDRON", "ENDER_PORTAL_FRAME", "DRAGON_EGG", "BEACON ", "DROPPER", "STATIONARY_LAVA", "STATIONARY_WATER"}).contains(block.getType().name()) ){
						if(!main.interact_mats.contains(block.getType())){
							player.sendMessage(ChatColor.RED + "You don't have the permission to interact here.");
							event.setCancelled(true);
						}
					}
				}
				if(event.getAction().name().equals("PHYSICAL")){
					if(!Arrays.asList(new String[] {"STONE_PLATE", "WOOD_PLATE", "REDSTONE_ORE", "GLOWING_REDSTONE_ORE" }).contains(block.getType().name()) ){
						if(!main.interact_mats.contains(block.getType())){
							player.sendMessage(ChatColor.RED + "You don't have the permission to interact here.");
							event.setCancelled(true);
						}
					}
				}
			}
		}	
	}
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent event){
		Player player = event.getPlayer();
		Entity entity = event.getRightClicked();
		boolean valid = validAction(player,entity.getLocation());
		if(!player.hasPermission("skullprotect.interact.own")){
			valid = false;		
		}
		if(player.hasPermission("skullprotect.interact.foreign")){
			valid = true;
		}
		//Cancel if no permission
		if(!valid){
			if(!main.interact_ents.contains(entity.getType())){
				player.sendMessage(ChatColor.RED + "You don't have the permission to interact with this.");
				event.setCancelled(true);
			}	
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event){
		System.out.print(event.getDamager().toString());
		if(event.getDamager() instanceof Player){
			
			Player player = (Player) event.getDamager();
			//Permission PvP
			if(event.getEntity() instanceof Player){
				Player victim = (Player) event.getEntity();
				
				Owner owner = blockOwner (victim.getLocation(),false);
				if( owner.getType().equals("GROUP")  ){
	            	if(!main.groups.get(owner.getOwnerId()).getPermission().getPvP() ){
						event.setCancelled(true);
					}
	            }
	            if( owner.getType().equals("PLAYER")  ){
	            	if(!main.splayer.get(owner.getUUID() ).getPermission().getPvP() ){
						event.setCancelled(true);
					}
	            }
				
				
			}else{
				
				Entity entity = event.getEntity();
				boolean valid = validAction(player,entity.getLocation());
				if(!player.hasPermission("skullprotect.interact.own")){
					valid = false;		
				}
				if(player.hasPermission("skullprotect.interact.foreign")){
					valid = true;
				}
				//Cancel if no permission
				if(!valid){
					if(!main.interact_ents.contains(entity.getType())){
						player.sendMessage(ChatColor.RED + "You don't have the permission to damage this.");
						event.setCancelled(true);
					}	
				}
			}

		}else if(event.getDamager() instanceof Arrow){
			Arrow arrow = (Arrow) event.getDamager();
			if(arrow.getShooter() instanceof Player){
				if(event.getEntity() instanceof Player){
					Player victim = (Player) event.getEntity();
					
					Owner owner = blockOwner (victim.getLocation(),false);
					if( owner.getType().equals("GROUP")  ){
		            	if(!main.groups.get(owner.getOwnerId()).getPermission().getPvP() ){
							event.setCancelled(true);
						}
		            }
		            if( owner.getType().equals("PLAYER")  ){
		            	if(!main.splayer.get(owner.getUUID() ).getPermission().getPvP() ){
							event.setCancelled(true);
						}
		            }

				}
			}

		}
	}
	@EventHandler
	public void onVehicleDestroy(VehicleDestroyEvent event){
		if(event.getAttacker() instanceof Player){
			Player player = (Player) event.getAttacker();
			Vehicle vehicle = event.getVehicle();
			boolean valid = validAction(player,vehicle.getLocation());
			if(!player.hasPermission("skullprotect.interact.own")){
				valid = false;		
			}
			if(player.hasPermission("skullprotect.interact.foreign")){
				valid = true;
			}
			//Cancel if no permission
			if(!valid){
				if(!main.interact_ents.contains(vehicle.getType())){
					player.sendMessage(ChatColor.RED + "You don't have the permission to destroy this.");
					event.setCancelled(true);
				}	
			}
		}
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event){
		HumanEntity hi = event.getPlayer();
		
		if(hi.getType().equals(EntityType.PLAYER)){
			Player player = (Player) hi;
			InventoryHolder ih = event.getInventory().getHolder();
			Location location = null;
			boolean override = false;
			if(ih instanceof BlockState) {
				if(main.interact_mats.contains( ((BlockState) ih).getType() ) ){
					override = true;
				}
				location = ((BlockState) ih).getLocation();
			}else if(ih instanceof Entity) {
				if(main.interact_ents.contains( ((Entity) ih).getType() ) ){
					override = true;
				}
				if( ((Entity) ih).equals(player) ){
					override = true;
				}
				location = ((Entity) ih).getLocation();
			}else if(ih instanceof DoubleChest) {
				if(main.interact_mats.contains( Material.CHEST ) ){
					override = true;
				}
				location = ((DoubleChest) ih).getLocation();
			}else{
				System.out.print("[skullprotect] Error while checking InventoryOpenEvent: neither entity nor doublechest.");
			}
			if(location != null){
				boolean valid = validAction(player,location);

				if(!player.hasPermission("skullprotect.interact.own")){
					valid = false;		
				}
				if(player.hasPermission("skullprotect.interact.foreign")){
					valid = true;
				}
				if(!valid){
					if(!override){
						player.sendMessage(ChatColor.RED + "You cant open this inventory here.");
						event.setCancelled(true);
					}
				}
			}
			
		}
		
	}
	
	
//Prevent creation of Landmarks by Pistons
	@EventHandler
	public void onBlockPull(BlockPistonRetractEvent event){	
		if( event.getRetractLocation().getBlock().getType().name().equals("FENCE")) {
			if( event.getRetractLocation().getBlock().getRelative(0,1,0).getType().name().equals("SKULL"))  {
				event.setCancelled(true);
			}
			if( event.getBlock().getRelative(event.getDirection()).getRelative(0,1,0).getType().name().equals("SKULL")) {
				event.setCancelled(true);
			}
		}
	}
	@EventHandler
	public void onBlockPush(BlockPistonExtendEvent event){	
		for (Block b : event.getBlocks()){
			if( b.getType().name().equals("FENCE")) {
				if( b.getRelative(0,1,0).getType().name().equals("SKULL"))  {
					event.setCancelled(true);
				}
				if( b.getRelative(event.getDirection()).getRelative(0,1,0).getType().name().equals("SKULL")) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	private Owner blockOwner(Location location, boolean onedown){
		TreeMap<Integer,Owner> owners = columnOwner(location.getWorld(),location.getBlockX(),location.getBlockZ());
		Owner highest = owners.get(0);
		for (Owner owner : owners.values()){
			if(onedown){
				if(location.getY() > owner.getHeight() ){
					highest = owner;
				}
			}else{
				if(location.getY() >= owner.getHeight() ){
					highest = owner;
				}
			}
		}
		return highest;
	}
	private Owner getMarker(Location location){
		TreeMap<Integer,Owner> owners = columnOwner(location.getWorld(),location.getBlockX(),location.getBlockZ());
		return owners.get(location.getBlockY());
	}
	
	private TreeMap<Integer,Owner> columnOwner(World world, int x, int z){
		Location loc = new Location(world, x, 0, z);
		Chunk chunk = world.getChunkAt(loc);
		File dir = new File("plugins/skullprotect/chunks/" + world.getName() + "/");
		dir.mkdirs();
		File file = new File(dir, chunk.getX() + "_" + chunk.getZ() + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);

		TreeMap<Integer, Owner> owners = new TreeMap<Integer, Owner>();
		if( yamlFile.contains("x"+ Math.abs(chunk.getX()*16 - x) + ".z" + Math.abs(chunk.getZ()*16 - z) ) ){
			String ownerstring = yamlFile.getString("x"+ Math.abs(chunk.getX()*16 - x) + ".z" + Math.abs(chunk.getZ()*16 - z) );
			
			List<String> segments = Arrays.asList(ownerstring.split(",")) ;
			owners.put(0,new Owner(0, "UNOWNED"));
			
			for(String string : segments){
				if(string.split(":").length >= 2){
					if( string.split(":")[1].startsWith("p") ){
						owners.put(Integer.parseInt(string.split(":")[0]),new Owner(Integer.parseInt(string.split(":")[0]), UUID.fromString(string.split(":")[1].substring(1)), "PLAYER"));
					}else if( string.split(":")[1].startsWith("g") ){
						owners.put(Integer.parseInt(string.split(":")[0]),new Owner(Integer.parseInt(string.split(":")[0]), Integer.parseInt(string.split(":")[1].substring(1)), "GROUP"));	
					}else{
						owners.put(Integer.parseInt(string.split(":")[0]),new Owner(Integer.parseInt(string.split(":")[0]), "UNOWNED"));
					}
				}
				
			}

		}else{
			owners.put(0,new Owner(0, "UNOWNED"));
		}

		return owners;
	}
	
	
	private boolean addblockOwner(Block block, Owner owner_){
		Chunk chunk = block.getChunk();
		File dir = new File("plugins/skullprotect/chunks/" + block.getWorld().getName() + "/");
		dir.mkdirs();
		File file = new File(dir, chunk.getX() + "_" + chunk.getZ() + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);

		for (int x_ = 0; x_ <= 15; x_++) {
			if(!yamlFile.contains("x" + x_)){
				yamlFile.createSection("x" + x_);
			}
			for (int z_ = 0; z_ <= 15; z_++) {
				if(!yamlFile.contains("x"+ x_ +".z" + z_)){
					yamlFile.createSection("x"+ x_ +".z" + z_);
					yamlFile.set("x"+ x_ +".z" + z_, "");
				}
			}
		}
		String old = yamlFile.getString("x"+ Math.abs(chunk.getX()*16 - block.getX()) + ".z" + Math.abs(chunk.getZ()*16 - block.getZ()) );
		String add = "";
		if(owner_.getType().equals("PLAYER")){
			add = owner_.getHeight() + ":p" + owner_.getUUID().toString();
		}else if(owner_.getType().equals("GROUP")){
			add = owner_.getHeight() + ":g" + owner_.getOwnerId();
		}else{
			add = owner_.getHeight() + ":0";
		}
		
		if(old == ""){
			yamlFile.set("x"+ Math.abs(chunk.getX()*16 - block.getX()) + ".z" + Math.abs(chunk.getZ()*16 - block.getZ()), add );
		}else{
			yamlFile.set("x"+ Math.abs(chunk.getX()*16 - block.getX()) + ".z" + Math.abs(chunk.getZ()*16 - block.getZ()), old += "," + add );
		}

		try {
			yamlFile.save(file);
			return true;
		} catch(IOException e) {e.printStackTrace();}
		return false;
	}

	private boolean removeblockOwner(Block block){
		
		TreeMap<Integer,Owner> owners_old = columnOwner(block.getWorld(),block.getX(),block.getZ());
		Chunk chunk = block.getChunk();
		File dir = new File("plugins/skullprotect/chunks/" + block.getWorld().getName() + "/");
		dir.mkdirs();
		File file = new File(dir, chunk.getX() + "_" + chunk.getZ() + ".yml");
		
		YamlConfiguration yamlFile = YamlConfiguration.loadConfiguration(file);

		for (int x_ = 0; x_ <= 15; x_++) {
			if(!yamlFile.contains("x" + x_)){
				yamlFile.createSection("x" + x_);
			}
			for (int z_ = 0; z_ <= 15; z_++) {
				if(!yamlFile.contains("x"+ x_ +".z" + z_)){
					yamlFile.createSection("x"+ x_ +".z" + z_);
					yamlFile.set("x"+ x_ +".z" + z_, "");
				}
			}
		}
		System.out.print("x"+ Math.abs(chunk.getX()*16 - block.getX()) + ".z" + Math.abs(chunk.getZ()*16 - block.getZ()));
		String add = "";
		for (Owner owner_ : owners_old.values()){
			if( !(owner_.getHeight() == block.getY()) && (owner_.getHeight() > 0) ){
				add += ",";
				if(owner_.getType().equals("PLAYER")){
					add += owner_.getHeight() + ":p" + owner_.getUUID().toString();
				}else if(owner_.getType().equals("GROUP")){
					add += owner_.getHeight() + ":g" + owner_.getOwnerId();
				}else{
					add += owner_.getHeight() + ":0";
				}	
			}	
		}
		if(add.equals("")){add += ",";}
		yamlFile.set("x"+ Math.abs(chunk.getX()*16 - block.getX()) + ".z" + Math.abs(chunk.getZ()*16 - block.getZ()), add.substring(1) );

		try {
			yamlFile.save(file);
			return true;
		} catch(IOException e) {e.printStackTrace();}
		return false;
	}
	
	//Permission: PvP inside onEntityDamage
	
	//Permission: Explosion
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event){	
		List<Block> destroyed = event.blockList();
        Iterator<Block> it = destroyed.iterator();
        while (it.hasNext()) {
        	Block block = it.next();
            Owner owner = blockOwner (block.getLocation(),false);
            if(block.getType().equals(Material.SKULL)){
            	it.remove();
            }else if(block.getType().equals(Material.FENCE) || block.getRelative(0, 1, 0).getType().equals(Material.SKULL)){
            	it.remove();
        	}else{	
            	if( owner.getType().equals("GROUP")  ){
            		if(!main.groups.get(owner.getOwnerId()).getPermission().getExplosion() ){
						it.remove();
					}
            	}
            	if( owner.getType().equals("PLAYER")  ){
            		if(!main.splayer.get(owner.getUUID() ).getPermission().getExplosion() ){
            			it.remove();
					}
            	}
			}
        }
	}
	
	//Permission: Firespread
	@EventHandler
	public void onBlockSpread(BlockSpreadEvent event){	
		if(event.getNewState().getType().equals(Material.FIRE)){
			BlockState newblock = event.getNewState();
			Owner owner = blockOwner (newblock.getLocation(),false);
			if( owner.getType().equals("GROUP")  ){
            	if(!main.groups.get(owner.getOwnerId()).getPermission().getFirespread() ){
					event.setCancelled(true);
				}
            }
            if( owner.getType().equals("PLAYER")  ){
            	if(!main.splayer.get(owner.getUUID() ).getPermission().getFirespread() ){
            		event.setCancelled(true);
    			}
				
            }	
		}
	}
	
	//Permission: Firespread
		@EventHandler
		public void onBlockIgnite(BlockIgniteEvent event){	
			if(event.getCause().equals(IgniteCause.SPREAD)){
				Block block = event.getBlock();
				Owner owner = blockOwner (block.getLocation(),false);
				if( owner.getType().equals("GROUP")  ){
	            	if(!main.groups.get(owner.getOwnerId()).getPermission().getFirespread() ){
						event.setCancelled(true);
					}
	            }
	            if( owner.getType().equals("PLAYER")  ){
	            	if(!main.splayer.get(owner.getUUID() ).getPermission().getFirespread() ){
	            		event.setCancelled(true);
	    			}
					
	            }	
			}
		}
		
		//Permission: Firespread
		@EventHandler
		public void onBlockBurn(BlockBurnEvent event){
			Block block = event.getBlock();
			Owner owner = blockOwner (block.getLocation(),false);
			if( owner.getType().equals("GROUP")  ){
	           	if(!main.groups.get(owner.getOwnerId()).getPermission().getFirespread() ){
					event.setCancelled(true);
				}
	        }
	        if( owner.getType().equals("PLAYER")  ){
	           	if(!main.splayer.get(owner.getUUID() ).getPermission().getFirespread() ){
	           		event.setCancelled(true);
	    		}		
	        }	
		}
	
	//Permission: Mobspawn
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event){	
		if(event.getEntity() instanceof Monster){
			Owner owner = blockOwner (event.getEntity().getLocation(),false);
			if( owner.getType().equals("GROUP")  ){
            	if(!main.groups.get(owner.getOwnerId()).getPermission().getMobspawn() ){
					event.setCancelled(true);
				}
            }
            if( owner.getType().equals("PLAYER")  ){
            	if(!main.splayer.get(owner.getUUID() ).getPermission().getMobspawn() ){
            		event.setCancelled(true);
				}
            }	
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){	
		Player player = event.getPlayer();
		Block block = event.getBlock();
		ItemStack item = event.getItemInHand();
		
		boolean valid = validAction(player,block.getLocation());

		if(player.hasPermission("skullprotect.place.blocks")){
			valid = true;
		}
		if(valid){
			if(event.getBlock().getType().name().equals("SKULL")){
				boolean myjob = false;
				if(item.getType().name().equals("SKULL_ITEM")){
					if(item.hasItemMeta()){
						SkullMeta sim = (SkullMeta) item.getItemMeta();	
						if(sim.hasDisplayName()){
							if(sim.getDisplayName().startsWith("Marker:")){
								myjob = true;
							}
						}
					}
				}
				if(myjob){

				//Placing MARKER ?
					if(!block.getRelative(0, -1, 0).getType().name().equals("FENCE")){
						SkullMeta sim = (SkullMeta) item.getItemMeta();	
									
						Owner newowner = null;
						if(sim.getDisplayName().startsWith("Marker: " + ChatColor.YELLOW)){

							if(player.hasPermission("skullprotect.place.skull.foreign")){	
								if(!player.getName().equals(sim.getOwner())){
									for (SPlayer sp_ : main.splayer.values()){
										if(sp_.getPlayer().getName().equals(sim.getOwner())){
											newowner = new Owner(block.getY(), sp_.getPlayer().getUniqueId(), "PLAYER");
										}
									}
								}else{
									newowner = new Owner(block.getY(), player.getUniqueId(), "PLAYER");
								}	
							}else if( sim.getOwner().equals(player.getName()) && player.hasPermission("skullprotect.place.skull.own")){
								newowner = new Owner(block.getY(), player.getUniqueId(), "PLAYER");
							}else{
								player.sendMessage(ChatColor.RED + "You can't use this skull.");
							}
	
						}else if(sim.getDisplayName().startsWith("Marker: " + ChatColor.BLUE)){
							for (Group g : main.groups.values()){
								if(g.getName().equals(sim.getDisplayName().substring(10))){
									if(player.hasPermission("skullprotect.place.skull.foreign")){
										newowner = new Owner(block.getY(), g.getId() , "GROUP");
									}else if( g.ismem(player.getUniqueId()) && player.hasPermission("skullprotect.place.skull.own")){
										newowner = new Owner(block.getY(), g.getId() , "GROUP");
									}else{
										player.sendMessage(ChatColor.RED + "You can't use this skull.");
									}
								}
							}
						}else{
							newowner = new Owner(block.getY(), "UNOWNED");
						}
						if(newowner != null){
							if(main.usingeconomy){
								//PLAYER
								if( newowner.getType().equals("PLAYER") ){
									boolean inreach = false;
									
									for(Landmark lm_ : main.splayer.get( player.getUniqueId() ).getLandmarks()){
										if( (Math.abs((block.getX() - lm_.getX())) <= main.lm_radius) && (Math.abs((block.getZ() - lm_.getZ())) <= main.lm_radius) ){
											inreach = true;
										}
									}
									
									if(inreach){
										String maxclaimstring = main.maxclaim.replace("plots", main.splayer.get( player.getUniqueId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.getBalance(player)) );
										double maxclaim_ = Math.floor(main.calculate( maxclaimstring) );
										String claimpricestring = main.claimprice.replace("plots", main.splayer.get( player.getUniqueId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.getBalance(player)) );
										double claimprice_ = Math.floor(main.calculate( claimpricestring) );
										if(skullprotect.economy.has(player, claimprice_)){
											if(main.splayer.get( player.getUniqueId() ).getSize() <= (maxclaim_ - 1) ){
												if(addblockOwner(block,newowner )){
													main.splayer.get( player.getUniqueId() ).sizeplusone();
													main.savePlayer(main.splayer.get( player.getUniqueId() ));
													skullprotect.economy.withdrawPlayer(player, claimprice_);
													player.sendMessage("("  + claimprice_ + skullprotect.economy.currencyNamePlural() + " paid) You now own " + main.splayer.get( player.getUniqueId() ).getSize() + " square meter.");
													
												}else{
													player.sendMessage("Maximum skulls on top of each other reached.");
													event.setCancelled(true);
												}
												
											}else{
												player.sendMessage("You have reached your maximum plot size.");
												event.setCancelled(true);	
											}
										}else{
											player.sendMessage("You dont have enough money to buy this.");
											event.setCancelled(true);
										}
										
									}else{
										player.sendMessage("No Landmark in reach.");
										event.setCancelled(true);
									}
								//GROUP	
								}else if( newowner.getType().equals("GROUP") ){
									boolean inreach = false;
									for(Landmark lm_ : main.groups.get( newowner.getOwnerId() ).getLandmarks()){
										if( (Math.abs((block.getX() - lm_.getX())) <= main.lm_radius) && (Math.abs((block.getZ() - lm_.getZ())) <= main.lm_radius) ){
											inreach = true;
										}
									}
									if(inreach){
										String maxclaimstring = main.maxclaim.replace("plots", main.groups.get( newowner.getOwnerId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.bankBalance("%" + newowner.getOwnerId()).balance) );
										double maxclaim_ = Math.floor(main.calculate( maxclaimstring) );
										String claimpricestring = main.claimprice.replace("plots", main.groups.get( newowner.getOwnerId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.bankBalance("%" + newowner.getOwnerId()).balance) );
										double claimprice_ = Math.floor(main.calculate( claimpricestring) );
		
										if(skullprotect.economy.bankBalance( "%" + newowner.getOwnerId() ).balance >= claimprice_){
											if(main.groups.get( newowner.getOwnerId() ).getSize() <= (maxclaim_ - 1) ){
		
												if(addblockOwner(block,newowner )){
													Group g = main.groups.get( newowner.getOwnerId() );
													g.setSize(g.getSize() + 1);
													main.saveGroup(g);
													skullprotect.economy.bankWithdraw( "%" + newowner.getOwnerId(), claimprice_);
		
													player.sendMessage("("  + claimprice_ + skullprotect.economy.currencyNamePlural() + " paid) Group "+ g.getName() +" now owns " + g.getSize() + " square meter.");
		
												}else{
													player.sendMessage("Maximum skulls on top of each other reached.");
													event.setCancelled(true);
												}						
											}else{
												player.sendMessage("This group has reached its maximum plot size.");
												event.setCancelled(true);
											}
										}else{
											player.sendMessage("Your group does not have enough money to buy this.");
											event.setCancelled(true);
										}	
									}else{
										player.sendMessage("No Landmark in reach.");
										event.setCancelled(true);
									}	
								//UNOWNED
								}else{
									if(!addblockOwner(block,new Owner(block.getY(), "UNOWNED" ) )){
										player.sendMessage("Maximum skulls on top of each other reached.");
										event.setCancelled(true);
									}
								}
				
							}else{
								if( newowner.getType().equals("PLAYER") ){
									
									if(addblockOwner(block,newowner )){
										main.splayer.get( player.getUniqueId() ).sizeplusone();
										main.savePlayer(main.splayer.get( player.getUniqueId() ));
										player.sendMessage("You now own " + main.splayer.get( player.getUniqueId() ).getSize() + " square meter.");
									
									}else{
										player.sendMessage("Maximum skulls on top of each other reached.");
									}
								}else if( newowner.getType().equals("GROUP") ){
									if(addblockOwner(block,newowner )){
										Group g = main.groups.get( newowner.getOwnerId() );
										g.setSize(g.getSize() + 1);
										main.saveGroup(g);
										player.sendMessage("Group "+ g.getName() +" now owns " + g.getSize() + " square meter.");
									}else{
										player.sendMessage("Maximum skulls on top of each other reached.");
									}
										
								}else{
									if(!addblockOwner(block,new Owner(block.getY(), "UNOWNED" ) )){
										player.sendMessage("Maximum skulls on top of each other reached.");
									}
								}
							}
						}else{
							player.sendMessage("Couldn't find owner. Has the name changed?");
						}

				//Placing LANDMARK?		
					}else{
						Block skullblock = block;
						SkullMeta sim = (SkullMeta) item.getItemMeta();	
						if(sim.getDisplayName().startsWith("Marker: " + ChatColor.GREEN)){
							event.setCancelled(true);
							player.sendMessage("What should that mean?");
						}else if(sim.getDisplayName().startsWith("Marker: " + ChatColor.BLUE)){
							for (Group g : main.groups.values()){
								if(g.getName().equals(sim.getDisplayName().substring(10))){
									
									if(main.usingeconomy){
										
										String landmarkstring = main.landmarkprice.replace("plots", g.getSize()+"").replace("money", Double.toString(skullprotect.economy.bankBalance("%" + g.getId()).balance) );
										double landmarkprice_ = Math.floor(main.calculate( landmarkstring) );
										if(skullprotect.economy.bankBalance( "%" + g.getId() ).balance >= landmarkprice_){
											skullprotect.economy.bankWithdraw( "%" + g.getId(), landmarkprice_);
											Landmark lm = new Landmark(skullblock);
											g.getLandmarks().add(lm);
											main.saveGroup(g);
											player.sendMessage("("  + landmarkprice_ + skullprotect.economy.currencyNamePlural() + " paid) Landmark for group " + ChatColor.BLUE + sim.getDisplayName().substring(10) + ChatColor.WHITE + " registered at [" + lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ() +"]");
										}
									}else{
										Landmark lm = new Landmark(skullblock);
										g.getLandmarks().add(lm);
										main.saveGroup(g);
										player.sendMessage("Landmark for group " + ChatColor.BLUE + sim.getDisplayName().substring(10) + ChatColor.WHITE + " registered at [" + lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ() +"]");
									}	
								}
							}								
						}else{
							if(main.usingeconomy){
								
								String landmarkpricestring = main.landmarkprice.replace("plots", main.splayer.get( player.getUniqueId() ).getSize()+"").replace("money", Double.toString(skullprotect.economy.getBalance(player)) );
								double landmarkprice_ = Math.floor(main.calculate( landmarkpricestring) );
								if(skullprotect.economy.has(player, landmarkprice_)){
									skullprotect.economy.withdrawPlayer(player, landmarkprice_);
									Landmark lm = new Landmark(skullblock);
									main.splayer.get( player.getUniqueId() ).getLandmarks().add(lm);
									main.savePlayer(main.splayer.get( player.getUniqueId() ));
									player.sendMessage("("  + landmarkprice_ + skullprotect.economy.currencyNamePlural() + " paid) Private landmark registered at [" + lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ() +"]");
								
								}
							}else{
								Landmark lm = new Landmark(skullblock);
								main.splayer.get( player.getUniqueId() ).getLandmarks().add(lm);
								main.savePlayer(main.splayer.get( player.getUniqueId() ));
								player.sendMessage("Private landmark registered at [" + lm.getWorld().getName() + "," + lm.getX() + "," + lm.getY() + "," + lm.getZ() +"]");
							}
						}

					}
					
				}else{
					player.sendMessage("This isn't a skullprotect skull! It won't interfere.");
				}
				
			}else if(event.getBlock().getType().name().equals("FENCE") && event.getBlock().getRelative(0, 1, 0).getType().name().equals("SKULL")){
				player.sendMessage("dont place landmarks that way");
				event.setCancelled(true);
			}					
		}else{
			player.sendMessage(ChatColor.RED + "You dont have the permission to build here.");
			event.setCancelled(true);
		}

	}
	
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event){
		if(event.getBlock().getType().name() == "SKULL" ){
			BlockState blockstate = event.getBlock().getState();
			Skull skull = (Skull) blockstate;
			if(skull.getSkullType().name().equals("PLAYER") && skull.getOwner() != null){
				event.setInstaBreak(true);	
			}
		}
	}
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){	
		Player player = event.getPlayer();
		Block block = event.getBlock();
		boolean valid = validAction(player,block.getLocation());

		if(player.hasPermission("skullprotect.break.blocks")){
			valid = true;
		}
		if(valid){
			if(event.getBlock().getType().name().equals("SKULL")){

				//Breaking MARKER?
				if(!block.getRelative(0, -1, 0).getType().name().equals("FENCE")){
					Owner owner = getMarker(block.getLocation());
					if(owner != null){
						if( owner.getType().equals("PLAYER") ){
							main.splayer.get(owner.getUUID()).sizeminusone();
							main.savePlayer(main.splayer.get( player.getUniqueId() ));
							removeblockOwner(block);
							player.sendMessage("You now own " + main.splayer.get( player.getUniqueId() ).getSize() + " square meter.");
							
						}else if( owner.getType().equals("GROUP") ){

							main.groups.get(owner.getOwnerId()).setSize(main.groups.get(owner.getOwnerId()).getSize() - 1);
							removeblockOwner(block);

							if(main.groups.get(owner.getOwnerId()).getNumMem() > 0){
								player.sendMessage("Group "+ main.groups.get(owner.getOwnerId()).getName() +" now owns " + main.groups.get(owner.getOwnerId()).getSize() + " square meter.");
							}else{
								if(main.groups.get(owner.getOwnerId()).getSize()<= 0){
									player.sendMessage(ChatColor.GREEN + "Group cleaned up and deleted.");
								}else{
									player.sendMessage(ChatColor.GREEN + "This group is dead. Thanks for cleaning up. (" + main.groups.get(owner.getOwnerId()).getSize() + " remaining)");
								}
							}
							main.saveGroup(main.groups.get(owner.getOwnerId()));
						}else{
							removeblockOwner(block);
						}
					}
				//Breaking LANDMARK?
				}else{
					boolean successful = false;
					Group g = null;
					SPlayer sp = null;
					Landmark lm = null;
					for (Group g_ : main.groups.values()){
						for (Landmark lm_ : g_.getLandmarks()){
							if(lm_.getLocation().equals(block.getLocation())){
								player.sendMessage("group landmark at [" + lm_.getWorld().getName() + "," + lm_.getX() + "," + lm_.getY() + "," + lm_.getZ() + "] removed");
								successful = true;
								g = g_;
								lm = lm_;
							}
						}
					}
					for (SPlayer sp_ : main.splayer.values()){
						for (Landmark lm_ : sp_.getLandmarks()){
							if(lm_.getLocation().equals(block.getLocation())){
								player.sendMessage("Private landmark at [" + lm_.getWorld().getName() + "," + lm_.getX() + "," + lm_.getY() + "," + lm_.getZ() +"] removed");
								successful = true;
								sp = sp_;
								lm = lm_;
							}
						}
					}	
					if(successful){
						if(g != null){
							g.getLandmarks().remove(lm);
							main.saveGroup(g);
						}
						if(sp != null){
							sp.getLandmarks().remove(lm);
							main.savePlayer(sp);
						}
					}else{
						player.sendMessage("No landmark found here. That's bad.");
					}

				}
				
			}else if(event.getBlock().getType().name().equals("FENCE") && event.getBlock().getRelative(0, 1, 0).getType().name().equals("SKULL")){
				player.sendMessage("dont remove landmarks that way");
				event.setCancelled(true);
			}
		}else{
			player.sendMessage(ChatColor.RED + "You dont have the permission to break here.");
			event.setCancelled(true);
		}

	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		
		Player player = event.getPlayer();
		
		if( (Math.floor(event.getFrom().getX()) != Math.floor(event.getTo().getX())) 
			|| (Math.floor(event.getFrom().getZ()) != Math.floor(event.getTo().getZ())) ){
			
			Owner ownerfrom = blockOwner(event.getFrom().getBlock().getLocation(),false);
			Owner ownerto = blockOwner(event.getTo().getBlock().getLocation(),false);

			if( !(ownerfrom.getOwnerId() == ownerto.getOwnerId())
			 || !(ownerfrom.getUUID().equals(ownerto.getUUID()) )
			 || !(ownerfrom.getType().equals(ownerto.getType()) )){
				
				String type = blockOwner(event.getTo().getBlock().getLocation(),false).getType();
				int id = blockOwner(event.getTo().getBlock().getLocation(),false).getOwnerId();
				UUID uuid = blockOwner(event.getTo().getBlock().getLocation(),false).getUUID();
				if(type.equals("GROUP")){
					for(Group g: main.groups.values()){
						if(g.getId() == id){
							player.sendMessage("~" + ChatColor.BLUE + g.getName() );
						}
					}	
				}else if(type.equals("PLAYER") ){
					if(uuid.equals(player.getUniqueId() ) ){
						player.sendMessage("~" + ChatColor.YELLOW + player.getName() );
					}else{
						player.sendMessage("~" + ChatColor.GOLD + Bukkit.getPlayer(uuid).getName() );
					}
				}else if(type.equals("UNOWNED")){
					player.sendMessage("~" + ChatColor.GREEN + u_name );
				}	
			}
				
		}		
	}
	
}
