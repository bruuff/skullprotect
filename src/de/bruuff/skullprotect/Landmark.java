package de.bruuff.skullprotect;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Landmark {
	World world;
	//String owner;
	int x;
	int y;
	int z;

	public Landmark(int x_, int y_, int z_){
		world=Bukkit.getWorld("world");
		x = x_;
		y = y_;
		z = z_;	
	} 
	public Landmark(World world_,int x_, int y_, int z_){
		world=world_;
		x = x_;
		y = y_;
		z = z_;	
	} 
	
	public Landmark(Block block){
		world=block.getWorld();
		x = block.getX();
		y = block.getY();
		z = block.getZ();
	} 
	public Landmark(Location location){
		world=location.getWorld();
		x = location.getBlockX();
		y = location.getBlockY();
		z = location.getBlockZ();
	}
	
	public World getWorld(){
		return world; 
    }
	public Location getLocation(){
		return new Location(world,x,y,z); 
    }
	public int getX(){
		return x; 
    }
	public int getY(){
		return y; 
    }
	public int getZ(){
		return z; 
    }
	public String txtString(){
		return "[" + world.getName() + "," + x + "," + y + "," + z + "]"; 
    }

	public void setX(int x_){
		x = x_;
    }
	public void setY(int y_){
		y = y_;
    }
	public void setZ(int z_){
		z = z_;
    }
	
}
