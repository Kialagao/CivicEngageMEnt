package com.gmail.kingarthuralagao.us.civicengagement.presentation.authentication.signup;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.gmail.kingarthuralagao.us.civicengagement.CivicEngagementApp;
import com.gmail.kingarthuralagao.us.civicengagement.presentation.authentication.IAuthenticationEventsListener;
import com.gmail.kingarthuralagao.us.civilengagement.databinding.FragmentSignUpBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;

import es.dmoral.toasty.Toasty;


public class SignUpFragment extends Fragment {

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
    }

    private final String TAG = getClass().getSimpleName();
    private final int GOOGLE_SIGN_IN = 200;
    private GoogleSignInClient googleSignInClient;
    private FragmentSignUpBinding binding;
    private IAuthenticationEventsListener iAuthenticationEventsListener;
    private FirebaseAuth firebaseAuth;
    private SignUpFragmentViewModel signUpFragmentViewModel;
    private Boolean emailLayoutHasFocus = false;
    private Boolean isValidEmail = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = ((CivicEngagementApp) getActivity().getApplication()).getAuthInstance();

        SignUpFragmentViewModelFactory factory = new SignUpFragmentViewModelFactory(getActivity().getApplication());
        signUpFragmentViewModel = new ViewModelProvider(this, factory).get(SignUpFragmentViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container, false);
        binding.nameLayout.setEndIconVisible(false);
        binding.emailLayout.setEndIconVisible(false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpEvents();
        subscribeToLiveData();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof IAuthenticationEventsListener) {
            iAuthenticationEventsListener = (IAuthenticationEventsListener) context;
        } else {
            throw new RuntimeException("Must Implement iAuthenticationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        iAuthenticationEventsListener = null; // Prevent Memory Leak
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void subscribeToLiveData() {
        signUpFragmentViewModel.isEmailTakenResponse.observe(this, booleanResource -> {
            switch (booleanResource.getStatus()) {
                case LOADING:
                    iAuthenticationEventsListener.onStartLoading("Verifying Email");
                    break;
                case SUCCESS:
                    Log.d(TAG, "Result: " + booleanResource.getData().toString());
                    iAuthenticationEventsListener.onStopLoading();
                    if (booleanResource.getData() == true) {
                        binding.emailLayout.setErrorEnabled(true);
                        binding.emailLayout.setError("Email is already in use");
                        isValidEmail = false;
                    } else {
                        binding.emailLayout.setEndIconVisible(true);
                        isValidEmail = true;
                    }
                    setSignUpButtonStatus();
                    break;
                case ERROR:
                    Log.d(TAG, "Error" );
                    if (booleanResource.getError().getMessage() == "Invalid Email") {
                        binding.emailLayout.setErrorEnabled(true);
                        binding.emailLayout.setError("Invalid Email");
                    } else {
                        iAuthenticationEventsListener.onStopLoading();
                    }
                    binding.emailLayout.setEndIconVisible(false);
                    isValidEmail = false;
                    setSignUpButtonStatus();
                    break;
                default:
                    Log.d(TAG, "Created");
                    break;
            }
        });

        signUpFragmentViewModel.googleSignInResponse.observe(this, resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    iAuthenticationEventsListener.onStartLoading("Initializing Sign Up Process");
                    break;
                case SUCCESS:
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    googleSignInClient.signOut();
                    if (resource.getData().isNewUser()) {
                        initializeUser(user, user.getDisplayName());
                    } else {
                        iAuthenticationEventsListener.onStopLoading();
                        updateUI(user);
                    }
                    break;
                case ERROR:
                    Log.w(TAG, "signInWithCredential:failure", resource.getError());
                    Snackbar.make(binding.getRoot(), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    iAuthenticationEventsListener.onStopLoading();
                    break;
                default:
                    Log.d(TAG, "Created");
                    break;
            }
        });

        signUpFragmentViewModel.createNewUserResponse.observe(this, firebaseUserResource -> {
            switch (firebaseUserResource.getStatus()) {
                case LOADING:
                    iAuthenticationEventsListener.onStartLoading("Initializing Sign Up Process");
                    break;
                case SUCCESS:
                    FirebaseUser user = firebaseUserResource.getData();
                    initializeUser(user, binding.nameEt.getText().toString());
                    break;
                case ERROR:
                    Log.w(TAG, "createUserWithEmail:failure", firebaseUserResource.getError());
                    Toasty.error(requireActivity(), firebaseUserResource.getError().getMessage(), Toast.LENGTH_LONG, true).show();
                    iAuthenticationEventsListener.onStopLoading();
                    break;
                default:
                    Log.d(TAG, "Created");
                    break;
            }
        });

        signUpFragmentViewModel.initializeUserResponse.observe(this, statusResource -> {
            switch (statusResource.getStatus()) {
                case LOADING:
                    iAuthenticationEventsListener.onSetLoadingText("Creating User Account");
                    break;
                case SUCCESS:
                    //iAuthenticationEventsListener.onStopLoading();
                    updateUI(firebaseAuth.getCurrentUser());
                    break;
                case ERROR:
                    Toasty.error(requireActivity(), "Error Creating Account", Toast.LENGTH_SHORT, true);
                    iAuthenticationEventsListener.onStopLoading();
                    break;
                default:
                    Log.d(TAG, "Created");
                    break;
            }
        });
    }

    private void setUpEvents() {
        binding.signInTv.setOnClickListener(view -> iAuthenticationEventsListener.onSwitchToSignIn());

        binding.googleSignInBtn.setOnClickListener(view -> initializeGoogleSignIn());

        binding.twitterSignInBtn.setOnClickListener(view -> initializeTwitterSignIn());

        binding.signUpBtn.setOnClickListener(view -> createUser(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString()));

        addTextWatchers();
    }

    private void addTextWatchers() {
        binding.nameEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.nameLayout.setEndIconVisible(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    binding.nameLayout.setEndIconVisible(true);
                } else {
                    binding.nameLayout.setEndIconVisible(false);
                }
                setSignUpButtonStatus();
            }
        });

        binding.passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setSignUpButtonStatus();
            }
        });

        binding.emailEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.emailLayout.setError("");
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                setSignUpButtonStatus();
            }
        });

        binding.emailEt.setOnFocusChangeListener((view, hasFocus) -> {
            if (emailLayoutHasFocus && binding.emailEt.getText().length() != 0) {
                signUpFragmentViewModel.checkIfEmailTaken(binding.emailEt.getText().toString());
            }
            emailLayoutHasFocus = hasFocus;
        });
    }

    /******************************* Password-based SignUp **********************************/

    private void setSignUpButtonStatus() {
        if (hasEmptyFields() || !inputConditionsMet())
            disableSignUpButton();
        else
            enableSignUpButton();
    }

    private boolean hasEmptyFields() {
        return binding.nameEt.getText().length() == 0
                || binding.emailEt.getText().length() == 0
                || binding.passwordEt.getText().length() == 0;
    }

    private boolean inputConditionsMet() {
        return binding.passwordEt.getText().length() >= 6
                && isValidEmail;
    }

    private void enableSignUpButton() {
        binding.signUpBtn.setEnabled(true);
    }

    private void disableSignUpButton() {
        binding.signUpBtn.setEnabled(false);
    }

    private void createUser(String email, String password) {
        signUpFragmentViewModel.createUserWithEmailAndPasswordUseCase(email, password);
    }

    private void initializeUser(FirebaseUser user, String name) {
        signUpFragmentViewModel.initializeUser(user, name);
    }

    /******************************* End of Password-based SignUp **********************************/

    /********************************** Google SignIn **********************************/

    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("156929816574-8mk4c2us7cngbe8ach8beu2ghcu7npnu.apps.googleusercontent.com")
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.

            if (account != null) {
                signUpFragmentViewModel.initializeSignUpWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode() + " " + e.getMessage());
        }
    }
    /********************************** End of Google SignIn **********************************/

    /********************************** Twitter SignIn **********************************/

    private void initializeTwitterSignIn() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        iAuthenticationEventsListener.onStartLoading("Initializing Sign Up Process");

        Task<AuthResult> pendingResultTask = firebaseAuth.getPendingAuthResult();
        if (pendingResultTask != null) {
            finishSignIn(pendingResultTask); // There's something already here! Finish the sign-in for your user.
        } else {
            startSignInFlow(provider);
        }
    }

    private void startSignInFlow(OAuthProvider.Builder provider) {
        firebaseAuth
                .startActivityForSignInWithProvider(requireActivity(), provider.build())
                .addOnSuccessListener(
                        authResult -> {
                            Log.i(TAG, authResult.getAdditionalUserInfo().getProfile().toString());
                            // User is signed in.
                            // IdP data available in
                            // authResult.getAdditionalUserInfo().getProfile().
                            // The OAuth access token can also be retrieved:
                            // authResult.getCredential().getAccessToken().
                            // The OAuth secret can be retrieved by calling:
                            // authResult.getCredential().getSecret().
                            if (authResult.getAdditionalUserInfo().isNewUser()) {
                                initializeUser(authResult.getUser(), authResult.getUser().getDisplayName());
                            } else {
                                iAuthenticationEventsListener.onStopLoading();
                                updateUI(authResult.getUser());
                            }
                        })
                .addOnFailureListener(
                        exception -> {
                            Log.e(TAG, exception.getMessage());
                            Toasty.error(requireActivity(), "Invalid Credentials", Toast.LENGTH_SHORT, true).show();
                            iAuthenticationEventsListener.onStopLoading();
                        });
    }

    private void finishSignIn(Task<AuthResult> pendingResultTask) {
        pendingResultTask
                .addOnSuccessListener(
                        authResult -> {
                            if (authResult.getAdditionalUserInfo().isNewUser()) {
                                initializeUser(authResult.getUser(), authResult.getUser().getDisplayName());
                            } else {
                                iAuthenticationEventsListener.onStopLoading();
                                updateUI(authResult.getUser());
                            }
                        })
                .addOnFailureListener(
                        exception -> {
                            Log.e(TAG, exception.getMessage());
                            Toasty.error(requireActivity(), "Invalid Credentials", Toast.LENGTH_SHORT, true).show();
                            iAuthenticationEventsListener.onStopLoading();
                        });
    }

    /********************************** End of Google Twitter SignIn **********************************/


    private void updateUI(FirebaseUser user) {
        iAuthenticationEventsListener.navigateToHome();
    }
}