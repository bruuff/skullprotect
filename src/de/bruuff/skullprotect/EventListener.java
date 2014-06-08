package de.bruuff.skullprotect;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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
	
	private String skullowner(Block block){

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
				
		if(block.getType().name() == "SKULL"){
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

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlace(BlockPlaceEvent event){		
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		ArrayList<String> valids = new ArrayList<String>();
		valids.add(player.getName());
		valids.add(main.getConfig().getString(main.getDescription().getName() + ".unowned.name"));
		
		for (group g : main.groups){
			if(g.ismem(player.getName())){
				valids.add("%" + g.getName());
			}
		}
		
		
		if(valids.contains(skullowner(block)) || player.hasPermission("skullprotect.place")  ){

			if(event.getBlockPlaced().getType().name() == "SKULL" ){
				ItemStack istack = event.getItemInHand();
				SkullMeta sm = (SkullMeta) istack.getItemMeta();
				
				if(sm.getDisplayName().startsWith("Marker")){
					istack.setAmount(2);
					ItemStack is = player.getItemInHand();
					is.setAmount(is.getAmount() - 1);
					player.setItemInHand(is); 
				}else{
					istack.setAmount(2);
					ItemStack is = player.getItemInHand();
					is.setAmount(0);
					player.setItemInHand(is); 
				}
				
				if(sm.getOwner().equals(u_head)){
				     sm.setOwner(u_name);
				}
				if(valids.contains(sm.getOwner())){
					boolean facing = false;
					if(skullowner(event.getBlockPlaced().getRelative(-1, 4, 0)).equals(sm.getOwner()) ){facing=true;}
					if(skullowner(event.getBlockPlaced().getRelative( 1, 4, 0)).equals(sm.getOwner()) ){facing=true;}
					if(skullowner(event.getBlockPlaced().getRelative( 0, 4,-1)).equals(sm.getOwner()) ){facing=true;}
					if(skullowner(event.getBlockPlaced().getRelative( 0, 4, 1)).equals(sm.getOwner()) ){facing=true;}
					if(skullowner(event.getBlockPlaced().getRelative( 0, 0, 0)).equals(sm.getOwner()) ){facing=true;}
	
					if(!facing && !sm.getDisplayName().startsWith("Starter")){
						event.setCancelled(true);	
						player.sendMessage("Has to extend existing area.");
					}
					
				}else{
					event.setCancelled(true);	
					player.sendMessage("You can't place foreign skulls.");
				}

			}
		}else{
			event.setCancelled(true);
			//player.sendMessage("You can't build here.");
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
		
		for (group g : main.groups){
			if(g.ismem(player.getName())){
				valids.add("%" + g.getName());
			}
		}
		
		if(!valids.contains(skullowner(block))  && !player.hasPermission("skullprotect.break")  ){
			event.setCancelled(true);
		}else{
			event.setCancelled(false);
		}
		if(event.getBlock().getType().name() == "SKULL" ){
			BlockState blockstate = event.getBlock().getState();
			Skull skull = (Skull) blockstate;
			if(skull.getSkullType().name().equals("PLAYER") && skull.getOwner() != null){
				event.setCancelled(true);
				event.getBlock().setType(Material.AIR);
			}
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		
		Player player = event.getPlayer();
		
		if( (Math.floor(event.getFrom().getX()) != Math.floor(event.getTo().getX())) 
			|| (Math.floor(event.getFrom().getZ()) != Math.floor(event.getTo().getZ())) ){
			
			if( !skullowner(event.getFrom().getBlock()).equals(skullowner(event.getTo().getBlock())) ){			
				String m = skullowner(event.getTo().getBlock());
						
				if(m.startsWith("%")){
					player.sendMessage("~" + ChatColor.BLUE + skullowner(event.getTo().getBlock()).substring(1) );
				}else if(m.equals(player.getName())){
					player.sendMessage("~" + ChatColor.YELLOW + skullowner(event.getTo().getBlock()) );
				}else if(m.equals(u_name)){
					player.sendMessage("~" + ChatColor.GREEN + skullowner(event.getTo().getBlock()) );
				}else{
					player.sendMessage("~" + ChatColor.GOLD + skullowner(event.getTo().getBlock()) );
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
