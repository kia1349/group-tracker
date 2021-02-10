package com.example.grouptracker.Main.Authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.grouptracker.Main.Maps.MapsActivity;
import com.example.grouptracker.Model.Group;
import com.example.grouptracker.Model.User;
import com.example.grouptracker.R;
import com.example.grouptracker.Utils.Common;
import com.example.grouptracker.Utils.UserClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.Serializable;


public class SignInActivity extends AppCompatActivity {

    public static final String TAG ="SignInActivity";

    private TextInputLayout mEmailInput, mPasswordInput;
    FirebaseAuth auth;
    FirebaseUser firebaseUser;
    DatabaseReference user_information;
    ProgressDialog pd;
    Toolbar toolbar;
    private FirebaseFirestore mDb;
    User user;
    Group group;
    private String admin;
    private String name;
    private String surrname;
    private String email;
    private String gender;
    private String isSharing;
    private String imageUri;
    private String userId;
    private String date;
    private String family;
    private String family_name;
    private int locationUpdateInterval;
    private String title;
    private String code;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        auth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondaryLight));

        mEmailInput = findViewById(R.id.emailLoginInput);
        mPasswordInput = findViewById(R.id.passLoginInput);
        MaterialButton login = findViewById(R.id.sign_in);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mPasswordInput.getEditText().setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                mPasswordInput.setError("");
                mPasswordInput.setErrorEnabled(false);
                return false;
            }
        });

        pd = new ProgressDialog(this);
        pd.setMessage("Logging In...");
        user_information = FirebaseDatabase.getInstance().getReference(Common.USER_INFORMATION);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);
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
                Intent previous = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(previous);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void getUserDetails() {
        name = user.getName();
        surrname = user.getSurrname();
        email = user.getEmail();
        gender = user.getGender();
        isSharing = user.getIsSharing();
        imageUri = user.getImageUri();
        userId = user.getUserid();
        date = user.getDate();
        family = user.getFamily();
        family_name = user.getFamily_name();
        locationUpdateInterval = user.getLocationUpdateInterval();
    }

    private void getUser() {
        final DocumentReference userDocumentReference = mDb
                .collection(getString(R.string.collection_users))
                .document(firebaseUser.getUid());

        userDocumentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user = task.getResult().toObject(User.class);
                    getUserDetails();
                    getGroupDetails();
                    ((UserClient) getApplicationContext()).setUser(user);
                    goToMapsActivity();
                }
            }
        });
    }

    public void goToMapsActivity() {
        Intent intent = new Intent(SignInActivity.this, MapsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user", (Serializable) user);
        bundle.putSerializable("group", (Serializable) group);
        //bundle.putSerializable("place", (Serializable) home);
        //bundle.putSerializable("groupsList", (Serializable) groupsList);
        //bundle.putSerializable("userPlaces", (Serializable) placeList);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    private void getGroupDetails() {
        final DocumentReference groupDocumentReference = mDb
                .collection(getString(R.string.collection_groups))
                .document(user.getFamily());

        groupDocumentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()) {
                    group = documentSnapshot.toObject(Group.class);
                    getGroup();
                }
            }
        });
    }

    private void getGroup() {
        admin = group.getGroup_admin();
        title = group.getGroup_title();
        code = group.getGroup_code();
        groupId = group.getGroup_id();
    }


    public void signIn() {
        pd.show();
        String email = mEmailInput.getEditText().getText().toString();
        String pass = mPasswordInput.getEditText().getText().toString();

        if(email.equals("")) {
            pd.dismiss();
            mPasswordInput.setError("");
            mPasswordInput.setErrorEnabled(false);
            mEmailInput.setError("Email is required!");
            mEmailInput.requestFocus();
        } else if(pass.equals("")) {
            pd.dismiss();
            mEmailInput.setError("");
            mEmailInput.setErrorEnabled(false);
            mPasswordInput.setError("Password is required!");
            mPasswordInput.requestFocus();
        } else {
            mEmailInput.setError("");
            mEmailInput.setErrorEnabled(false);
            mPasswordInput.setError("");
            mPasswordInput.setErrorEnabled(false);
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                firebaseUser = task.getResult().getUser();
                                if (!firebaseUser.isEmailVerified()) {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(), "Email is not verified yet, check your inbox.", Toast.LENGTH_LONG).show();
                                } else {
                                    getUser();
                                }
                            } else {
                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                                switch (errorCode) {
                                    case "ERROR_INVALID_EMAIL":
                                        mEmailInput.setError("The email address is badly formatted.");
                                        mEmailInput.requestFocus();
                                        break;

                                    case "ERROR_WRONG_PASSWORD":
                                        mPasswordInput.setError("The password is invalid.");
                                        mPasswordInput.requestFocus();
                                        mPasswordInput.getEditText().setText("");
                                        break;

                                    case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                        Toast.makeText(SignInActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                        Toast.makeText(SignInActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "ERROR_USER_DISABLED":
                                        Toast.makeText(SignInActivity.this, "The user account has been disabled by an administrator.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "ERROR_USER_TOKEN_EXPIRED":
                                        Toast.makeText(SignInActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                        break;

                                    case "ERROR_USER_NOT_FOUND":
                                        mEmailInput.setError("User with this email does not exist.");
                                        mEmailInput.requestFocus();
                                        break;
                                }
                            }
                        }
                    });
        }
        pd.dismiss();
    }

    public void showRecoverPasswordDialog(View v) {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);
        //view to set in dialog
        final EditText emailEntry = new EditText(this);
        emailEntry.setHint("Email");
        emailEntry.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEntry.setMinEms(10);

        linearLayout.addView(emailEntry);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String recoveryEmail = emailEntry.getText().toString().trim();
                beginRecovery(recoveryEmail);
            }
        });
        //buttons cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String emailr) {
        //show progress dialog

        auth.sendPasswordResetEmail(emailr)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SignInActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignInActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //get and show proper error message
                Toast.makeText(SignInActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void register(View v) {
        Intent myIntent = new Intent(SignInActivity.this, SignUpActivity.class);
        startActivity(myIntent);
    }

    @Override
    public void onBackPressed() {
        Intent previous = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(previous);
    }

    private void updateToken(final FirebaseUser firebaseUser) {
        final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Common.TOKENS);

        // Get token
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        tokens.child(firebaseUser.getUid())
                                .setValue(instanceIdResult.getToken());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignInActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}