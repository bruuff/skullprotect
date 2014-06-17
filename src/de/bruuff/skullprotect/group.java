package de.bruuff.skullprotect;

import java.util.ArrayList;

public class group {
	String name;
	int size;
	ArrayList<String> ops;
	ArrayList<String> ass;
	ArrayList<String> mem;	
	
	public group(String name_,int size_,ArrayList<String> ops_, ArrayList<String> ass_,ArrayList<String> mem_){
		name=name_;
		size=size_;
		ops = ops_;
		ass = ass_;
		mem = mem_;	
	} 
	public group(String name_,ArrayList<String> ops_, ArrayList<String> ass_,ArrayList<String> mem_){
		name=name_;
		size=0;
		ops = ops_;
		ass = ass_;
		mem = mem_;	
	}
	public void addop(String name){
		ops.add(name);
    }
	
	public void addass(String name){
		ass.add(name);
    }
	
	public void addmem(String name){
		mem.add(name);
    }
	
	public void remove(String name){
		ops.remove(name);
		ass.remove(name);
		mem.remove(name);
    }
	
	public String getName(){
		return name; 
    }
	
	public int getSize(){
		return size; 
    }
	public void setSize(int size_){
		size = size_;
    }
	public ArrayList<String> getOps(){
		return ops; 
    }
	
	public ArrayList<String> getAss(){
		return ass; 
    }
	
	public ArrayList<String> getMem(){
		return mem; 
    }
	
	public int getNumMem(){
		return (ops.size() + ass.size() + mem.size()); 
    }
	
	public boolean isop(String name){
		for (String v : ops){
			if(v.equals(name) ){
				return true;
			}
		}
		return false; 
    }
	
	public boolean isass(String name){
		for (String v : ops){
			if(v.equals(name) ){
				return true;
			}
		}
		for (String v : ass){
			if(v.equals(name) ){
				return true;
			}
		}
		return false; 
    }
	
	public boolean ismem(String name){
		for (String v : ops){
			if(v.equals(name) ){
				return true;
			}
		}
		for (String v : ass){
			if(v.equals(name) ){
				return true;
			}
		}
		for (String v : mem){
			if(v.equals(name) ){
				return true;
			}
		}
		return false; 
    }
}
