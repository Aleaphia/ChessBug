/*
Code from my solution for CSC 322 Assignment 5, converted to generics
*/

package listHelper;

import java.util.*;
import java.io.*;


public class SavableList<E>{
    private ArrayList<E> list; //List content
    private String fileName; //Location to save and find list
    
    //Constructor
    public SavableList(String fileName){
        this.fileName = fileName; //save file location
        
        //Create new ArrayList to store file information
        list = new ArrayList<>();
        
        //Load list from file
        try(ObjectInputStream fileInput =
                new ObjectInputStream(new FileInputStream (fileName));){
            //Get list size
            int setSize = fileInput.readInt();
            for (int i = 0; i < setSize; i++){
                try{
                    //Add item to growing list
                    list.add((E)(fileInput.readObject()));
                }
                catch(ClassNotFoundException e){}//Skip item
            }
            System.out.println("File Loaded");
        }
        catch(IOException e){System.out.println("File Not Loaded");} //Nothing to load
    }
    
    //Method to save list to fileName
    public void save(){
        //Open output stream
        try(ObjectOutputStream fileOutput =
                new ObjectOutputStream(new FileOutputStream (new File(fileName)))){
            //Start file with an int representing the size of array
            fileOutput.writeInt(list.size());
            for (int i = 0; i < list.size(); i++){
                //Output contact
                fileOutput.writeObject(list.get(i));
            }
            System.out.println("File Saved");
        }
        catch (Exception e){System.out.println("File Not Saved");}
    }
    
    //Provide common array methods for interaction with list: add, remove
    public void add(E newItem){
        list.add(newItem);
    }
    public void remove(int index){
        list.remove(index);
    }
    
    //Getter methods
    public ArrayList<E> getList(){
        //Create arraylist copy. This ensures getContactList's caller cannot edit contactList
        ArrayList<E> copy = new ArrayList<>();
        copy.addAll(list);
        //Return copy
        return copy;
    }
    public E get(int index){
        return list.get(index);
    }
    public int getSize(){
        return list.size();
    }  
}
