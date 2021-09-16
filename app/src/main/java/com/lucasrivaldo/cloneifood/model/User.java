package com.lucasrivaldo.cloneifood.model;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.lucasrivaldo.cloneifood.config.ConfigurateFirebase;

import java.io.Serializable;
import java.util.HashMap;

import static com.lucasrivaldo.cloneifood.helper.IfoodHelper.TAG;
import static com.lucasrivaldo.cloneifood.config.ConfigurateFirebase.USERS;

public class User implements Serializable{

    private String email, id, name, address;

    public User() {
    }

    public boolean save(boolean isSaving){

        DatabaseReference usersRef = ConfigurateFirebase.getFireDBRef()
                                                            .child(USERS)
                                                                .child(this.getId());
        try {

            if (isSaving) {
                usersRef.setValue(this);
            }else {
                usersRef.removeValue();
            }

            return true;

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "User.save(): "+e.getMessage());

            return false;
        }
    }

    public boolean update(){
        DatabaseReference usersRef = ConfigurateFirebase.getFireDBRef()
                .child(USERS)
                .child(this.getId());

        HashMap userMap = new HashMap();

        userMap.put("name", this.getName());
        userMap.put("address", this.getAddress());

        try {
            usersRef.updateChildren(userMap);
            return true;

        }catch (Exception e ){
            e.printStackTrace();
            Log.d(TAG, "User.update(): "+e.getMessage());

            return false;
        }

    }

    public String getAddress() { return address; }
    public String getEmail() { return email; }
    public String getId() { return id; }
    public String getName() { return name; }


    public void setAddress(String address) { this.address = address; }
    public void setEmail(String email) { this.email = email; }
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
}
