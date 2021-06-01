package com.example.taller2.provider;

import android.content.Context;
import android.util.Log;

import com.example.taller2.model.User;
import com.example.taller2.provider.notification.NotificationProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProvider {

    public static final String PATH_USERS = "users/";

    public static final String TAG = "Provider User";

    public static FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static DatabaseReference ref = database.getInstance().getReference();

    public static void listenerChangeUser(Context ctx){

        ref = database.getReference(PATH_USERS);

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                User user = dataSnapshot.getValue(User.class);
                if(user.available) {
                    NotificationProvider.notificationUser.message = user.name + " " + user.lastName + " esta disponible";
                    NotificationProvider.createAlarm(ctx);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    public static void loadUsers(){
        ref = database.getReference(PATH_USERS);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                     User user = singleSnapshot.getValue(User.class);
                     Log.i(TAG, "Encontró usuario: " + user.name);
                     String name = user.name;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Error en la consulta",databaseError.toException());
            }
        });
    }
}
