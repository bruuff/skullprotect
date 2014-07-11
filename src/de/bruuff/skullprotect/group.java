package de.bruuff.skullprotect;

import java.util.ArrayList;
import java.util.UUID;

public class Group {
	int id;
	String name;
	int size;
	ArrayList<UUID> ops;
	ArrayList<UUID> ass;
	ArrayList<UUID> mem;	
	ArrayList<Landmark> landmarks;	
	Perm permission;
	
	public Group(int id_, String name_,int size_,ArrayList<UUID> ops_, ArrayList<UUID> ass_,ArrayList<UUID> mem_,ArrayList<Landmark> landmarks_, Perm permission_){
		id=id_;
		name=name_;
		size=size_;
		ops = ops_;
		ass = ass_;
		mem = mem_;	
		landmarks = landmarks_;	
		permission = permission_;
	} 
	public Group(int id_, String name_,int size_,ArrayList<UUID> ops_, ArrayList<UUID> ass_,ArrayList<UUID> mem_){
		id=id_;
		name=name_;
		size=size_;
		ops = ops_;
		ass = ass_;
		mem = mem_;
		landmarks = new ArrayList<Landmark>();
		permission = new Perm(true,true,true,true);
	} 
	public Group(int id_, String name_,ArrayList<UUID> ops_, ArrayList<UUID> ass_,ArrayList<UUID> mem_){
		id=id_;
		name=name_;
		size=0;
		ops = ops_;
		ass = ass_;
		mem = mem_;	
		landmarks = new ArrayList<Landmark>();
		permission = new Perm(true,true,true,true);
	}
	
	public Group(int id_, String name_,ArrayList<UUID> ops_, ArrayList<UUID> ass_,ArrayList<UUID> mem_, Perm permission_){
		id=id_;
		name=name_;
		size=0;
		ops = ops_;
		ass = ass_;
		mem = mem_;	
		landmarks = new ArrayList<Landmark>();
		permission = permission_;
	}
	
	public int getId(){
		return id; 
    }
	public void setId(int id_){
		id = id_;
	}
	
	public void addop(UUID id_){
		ops.add(id_);
    }
	
	public void addass(UUID id_){
		ass.add(id_);
    }
	
	public void addmem(UUID id_){
		mem.add(id_);
    }
	
	public void remove(UUID id_){
		ops.remove(id_);
		ass.remove(id_);
		mem.remove(id_);
    }
	
	public String getName(){
		return name; 
    }
	public void setName(String name_){
		name = name_;
    }
	
	public int getSize(){
		return size; 
    }
	public void setSize(int size_){
		size = size_;
    }
	public ArrayList<UUID> getOps(){
		return ops; 
    }
	
	public ArrayList<UUID> getAss(){
		return ass; 
    }
	
	public ArrayList<UUID> getMem(){
		return mem; 
    }
	
	public ArrayList<Landmark> getLandmarks(){
		return landmarks; 
    }
	
	public void setLandmarks(ArrayList<Landmark> landmarks_){
		landmarks = landmarks_; 
    }
	
	public int getNumMem(){
		return (ops.size() + ass.size() + mem.size()); 
    }
	
	public boolean isop(UUID id_){
		for (UUID v : ops){
			if(v.equals(id_) ){
				return true;
			}
		}
		return false; 
    }
	
	public boolean isass(UUID id_){
		for (UUID v : ops){
			if(v.equals(id_) ){
				return true;
			}
		}
		for (UUID v : ass){
			if(v.equals(id_) ){
				return true;
			}
		}
		return false; 
    }
	
	public boolean ismem(UUID id_){
		for (UUID v : ops){
			if(v.equals(id_) ){
				return true;
			}
		}
		for (UUID v : ass){
			if(v.equals(id_) ){
				return true;
			}
		}
		for (UUID v : mem){
			if(v.equals(id_) ){
				return true;
			}
		}
		return false; 
    }
	
	public Perm getPermission(){
		return permission; 
    }
	public void setSize(Perm permission_){
		permission = permission_;
	}
}
