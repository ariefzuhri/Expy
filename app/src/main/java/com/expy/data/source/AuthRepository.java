package com.expy.data.source;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.expy.R;
import com.expy.utils.Event;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class AuthRepository {
    private final String TAG = getClass().getSimpleName();

    private final Application application;
    private final FirebaseAuth firebaseAuth;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoading(){
        return _isLoading;
    }

    private final MutableLiveData<FirebaseUser> _user = new MutableLiveData<>();
    public MutableLiveData<FirebaseUser> getUser() {
        return _user;
    }

    private final MutableLiveData<Event<String>> _toastText = new MutableLiveData<>();
    public MutableLiveData<Event<String>> getToastText(){
        return _toastText;
    }

    private volatile static AuthRepository INSTANCE = null;

    private AuthRepository (Application application){
        this.application = application;
        firebaseAuth = FirebaseAuth.getInstance();
        _user.postValue(firebaseAuth.getCurrentUser());
    }

    public static AuthRepository getInstance(Application application){
        if (INSTANCE == null) {
            synchronized (AuthRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AuthRepository(application);
                }
            }
        }
        return INSTANCE;
    }

    public void authWithGoogle(AuthCredential authCredential){
        _isLoading.postValue(true);
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.d(TAG, "signInWithCredential: success");
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                _user.postValue(firebaseUser);
            } else {
                Log.w(TAG, "signInWithCredential: failure", task.getException());
            }
            _isLoading.postValue(false);
        });
    }

    public void registerWithEmail(String name, String email, String password){
        _isLoading.postValue(true);
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.d(TAG, "createUserWithEmail: success");
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                updateName(name);
                sendEmailVerification();
                _user.postValue(firebaseUser);
            } else {
                _toastText.postValue(new Event<>("Email sudah terdaftar"));
                Log.w(TAG, "createUserWithEmail: failure", task.getException());
            }
            _isLoading.postValue(false);
        });
    }

    public void loginWithEmail(String email, String password){
        _isLoading.postValue(true);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                Log.d(TAG, "signInWithEmail: success");
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                _user.postValue(firebaseUser);
            } else {
                _toastText.postValue(new Event<>("Kata sandi salah"));
                Log.w(TAG, "signInWithEmail: failure", task.getException());
            }
            _isLoading.postValue(false);
        });
    }

    private void sendEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            _isLoading.postValue(true);
            firebaseUser.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    _toastText.postValue(new Event<>("Cek email untuk memverifikasi akunmu"));
                    Log.d(TAG, "sendEmailVerification: success");
                }
                else Log.w(TAG, "sendEmailVerification: failure", task.getException());
                _isLoading.postValue(false);
            });
        }
    }

    public void sendPasswordReset(String email){
        _isLoading.postValue(true);
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                _toastText.postValue(new Event<>("Cek email untuk ganti kata sandi"));
                Log.d(TAG, "sendPasswordReset: success");
            } else {
                _toastText.postValue(new Event<>("Email belum terdaftar"));
                Log.w(TAG, "sendPasswordReset: failure", task.getException());
            }
            _isLoading.postValue(false);
        });
    }

    public void updateName(String newName){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            _isLoading.postValue(true);
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();
            firebaseUser.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) Log.d(TAG, "updateName: success");
                else Log.w(TAG, "updateName: failure", task.getException());
                _isLoading.postValue(false);
            });
        }
    }

    public void updateProfile(Uri newProfile){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null){
            _isLoading.postValue(true);
            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(newProfile)
                    .build();
            firebaseUser.updateProfile(profileUpdate).addOnCompleteListener(task -> {
                if (task.isSuccessful()) Log.d(TAG, "updateProfile: success");
                else Log.w(TAG, "updateProfile: failure", task.getException());
                _isLoading.postValue(false);
            });
        }
    }

    public void logout(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(application.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(application, gso).signOut();
        firebaseAuth.signOut();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        _user.postValue(firebaseUser);
    }
}
