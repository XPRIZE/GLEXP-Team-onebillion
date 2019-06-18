package com.maq.xprize.onecourse.hindi.utils;

import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.os.build;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.utils.OC_FatController;

import com.maq.xprize.onecourse.hindi.mainui.MainActivity;
import com.maq.xprize.onecourse.hindi.mainui.OBMainViewController;
import com.maq.xprize.onecourse.hindi.mainui.OBSectionController;


public class Firebase {
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private static DatabaseReference databaseReference = firebaseDatabase.getReference("main");
    private static DatabaseReference module_child;

    static User user = new User();   // its the user object that is being created using User.java. It contain the getter and setter.
    static OC_User CurrrentUser = new OC_User();
    static OCM_User u;
    static OC_FatController ff = new OC_FatController();

    private static String username;
    private static int Age;
    private static long elapseTime;
    private static String module_name;
    private static long lastStartTime;
    private static long lastendTime;
    private static String lastModule;
    private static int userIID;
    private static String DeviceID;

    private static long f;  /// for the testing perpose.


    public static void load(String m, int t){   //that's the part for testing the app connection with the FirebaseDatabase
        //String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //String token = FirebaseInstanceID.get
        databaseReference.child("device").child("mobile").setValue("connected");
        databaseReference.child("device").child("mobile").child("module").setValue(m);
        databaseReference.child("device").child("mobile").child("time").setValue(t);
    }

    public static void getValue(String module, final long startTime, int userid){
        userIID = userid;

//        DBSQL db = new DBSQL(true);
//        //u = ff.load();//OCM_FatController.lastUserActiveFromDB(db);
//        //CurrrentUser = OC_FatController.lastUserActiveFromDB(db);
//        //username =  CurrrentUser.name;//OC_FatController.currentUserName();//OC_FatController.getFirebase_user();//OC_FatController.firebase_user;//currentUser.userid;//"User1";
//        ff.loadUserDBSQL(db);
//        username = ff.getFirebase_user();
//        ff.currentUserName();
        username = Integer.toString(userid);//userid.toString()//"User1";
        elapseTime = 0;
        Age = 8;
        user.setUserName(username);
        user.setAge(Age);
        user.setModuleName(module);
        user.setStartTime(startTime);
        user.setElapseTIme(0);
        user.setEndTime(0);
        //s.substring(s.lastIndexOf(".") + 1);                            /// NOTE :: Firebase Database paths must NOT Contain (( '.', '#', '$', '[', or ']' ))
        System.out.println(module);
        module_name = module;
       // module_name = module.substring(module.lastIndexOf("." + 1));   /// it will remove the starting path of the module which is before dot(".")
        //String h = module.substring(module.lastIndexOf("." + 1));
        //elapseTime = endTime - startTime;

       // user.setEndTime(endTime);user.setElapseTime(endTime-startTime);
       // user.setElapseTime(elapseTime);

        DeviceID = OBSystemsManager.sharedManager.device_getUUID();

        databaseReference.child(DeviceID).child(username).child(module).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    databaseReference.child(DeviceID).child(username).child(module_name).child("startTime").setValue(startTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                System.out.println("data is successfully added..");
                            }
                        }
                    });
                    databaseReference.child(DeviceID).child(username).child(module_name).child("elapseTIme").setValue(102).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                System.out.println("data is successfully added..");
                            }
                        }
                    });
                }
                else {
                    uploadValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        uploadValue();
//        /*module_child =*/



    }


    public static void uploadValue(){
        DeviceID = OBSystemsManager.sharedManager.device_getUUID();

        databaseReference.child(DeviceID).child(username).child(module_name).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {    //TODO  change the mobile to the device ID
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    System.out.println("data is successfully addded");
                }
                else{
                    System.out.println("data is failed to upload to the firebase database");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.d(Tag, e.getMessage());
                System.out.println("Firebase database error: " + e.getMessage());
            }
        });
        // module_child.setValue(user);

        f = 54; /// for the testing purpose.

        //databaseReference.child("device").child("mobile").child(module_name).setValue(user);                                         // change the mobile to the device ID

        //databaseReference.addValueEventListener()

    }

    public static long fetchData(String module){
        DeviceID = OBSystemsManager.sharedManager.device_getUUID();


        databaseReference.child(DeviceID).child(username).child(module).child("startTime").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long StartTime = dataSnapshot.getValue().hashCode();
                lastStartTime = StartTime;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                System.out.println("application has failed to fetch last start time from the database");

            }
        });


        return lastStartTime;



    }

    public  static void endTime(String module,long endTime,int userid){                                   //using this, we are going to update the End Time of the module and elapse time
        DeviceID = OBSystemsManager.sharedManager.device_getUUID();
        userIID = userid;
        username = Integer.toString(userid);
        long Time;// = fetchData(module);//databaseReference.child(DeviceID).child(username).child(module).child("startTime").getValue().hashCode();//
        lastendTime = endTime;
        lastModule = module;
        databaseReference.child(DeviceID).child(username).child(module).child("startTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long StartTime = dataSnapshot.getValue().hashCode();
                lastStartTime = StartTime;
                elapseTime = lastendTime - lastStartTime;
                databaseReference.child(DeviceID).child(username).child(lastModule).child("elapseTIme").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue().hashCode() == 0){
                            databaseReference.child(DeviceID).child(username).child(lastModule).child("elapseTIme").setValue(elapseTime).addOnCompleteListener(new OnCompleteListener<Void>() {    //TODO  change the mobile to the device ID
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        System.out.println("data is successfully addded");
                                    }
                                    else{

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Log.d(Tag, e.getMessage());
                                    System.out.println("Firebase database error: " + e.getMessage());
                                }
                            });
                        }
                        else{
                            long updateElapseTime = dataSnapshot.getValue().hashCode();
                            elapseTime += updateElapseTime;
                            databaseReference.child(DeviceID).child(username).child(lastModule).child("elapseTIme").setValue(elapseTime).addOnCompleteListener(new OnCompleteListener<Void>() {    //TODO  change the mobile to the device ID
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        System.out.println("data is successfully addded");
                                    }
                                    else{

                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Log.d(Tag, e.getMessage());
                                    System.out.println("Firebase database error: " + e.getMessage());
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
//                databaseReference.child(DeviceID).child(username).child(lastModule).child("elapseTIme").setValue(elapseTime).addOnCompleteListener(new OnCompleteListener<Void>() {    //TODO  change the mobile to the device ID
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if(task.isSuccessful()){
//                            System.out.println("data is successfully addded");
//                        }
//                        else{
//
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        //Log.d(Tag, e.getMessage());
//                        System.out.println("Firebase database error: " + e.getMessage());
//                    }
//                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                System.out.println("application has failed to fetch last start time from the database");

            }
        });

      //  elapseTime = endTime - Time;


        databaseReference.child(DeviceID).child(username).child(module).child("endTime").setValue(endTime).addOnCompleteListener(new OnCompleteListener<Void>() {    //TODO  change the mobile to the device ID
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    System.out.println("data is successfully addded");
                }
                else{

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.d(Tag, e.getMessage());
                System.out.println("Firebase database error: " + e.getMessage());
            }
        });


    }


    public static void getData(){
        DeviceID = OBSystemsManager.sharedManager.device_getUUID();

        databaseReference.child(DeviceID).child(username).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                System.out.println("application has failed to fetch last start time from the database : " + databaseError);

            }
        });


    }


//    public String DevicID(){
//        String id ;//= Settings.Secure.getString(MainActivity.getContentResolver(),Settings.Secure.ANDROID_ID);
//        //TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//
//        return id;
//    }

//    public String device_getUUID()
//    {
//        String uuid = device_getSerial();
//        //
//        if (uuid == null)
//        {
//            uuid = "unknown_uuid";
//
//            String mac = device_getMac();
//            if (mac != null)
//                uuid = mac.replace(":", "");
//
//        }
//        //
//        return uuid;
//    }




}
