package com.example.grouptracker.Main.Authentication;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.grouptracker.Main.Maps.MapsFragment;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SignUpActivity extends AppCompatActivity{
    String email, password, password2, name, gender, code, date;
    TextInputLayout emailInput;
    TextInputLayout passInput;
    TextInputLayout pass2Input;
    TextInputLayout nameInput;
    CircleImageView profileImage;
    Button signUp;
    Uri image_uri;
    ProgressDialog dialog;
    Toolbar toolbar;
    String mCurrentPhotoPath;
    private Group group;
    private ProgressBar mProgressBar;

    // Firebase variables
    FirebaseAuth auth;
    DatabaseReference user_information;
    StorageReference storageReference;
    DatabaseReference groupReference;
    private FirebaseFirestore mDb;
    private FirebaseUser firebaseUser;
    private User user;
    private String surrname;
    private String isSharing;
    private String title;
    Bitmap bm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailInput = (TextInputLayout) findViewById(R.id.firstEmailInput);
        passInput = (TextInputLayout) findViewById(R.id.firstPassInput);
        pass2Input = (TextInputLayout) findViewById(R.id.secondPassInput);
        nameInput = (TextInputLayout) findViewById(R.id.firstNameInput);
        mProgressBar = findViewById(R.id.progressBar);
        profileImage = findViewById(R.id.image_choose_avatar);
        gender = "";
        signUp = findViewById(R.id.sign_up);
        name = "";
        code = generateCode();
        date = generateDate();

        bm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.com_facebook_profile_picture_blank_portrait);

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        mDb = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondaryLight));

        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);
        storageReference = FirebaseStorage.getInstance().getReference().child("Images");
        groupReference = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Signing up user...");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
    }

    public void female(View v) {
        gender = "Female";
        profileImage.setImageResource(R.mipmap.default_female_photo);
    }
    public void male(View v) {
        gender = "Male";
        profileImage.setImageResource(R.mipmap.default_male_photo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.basic_back_navigation, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to previous activity
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                Intent previous = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(previous);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public String generateCode() {
        Random r = new Random();
        int n = 100000 + r.nextInt(900000);
        return String.valueOf(n);
    }

    public String generateDate() {
        Date myDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault());
        return dateFormat.format(myDate);
    }

    public void signUpUser() {
        dialog.setMessage("Signing up user...");
        dialog.show();
        email = emailInput.getEditText().getText().toString();
        password = passInput.getEditText().getText().toString();
        password2 = pass2Input.getEditText().getText().toString();
        name = nameInput.getEditText().getText().toString();
        isSharing = "true";
        date = generateDate();
        if (name.contains(" ")) {
            surrname = name.split(" ")[1];
        } else {
            surrname = name;
        }

        if(email.equals("")) {
            dialog.dismiss();
            emailInput.setError("Email is required!");
            emailInput.requestFocus();
        } else if(password.equals("")) {
            dialog.dismiss();
            emailInput.setError("");
            passInput.setError("Password is required!");
            passInput.requestFocus();
        } else if (!password.equals(password2)) {
            dialog.dismiss();
            emailInput.setError("");
            passInput.setError("");
            pass2Input.setError("Passwords must match");
            pass2Input.requestFocus();
        } else if (name.equals("")) {
            dialog.dismiss();
            emailInput.setError("");
            passInput.setError("");
            pass2Input.setError("");
            nameInput.setError("You must enter a name/nickname");
            nameInput.requestFocus();
        } else if (gender.equals("")) {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please choose a gender", Toast.LENGTH_SHORT).show();
        } else {
            nameInput.setError("");
            pass2Input.setError("");
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = task.getResult().getUser();
                                user = new User(name, surrname, email, gender, isSharing, "", firebaseUser.getUid(), date, 4000);
                                createGroupDetails();
                                if (image_uri != null) {
                                    final StorageReference sr = storageReference.child(firebaseUser.getUid() + ".jpg");
                                        sr.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri) {
                                                                    user.setImageUri(uri.toString());
                                                                }
                                                            });
                                                }
                                            }
                                        });
                                } else {
                                    final StorageReference sr = storageReference.child(firebaseUser.getUid() + ".jpg");
                                    sr.putFile(getImageUri(getApplicationContext(), bm)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        user.setImageUri(uri.toString());
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                                ((UserClient) getApplicationContext()).setUser(user);
                                uploadUser();
                                uploadGroup();
                                sendVerificationEmail();
                                goToLoginActivity();

                            } else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                switch(errorCode) {
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        emailInput.setError("The email address is already in use.");
                                        emailInput.requestFocus();
                                        break;

                                    case "ERROR_WEAK_PASSWORD":
                                        passInput.setError("The password must be at least 6 characters long");
                                        passInput.requestFocus();
                                        break;

                                    case "ERROR_INVALID_EMAIL":
                                        emailInput.setError("The email address is badly formatted.");
                                        emailInput.requestFocus();
                                        break;
                                }
                            }
                        }
                    });
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void goToLoginActivity() {
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    private void uploadUser() {
        DocumentReference userDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid());

        userDocumentReference.set(user);
        DocumentReference userGroupsDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid())
                .collection(getString(R.string.collection_group_list))
                .document(user.getFamily());

        userGroupsDocumentReference.set(group);
    }

    private void uploadGroup() {
        DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(user.getFamily());

        groupDocumentReference.set(group);

        DocumentReference groupMembersReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(user.getFamily())
                .collection(getString(R.string.collection_group_user_list))
                .document(firebaseUser.getUid());

        groupMembersReference.set(user);
    }

    private void createGroupDetails() {
        DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document();

        code = generateCode();
        if (name.contains(" ")) {
            surrname = name.split(" ")[1];
        }
        title = surrname + " Family";
        group = new Group(title, groupDocumentReference.getId(), firebaseUser.getUid(), code);
        user.setFamily(group.getGroup_id());
        user.setFamily_name(title);
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void selectImage(View v) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        startActivityForResult(i, 12);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12 && resultCode == RESULT_OK && data != null) {
            CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1, 1)
            .setBorderLineColor(R.color.colorSecondary)
            .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                image_uri = result.getUri();
                profileImage.setImageURI(image_uri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void sendVerificationEmail() {
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
@Override
public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            Toast.makeText(getApplicationContext(), "Email sent for verification", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Could not send email", Toast.LENGTH_SHORT).show();
        }
        }
        });
    }
}
