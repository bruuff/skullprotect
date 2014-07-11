package de.bruuff.skullprotect;

import java.util.UUID;

public class Owner {
	int height;
	int ownerid;
	UUID uuid;
	String type;
	
	public Owner(int height_, int ownerid_, String type_){
		height = height_;
		ownerid = ownerid_;
		uuid = new UUID(0,0);
		type = type_;
	} 
	
	public Owner(int height_, UUID uuid_, String type_){
		height = height_;
		ownerid = -1;
		uuid = uuid_;
		type = type_;
	}
	
	public Owner(int height_, String type_){
		height = height_;
		ownerid = 0;
		uuid = new UUID(0,0);
		type = type_;
	} 
	
	public int getHeight(){
		return height; 
    }
	public int getOwnerId(){
		return ownerid;
    }
	public UUID getUUID(){
		return uuid;
    }
	public String getType(){
		return type; 
    }
	public String toString(){
		if(type.equals("GROUP") ){
			return "[" + height + "," + ownerid + "," + type + "]";
		}
		if(type.equals("PLAYER") ){
			return "[" + height + "," + uuid.toString() + "," + type + "]";
		}
		return "";
    }
	public void setHeight(int height_){
		height = height_;
	}
	public void setOwnerId(int ownerid_){
		ownerid = ownerid_;
    }
	public void setUUID(UUID uuid_){
		uuid = uuid_;
    }
	public void setType(String type_){
		type = type_;
	}
}
