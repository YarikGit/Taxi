package com.example.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private static final int AVATAR_PICKER = 1;

    private ImageView avatar;
    private EditText name, phone;
    private TextView email;
    private ImageButton save, signOut;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private ChildEventListener usersChildEventListener;

    private Uri avatarUri;
    private String currentKey;
    private String currentAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        linkViews();
        setDatabase();
        getUserData();
        createButtons();
    }

    private void createButtons() {
        avatar.setOnClickListener(v -> {
            Intent imageIntent = new Intent(Intent.ACTION_GET_CONTENT);
            imageIntent.setType("image/*");
            imageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(imageIntent, "Choose an image"), AVATAR_PICKER);
        });

        signOut.setOnClickListener(v -> FirebaseAuth.getInstance().signOut());

        save.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to save your changes?");
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                if (dialog != null) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("Save", (dialog, which) -> saveSettings());
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    private void linkViews() {
        avatar = findViewById(R.id.settingsAvatar);
        name = findViewById(R.id.settingsNameEditText);
        phone = findViewById(R.id.settingsPhoneEditText);
        email = findViewById(R.id.settingsEmailTextView);
        save = findViewById(R.id.settingsSaveImageButton);
        signOut = findViewById(R.id.settingsSignOut);
    }

    private void setDatabase() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        databaseReference = database.getReference().child("users");
        storageReference = storage.getReference().child("avatar_images");
    }

    private void getUserData() {
        currentKey = null;
        if (usersChildEventListener == null) {
            usersChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    User user = snapshot.getValue(User.class);
                    if (user.getId().equals(auth.getCurrentUser().getUid())) {
                        name.setText(user.getName());
                        email.setText(user.getEmail());
                        avatarUri = Uri.parse(user.getAvatar());
                        if (currentKey == null) {
                            currentKey = user.getKey();
                            currentAvatar = user.getAvatar();
                            Glide.with(avatar.getContext()).load(user.getAvatar()).into(avatar);
                        }
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            };
            databaseReference.addChildEventListener(usersChildEventListener);
        }
    }

    private void getNewUSerData(Uri avatarUri) {
        User user = new User();
        String key = database.getReference().push().getKey();
        user.setName(name.getText().toString().trim());
        user.setEmail(email.getText().toString().trim());
        user.setId(auth.getUid());
        user.setKey(key);
        user.setAvatar(avatarUri.toString());

        HashMap<String, Object> postValues = new HashMap<>();
        postValues.put("avatar", user.getAvatar());
        postValues.put("email", user.getEmail());
        postValues.put("id", user.getId());
        postValues.put("key", user.getKey());
        postValues.put("name", user.getName());

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + key, postValues);

        database.getReference().child("/users/" + currentKey).removeValue();
        database.getReference().updateChildren(childUpdates);
        getUserData();
    }

    private void saveSettings() {

        if (!(currentAvatar.equals(avatarUri.toString()))) {
            final StorageReference imageReference = storageReference.
                    child(avatarUri.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(avatarUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return imageReference.getDownloadUrl();
            }).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    getNewUSerData(downloadUri);

                } else {
                    // Handle failures
                }
            });
        } else {
            getNewUSerData(Uri.parse(currentAvatar));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AVATAR_PICKER && resultCode == RESULT_OK) {
            if (data != null) {
                avatarUri = data.getData();
                Glide.with(avatar.getContext()).load(data.getData()).into(avatar);
            }
        }
    }

}
