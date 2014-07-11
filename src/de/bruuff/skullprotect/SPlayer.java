package de.bruuff.skullprotect;

import java.util.ArrayList;

import org.bukkit.OfflinePlayer;

public class SPlayer {
	//int id;
	OfflinePlayer op;
	int size;
	ArrayList<Landmark> landmarks;	
	Perm permission;

	public SPlayer(OfflinePlayer op_){
		//id = id_;
		op = op_;
		size = 0;
		landmarks = new ArrayList<Landmark>();
		permission = new Perm(true,true,true,true);
	} 
	
	public SPlayer(OfflinePlayer op_, int size_){
		//id = id_;
		op = op_;
		size = size_;
		landmarks = new ArrayList<Landmark>();
		permission = new Perm(true,true,true,true);
	} 
	public SPlayer(OfflinePlayer op_, int size_, ArrayList<Landmark> landmarks_){
		//id = id_;
		op = op_;
		size = size_;
		landmarks = landmarks_;	
		permission = new Perm(true,true,true,true);
	} 
	public SPlayer(OfflinePlayer op_, int size_, ArrayList<Landmark> landmarks_, Perm permission_){
		//id = id_;
		op = op_;
		size = size_;
		landmarks = landmarks_;	
		permission = permission_;
	}
	/*
	public int getId(){
		return id; 
    }
	public void setId(int id_){
		id = id_;
	}
	*/
	public OfflinePlayer getPlayer(){
		return op; 
    }
	public void setPlayer(OfflinePlayer op_){
		op = op_;
	}
	public void setLandmarks(ArrayList<Landmark> landmarks_){
		landmarks = landmarks_; 
    }
	
	public int getSize(){
		return size; 
    }
	public void setSize(int size_){
		size = size_;
	}
	public void sizeplusone(){
		size++;
    }
	public void sizeminusone(){
		size--;
    }
	public ArrayList<Landmark> getLandmarks(){
		return landmarks; 
	}
	
	public Perm getPermission(){
		return permission; 
    }
	public void setSize(Perm permission_){
		permission = permission_;
	}
	
}
