package com.gmail.kingarthuralagao.us.civicengagement.presentation.authentication.signup;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.gmail.kingarthuralagao.us.civicengagement.core.utils.InvalidEmailException;
import com.gmail.kingarthuralagao.us.civicengagement.core.utils.StateLiveData;
import com.gmail.kingarthuralagao.us.civicengagement.core.utils.Utils;
import com.gmail.kingarthuralagao.us.civicengagement.data.Status;
import com.gmail.kingarthuralagao.us.civicengagement.data.repository.authentication.signup.SignUpRepositoryImpl;
import com.gmail.kingarthuralagao.us.civicengagement.domain.usecase.authentication.signup.CheckIfEmailIsTakenUseCase;
import com.gmail.kingarthuralagao.us.civicengagement.domain.usecase.authentication.signup.CreateUserWithEmailAndPasswordUseCase;
import com.gmail.kingarthuralagao.us.civicengagement.domain.usecase.authentication.signup.SetUserDisplayNameUseCase;
import com.gmail.kingarthuralagao.us.civicengagement.domain.usecase.authentication.signup.SignUpWithGoogleUseCase;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;

public class SignUpFragmentViewModel extends AndroidViewModel {

    public StateLiveData<Boolean> isEmailTakenResponse = new StateLiveData<>();
    public StateLiveData<FirebaseUser> googleSignInResponse = new StateLiveData<>();
    public StateLiveData<FirebaseUser> createNewUserResponse = new StateLiveData<>();
    public StateLiveData<FirebaseUser> setUserDisplayNameResponse = new StateLiveData<>();

    private CheckIfEmailIsTakenUseCase checkIfEmailIsTakenUseCase =
            new CheckIfEmailIsTakenUseCase(SignUpRepositoryImpl.newInstance(getApplication()));

    private SignUpWithGoogleUseCase signUpWithGoogleUseCase =
            new SignUpWithGoogleUseCase(SignUpRepositoryImpl.newInstance(getApplication()));

    private CreateUserWithEmailAndPasswordUseCase createUserWithEmailAndPasswordUseCase =
            new CreateUserWithEmailAndPasswordUseCase(SignUpRepositoryImpl.newInstance(getApplication()));

    private SetUserDisplayNameUseCase setUserDisplayNameUseCase =
            new SetUserDisplayNameUseCase(SignUpRepositoryImpl.newInstance(getApplication()));

    public SignUpFragmentViewModel(@androidx.annotation.NonNull Application application) {
        super(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        checkIfEmailIsTakenUseCase.dispose();
        signUpWithGoogleUseCase.dispose();
        createUserWithEmailAndPasswordUseCase.dispose();
        setUserDisplayNameUseCase.dispose();
    }

    public void checkIfEmailTaken(String email) {
        if (Utils.isValidEmail(email)) {
            isEmailTakenResponse.postLoading();

            DisposableObserver<Boolean> disposableObserver = new DisposableObserver<Boolean>() {
                @Override
                public void onNext(@NonNull Boolean aBoolean) {
                    isEmailTakenResponse.postSuccess(aBoolean);
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    isEmailTakenResponse.postError(e);
                }

                @Override
                public void onComplete() {}
            };

            checkIfEmailIsTakenUseCase.execute(disposableObserver, CheckIfEmailIsTakenUseCase.Params.forCheckEmail(email));
        } else {
            isEmailTakenResponse.postError(new InvalidEmailException("Invalid Email"));
        }
    }

    public void initializeSignUpWithGoogle(String idToken) {
        googleSignInResponse.postLoading();

        DisposableObserver<FirebaseUser> disposableObserver = new DisposableObserver<FirebaseUser>() {
            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull FirebaseUser firebaseUser) {
                googleSignInResponse.postSuccess(firebaseUser);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                googleSignInResponse.postError(e);
            }

            @Override
            public void onComplete() {}
        };

        signUpWithGoogleUseCase.execute(disposableObserver, SignUpWithGoogleUseCase.Params.signUpWithGoogle(idToken));
    }

    public void createUserWithEmailAndPasswordUseCase(String email, String password) {
        createNewUserResponse.postLoading();

        DisposableObserver<FirebaseUser> disposableObserver = new DisposableObserver<FirebaseUser>() {
            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull FirebaseUser firebaseUser) {
                createNewUserResponse.postSuccess(firebaseUser);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                createNewUserResponse.postError(e);
            }

            @Override
            public void onComplete() {}
        };

        createUserWithEmailAndPasswordUseCase.execute(disposableObserver,
                CreateUserWithEmailAndPasswordUseCase.Params.createUserWithEmailAndPassword(email,password));
    }

    public void setUserDisplayName(FirebaseUser user, String name) {
        setUserDisplayNameResponse.postLoading();

        DisposableObserver<FirebaseUser> disposableObserver = new DisposableObserver<FirebaseUser>() {
            @Override
            public void onNext(@io.reactivex.rxjava3.annotations.NonNull FirebaseUser user) {
                setUserDisplayNameResponse.postSuccess(user);
            }

            @Override
            public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                setUserDisplayNameResponse.postError(e);
            }

            @Override
            public void onComplete() {}
        };

        setUserDisplayNameUseCase.execute(disposableObserver,
                SetUserDisplayNameUseCase.Params.setUserDisplayName(name, user));
    }
}