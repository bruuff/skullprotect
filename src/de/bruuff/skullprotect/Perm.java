package de.bruuff.skullprotect;

public class Perm {
	boolean pvp;
	boolean explosions;
	boolean firespread;
	boolean mobspawn;
	
	public Perm(boolean pvp_, boolean explosions_, boolean firespread_, boolean mobspawn_){
		pvp = pvp_;
		explosions = explosions_;
		firespread = firespread_;
		mobspawn = mobspawn_;
	} 
	
	public boolean getPvP(){
		return pvp; 
    }
	public void setPvP(boolean pvp_){
		pvp = pvp_;
	}
	
	public boolean getExplosion(){
		return explosions; 
    }
	public void setExplosion(boolean explosions_){
		explosions = explosions_;
	}
	
	public boolean getFirespread(){
		return firespread; 
    }
	public void setFirespread(boolean firespread_){
		firespread = firespread_;
	}
	
	public boolean getMobspawn(){
		return mobspawn; 
    }
	public void setMobspawn(boolean mobspawn_){
		mobspawn = mobspawn_;
	}
	
	public String toString(){
		return pvp + "," + explosions + "," + firespread + "," + mobspawn;
	}
}
