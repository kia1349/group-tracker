package com.example.grouptracker.Main.Authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    FirebaseUser currentUser;
    DatabaseReference user_information;
    StorageReference storageReference;
    DatabaseReference groupReference;
    private FirebaseFirestore mDb;

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

        mDb = FirebaseFirestore.getInstance();

        auth = FirebaseAuth.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

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

    public void signUpUser(View v) {
        dialog.setMessage("Signing up user...");
        dialog.show();
        email = emailInput.getEditText().getText().toString();
        password = passInput.getEditText().getText().toString();
        password2 = pass2Input.getEditText().getText().toString();
        name = nameInput.getEditText().getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialog.dismiss();
            passInput.setError(null);
            passInput.setErrorEnabled(false);
            pass2Input.setError(null);
            pass2Input.setErrorEnabled(false);
            nameInput.setError(null);
            nameInput.setErrorEnabled(false);

            emailInput.setError("Invalid Email");
            emailInput.setFocusable(true);
        } else if (password.length() < 6) {
            dialog.dismiss();
            emailInput.setError(null);
            emailInput.setErrorEnabled(false);
            pass2Input.setError(null);
            pass2Input.setErrorEnabled(false);
            nameInput.setError(null);
            nameInput.setErrorEnabled(false);

            passInput.setError("Password length at least 6 characters");
            passInput.setFocusable(true);
        } else if (!password.matches(".*\\d.*")) {
            dialog.dismiss();
            emailInput.setError(null);
            emailInput.setErrorEnabled(false);
            pass2Input.setError(null);
            pass2Input.setErrorEnabled(false);
            nameInput.setError(null);
            nameInput.setErrorEnabled(false);

            passInput.setError("Password should also contain a number");
            passInput.setFocusable(true);
        } else if (!password.equals(password2)) {
            dialog.dismiss();
            emailInput.setError(null);
            emailInput.setErrorEnabled(false);
            passInput.setError(null);
            passInput.setErrorEnabled(false);
            nameInput.setError(null);
            nameInput.setErrorEnabled(false);

            pass2Input.setError("Passwords must match");
            pass2Input.setFocusable(true);
        } else if (name.equals("")) {
            dialog.dismiss();
            emailInput.setError(null);
            emailInput.setErrorEnabled(false);
            passInput.setError(null);
            passInput.setErrorEnabled(false);
            pass2Input.setError(null);
            pass2Input.setErrorEnabled(false);

            nameInput.setError("You must enter a name/nickname");
            nameInput.setFocusable(true);
        } else if (gender.equals("")) {
            dialog.dismiss();
            Toast.makeText(getApplicationContext(), "Please choose a gender", Toast.LENGTH_SHORT).show();
        } else {
            auth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            if (task.isSuccessful()) {
                                boolean check = !task.getResult().getSignInMethods().isEmpty();
                                // if email exists
                                if (check) {
                                    dialog.dismiss();
                                    passInput.setError(null);
                                    passInput.setErrorEnabled(false);
                                    pass2Input.setError(null);
                                    pass2Input.setErrorEnabled(false);
                                    nameInput.setError(null);
                                    nameInput.setErrorEnabled(false);

                                    emailInput.setError("A user with that email already exists");
                                    emailInput.setFocusable(true);
                                }
                            }
                        }
                    });
            registerUser();
        }
    }

    public void setGroup(final Group group) {
        final String current_user_id = currentUser.getUid();
        Query query = groupReference.orderByChild("userid").equalTo(current_user_id);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    User createUser = null;
                    for (DataSnapshot childDss : dataSnapshot.getChildren())
                    {
                        createUser = childDss.getValue(User.class);
                        String join_user_id = createUser.userid;

                        groupReference = FirebaseDatabase.getInstance().getReference()
                                .child(Common.USER_INFORMATION).child(join_user_id).child("MyGroups");

                        groupReference.child(join_user_id).setValue(group)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        } else {
                                            Toast.makeText(getApplicationContext(), "Could not join group, try again", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Group code is invalid", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    // Registers the user
    public void registerUser() {
        showDialog();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(image_uri != null) {
                                final StorageReference sr = storageReference.child(currentUser.getUid() + ".jpg");
                                sr.putFile(image_uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                        if(task.isSuccessful()) {
                                            sr.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    final String image_path = uri.toString();

                                                    DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                                                            .document(currentUser.getUid());

                                                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                            if (documentSnapshot.exists()) {
                                                                Toast.makeText(SignUpActivity.this, "This user already exists", Toast.LENGTH_SHORT).show();
                                                            } else if (!documentSnapshot.exists()) {
                                                                String surrname = name;
                                                                if (surrname.contains(" ")) {
                                                                    surrname = surrname.split(" ")[1];
                                                                }
                                                                String title = surrname + " Family";
                                                                group = new Group(title, currentUser.getUid(), currentUser.getUid());
                                                                code = generateCode();
                                                                group.setGroup_code(code);
                                                                User user = new User(name, surrname, email, gender, "false", image_path, currentUser.getUid(), date, 4000);
                                                                ((UserClient) getApplicationContext()).setUser(user);
                                                                uploadToCollection(user, group);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                                        .document(currentUser.getUid());

                                userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            ((UserClient) getApplicationContext()).setUser(documentSnapshot.toObject(User.class));
                                        } else if (!documentSnapshot.exists()) {
                                            String surrname = name;
                                            if (surrname.contains(" ")) {
                                                surrname = surrname.split(" ")[1];
                                            }
                                            String title = surrname + " Family";
                                            group = new Group(title, currentUser.getUid(), currentUser.getUid());
                                            code = generateCode();
                                            group.setGroup_code(code);
                                            User user = new User(name, surrname, email, gender, "false", "", currentUser.getUid(), date, 4000);
                                            ((UserClient) getApplicationContext()).setUser(user);
                                            uploadToCollection(user, group);
                                        }
                                    }
                                });
                            }
                        }
                        else {
                            Log.d("SignUpActiviy-Auth", "FAILED LOGIN");
                            Toast.makeText(SignUpActivity.this, "Something went wrong while creating user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

public void uploadToCollection(final User newUser, final Group family) {
    if (newUser != null) {
        DocumentReference userRef = mDb.collection(getString(R.string.collection_users))
                .document(FirebaseAuth.getInstance().getUid());

        userRef.set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (family != null) {
                    DocumentReference familyRef = mDb
                            .collection(getString(R.string.collection_groups))
                            .document(family.getGroup_id());

                    User user = ((UserClient) (getApplicationContext())).getUser();
                    familyRef.set(family); // Don't care about listening for completion.

                    DocumentReference userList = mDb.collection(getString(R.string.collection_groups))
                            .document(family.getGroup_id())
                            .collection(getString(R.string.collection_group_user_list))
                            .document(FirebaseAuth.getInstance().getUid());

                    userList.set(newUser);

                    DocumentReference usersGroupListRef = mDb
                            .collection(getString(R.string.collection_users))
                            .document(newUser.getUserid())
                            .collection(getString(R.string.collection_group_list))
                            .document(family.getGroup_id());

                    usersGroupListRef.set(family);
                }
                sendVerificationEmail();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
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
        currentUser.sendEmailVerification()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
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
