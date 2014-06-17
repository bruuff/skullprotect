package de.bruuff.skullprotect;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class EventListener implements Listener {
	skullprotect main = null;
	String u_head = "";
	String u_name = "";
	
	public EventListener(skullprotect plugin){
		main = plugin;
		u_head = main.getConfig().getString(main.getDescription().getName() + ".unowned.head");
		u_name = main.getConfig().getString(main.getDescription().getName() + ".unowned.name");
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	/*
	@EventHandler
	public void onLogin(PlayerLoginEvent event){	

	}
	*/

	
	private String skullowner(Block block, Boolean onedown){

		String owner = null;
		String owner1d = null;
		
		for ( int i = 0; i <= block.getY(); i ++ ) {	
			if(block.getWorld().getBlockAt(block.getX(), i, block.getZ()).getType().name() == "SKULL" ){
				
				BlockState blockstate = block.getWorld().getBlockAt(block.getX(), i, block.getZ()).getState();
				owner1d = owner;
				if (blockstate instanceof Skull) {
					Skull skull = (Skull) blockstate;
					if(skull.getSkullType().name().equals("PLAYER") && skull.getOwner() != null){
						owner = skull.getOwner();	
					}
					 
				}
				
			}
		}	
				
		if((block.getType().name() == "SKULL" && owner.equals(u_head))  || onedown ){
			owner = owner1d;
		}	
		if(owner == null){
			owner = u_name;
		}
		if(owner.equals(u_head) ){
			owner = u_name;
		}
		return owner;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		ArrayList<String> valids = new ArrayList<String>();
		valids.add(player.getName());
		valids.add(main.getConfig().getString(main.getDescription().getName() + ".unowned.name"));
		
		for (group g : main.groups.values()){
			if(g.ismem(player.getName())){
				valids.add("%" + g.getName());
			}
		}
		
		if(event.getBlock().getType().name() == "SKULL" ){
			if( (valids.contains(skullowner(block,true)) && player.hasPermission("skullprotect.place.skull.own") ) || player.hasPermission("skullprotect.place.skull.foreign") ){
				
				ItemStack istack = event.getItemInHand();
				SkullMeta sm = (SkullMeta) istack.getItemMeta();
				
				if(sm.getDisplayName().startsWith("Marker")){
					istack.setAmount(2);
					ItemStack is = player.getItemInHand();
					is.setAmount(is.getAmount() - 1);
					player.setItemInHand(is); 
				}
				
				if(sm.getOwner().equals(u_head)){
				     sm.setOwner(u_name);
				}

				boolean facing = false;
				if(skullowner(event.getBlockPlaced().getRelative(-1, 4, 0),false).equals(sm.getOwner()) ){facing=true;}
				if(skullowner(event.getBlockPlaced().getRelative( 1, 4, 0),false).equals(sm.getOwner()) ){facing=true;}
				if(skullowner(event.getBlockPlaced().getRelative( 0, 4,-1),false).equals(sm.getOwner()) ){facing=true;}
				if(skullowner(event.getBlockPlaced().getRelative( 0, 4, 1),false).equals(sm.getOwner()) ){facing=true;}
				if(skullowner(event.getBlockPlaced().getRelative( 0, 0, 0),true).equals(sm.getOwner()) ){facing=true;}
	
				if(!facing && !sm.getDisplayName().startsWith("Starter") && !sm.getOwner().equals(u_name) ){
					event.setCancelled(true);	
					player.sendMessage("Has to extend existing area.");
				}else{
					if(main.usingeconomy){
						
						if(sm.getOwner().equals(player.getName()) ){
							
							String maxclaimstring = main.maxclaim.replace("plots", main.landsize( player.getName() )+"").replace("money", Double.toString(skullprotect.economy.getBalance(player)) );
							double maxclaim_ = Math.floor(main.calculate( maxclaimstring) );
							String claimpricestring = main.claimprice.replace("plots", main.landsize( player.getName() )+"").replace("money", Double.toString(skullprotect.economy.getBalance(player)) );
							double claimprice_ = Math.floor(main.calculate( claimpricestring) );
							if(skullprotect.economy.has(player, claimprice_)){
								if(main.landsize(player.getName()) <= (maxclaim_ - 1) ){
									main.setlandsize(player, main.landsize(player.getName()) + 1);
									skullprotect.economy.withdrawPlayer(player, claimprice_);
									player.sendMessage("("  + claimprice_ + skullprotect.economy.currencyNamePlural() + " paid) You now own " + main.landsize(player.getName()) + " square meter.");
								}else{
									player.sendMessage("You have reached your maximum plot size.");
									event.setCancelled(true);	
								}
							}else{
								player.sendMessage("You dont have enough money to buy this.");
								event.setCancelled(true);
							}
						}else if(!sm.getOwner().equals(u_head) ){
						
							if(sm.getOwner().startsWith("%") && main.groups.get(sm.getOwner().substring(1)) != null){
								
								String maxclaimstring = main.maxclaim.replace("plots", main.groups.get(sm.getOwner().substring(1)).getSize()+"").replace("money", Double.toString(skullprotect.economy.bankBalance(sm.getOwner().substring(1)).balance) );
								double maxclaim_ = Math.floor(main.calculate( maxclaimstring) );
								String claimpricestring = main.claimprice.replace("plots", main.groups.get(sm.getOwner().substring(1)).getSize()+"").replace("money", Double.toString(skullprotect.economy.bankBalance(sm.getOwner().substring(1)).balance) );
								double claimprice_ = Math.floor(main.calculate( claimpricestring) );
								
								if(skullprotect.economy.bankBalance(sm.getOwner().substring(1)).balance >= claimprice_){
									if(main.groups.get(sm.getOwner().substring(1)).getSize() <= (maxclaim_ - 1) ){
										group g = main.groups.get(sm.getOwner().substring(1));
										g.setSize(g.getSize() + 1);
										main.saveGroup(g);
										skullprotect.economy.bankWithdraw(sm.getOwner().substring(1), claimprice_);
										player.sendMessage("("  + claimprice_ + skullprotect.economy.currencyNamePlural() + " paid) Group "+ g.getName() +" now owns " + g.getSize() + " square meter.");
										
									}else{
										player.sendMessage("This group has reached its maximum plot size.");
										event.setCancelled(true);	
									}
								}else{
									player.sendMessage("Your group does not have enough money to buy this.");
									event.setCancelled(true);
								}
							}		
						}
						
					}else{
						if(sm.getOwner().equals(player.getName()) ){
							main.setlandsize(player, main.landsize(player.getName()) + 1);
							player.sendMessage("You now own " + main.landsize(player.getName()) + " square meter.");
						}else if(!sm.getOwner().equals(u_head) ){
							
							if(sm.getOwner().startsWith("%") && main.groups.get(sm.getOwner().substring(1)) != null){
								group g = main.groups.get(sm.getOwner().substring(1));
								g.setSize(g.getSize() + 1);
								main.saveGroup(g);
								player.sendMessage("Group "+ g.getName() +" now owns " + g.getSize() + " square meter.");
							}	
						}

					}
				}
	
			}else{
				player.sendMessage(ChatColor.RED + "You dont have the permission to place this skull.");
				event.setCancelled(true);
			}
				
		}else{
			
			if(valids.contains(skullowner(block,false))  || player.hasPermission("skullprotect.place.blocks")){
				
			}else{
				player.sendMessage(ChatColor.RED + "You dont have the permission to place here.");
				event.setCancelled(true);
			}
			
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
		
		ArrayList<String> valids = new ArrayList<String>();
		valids.add(player.getName());
		valids.add(main.getConfig().getString(main.getDescription().getName() + ".unowned.name"));
		
		boolean emptycheck = false;
		for (group g : main.groups.values()){
			if(g.ismem(player.getName())){
				valids.add("%" + g.getName());
			}
			if(g.getNumMem() <= 0){
				emptycheck = true;
			}
		}
		
		

		if(event.getBlock().getType().name() == "SKULL" ){
	
			if( (valids.contains(skullowner(block,false)) && player.hasPermission("skullprotect.break.skull.own") ) || player.hasPermission("skullprotect.break.skull.foreign") ){
				
				BlockState blockstate = event.getBlock().getState();
				Skull skull = (Skull) blockstate;
				if(skull.getSkullType().name().equals("PLAYER") && skull.getOwner() != null){	
					
					
					if(skull.getOwner().equals(player.getName()) ){
						main.setlandsize(player, main.landsize(player.getName()) - 1);
						player.sendMessage("You now own " + main.landsize(player.getName()) + " square meter.");
					}else if(!skull.getOwner().equals(u_head) ){
						
						if(skull.getOwner().startsWith("%") && main.groups.get(skull.getOwner().substring(1)) != null){
							group g = main.groups.get(skull.getOwner().substring(1));
							g.setSize(g.getSize() - 1);
							main.saveGroup(g);
							player.sendMessage("Group "+ g.getName() +" now owns " + g.getSize() + " square meter.");
						}	
					}
					
					event.setCancelled(true);
					event.getBlock().setType(Material.AIR);
				}
			}else if(emptycheck){
				
				BlockState blockstate = event.getBlock().getState();
				Skull skull = (Skull) blockstate;
				group g = main.groups.get(skull.getOwner().substring(1));
				g.setSize(g.getSize() - 1);
				if(g.getSize()<= 0){
					player.sendMessage(ChatColor.GREEN + "Group cleaned up and deleted.");
					main.deleteGroup(g.getName());
				}else{
					player.sendMessage(ChatColor.GREEN + "This group is dead. Thanks for cleaning up. (" + g.getSize() + " remaining)");
					main.saveGroup(g);
				}
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
			}else{
				player.sendMessage(ChatColor.RED + "You dont have the permission to break this skull.");
				event.setCancelled(true);
			}
				
		}else{
			
			if(valids.contains(skullowner(block,false))  || player.hasPermission("skullprotect.break.blocks")){
				
			}else{
				player.sendMessage(ChatColor.RED + "You dont have the permission to break here.");
				event.setCancelled(true);
			}
			
			
		}

	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		
		Player player = event.getPlayer();
		
		if( (Math.floor(event.getFrom().getX()) != Math.floor(event.getTo().getX())) 
			|| (Math.floor(event.getFrom().getZ()) != Math.floor(event.getTo().getZ())) ){
			
			if( !skullowner(event.getFrom().getBlock(),false).equals(skullowner(event.getTo().getBlock(),false)) ){			
				String m = skullowner(event.getTo().getBlock(),false);
						
				if(m.startsWith("%")){
					player.sendMessage("~" + ChatColor.BLUE + skullowner(event.getTo().getBlock(),false).substring(1) );
				}else if(m.equals(player.getName())){
					player.sendMessage("~" + ChatColor.YELLOW + skullowner(event.getTo().getBlock(),false) );
				}else if(m.equals(u_name)){
					player.sendMessage("~" + ChatColor.GREEN + skullowner(event.getTo().getBlock(),false) );
				}else{
					player.sendMessage("~" + ChatColor.GOLD + skullowner(event.getTo().getBlock(),false) );
				}
				
				
			}		
		}		
	}
	
//Prevent Dropping Markers
	@EventHandler
    public void ItemDrop(PlayerDropItemEvent event){
        if(event.getItemDrop().getItemStack().getType() == Material.SKULL_ITEM){
        	event.getItemDrop().remove();
        }
    }
	@EventHandler
    public void DeathDrop(PlayerDeathEvent event){      	
        for (ItemStack istack : event.getDrops()){
        	if(istack.getType() == Material.SKULL_ITEM){
            	istack.setAmount(0);
            }
        }

    }
	
}
