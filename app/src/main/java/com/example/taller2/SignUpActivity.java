package com.example.taller2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignUpActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int TAKE_IMAGE = 2;

    private ImageView profilePicture;
    private ImageButton cameraBtn, mediaBtn;

    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabase;

    private EditText nameField, lastNameField, emailField, passwordField, identificationField;
    private TextView latitudeField, longitudeField;
    private Button signUpBtn, loginBtn;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private String latitude, longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Storage
        mStorage = FirebaseStorage.getInstance();

        // Get database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // LINK UI
        nameField = findViewById(R.id.r_name_field);
        lastNameField = findViewById(R.id.r_last_name_field);
        identificationField = findViewById(R.id.r_id_field);
        emailField = findViewById(R.id.r_email_field);
        passwordField = findViewById(R.id.r_password_field);
        profilePicture = findViewById(R.id.register_profile_pic);
        cameraBtn = findViewById(R.id.use_camera_btn);
        mediaBtn = findViewById(R.id.select_media_btn);
        latitudeField = findViewById(R.id.r_latitude_field);
        longitudeField = findViewById(R.id.r_longitude_field);
        signUpBtn = findViewById(R.id.sign_up_btn);
        loginBtn = findViewById(R.id.open_login_btn);

        // Location and permissions
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationRequest = createLocationRequest();

        checkGPSPermissions();
        checkCameraPermissions();

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                if(location != null){
                    updateUI(location);
                }
            }
        };

        // Button listeners
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLoginActivity();
            }
        });

        mediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(android.content.Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");

                startActivityForResult(Intent.createChooser(galleryIntent,"Seleccione una imagen"), PICK_IMAGE);
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, TAKE_IMAGE);
            }
        });
    }

    private void checkCameraPermissions(){
        // Camera permission
        if(ContextCompat.checkSelfPermission(SignUpActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(SignUpActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    }, TAKE_IMAGE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePicture.setImageBitmap(bitmap);
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        if(requestCode == TAKE_IMAGE && resultCode == RESULT_OK){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            profilePicture.setImageBitmap(bitmap);
        }
    }

    private void startLocationUpdates(){
        int gpsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarsePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(gpsPermission == PackageManager.PERMISSION_GRANTED && coarsePermission == PackageManager.PERMISSION_GRANTED){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }

    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected LocationRequest createLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void checkGPSPermissions(){
        int gpsPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int coarsePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if(gpsPermission != PackageManager.PERMISSION_GRANTED && coarsePermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},2000);
        }
    }

    private void updateUI(Location location){
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());

        latitudeField.setText("LATITUD: " + latitude);
        longitudeField.setText("LONGITUD: " + longitude);
    }

    private void signUp(){
        String name = nameField.getText().toString();
        String lastName = lastNameField.getText().toString();
        String id = identificationField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        UserDTO newUser = new UserDTO(name, lastName, id, latitude, longitude);

        Bitmap bitmap = ((BitmapDrawable)profilePicture.getDrawable()).getBitmap();

        if(!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && bitmap != null){
            createUserInFirebase(email, password, newUser);
        }else{
            Toast.makeText(SignUpActivity.this,"Verifique que los campos esten completos y bien",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openLoginActivity(){
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveProfileImage(FirebaseUser user){
        String userID = user.getUid();
        StorageReference storageReference = mStorage.getReference();
        StorageReference imagesReference = storageReference.child("images");

        StorageReference profileImageReference = imagesReference.child(userID + "/profilePic.jpg");

        BitmapDrawable bitmapDrawable = (BitmapDrawable) profilePicture.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileImageReference.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
    }

    private void createUserInFirebase(String email, String password, UserDTO newUser){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            saveProfileImage(user);
                            addUserInfoToRealTimeDB(user, newUser);
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Error creando usuario",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void addUserInfoToRealTimeDB(FirebaseUser user, UserDTO newUser){
        String currentID = user.getUid().toString();
        mDatabase.child("users").child(currentID).setValue(newUser);
    }

    public class UserDTO{
        public String name, lastName, identification, latitude, longitude;

        public UserDTO(){

        }

        public UserDTO(String name, String lastName, String identification, String latitude, String longitude) {
            this.name = name;
            this.lastName = lastName;
            this.identification = identification;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}