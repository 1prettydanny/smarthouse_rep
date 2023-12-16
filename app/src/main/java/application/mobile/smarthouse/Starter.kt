package application.mobile.smarthouse

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import application.mobile.smarthouse.databinding.ActivityStarterBinding
import application.mobile.smarthouse.ui.home.HomeFragment
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream


class Starter : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        UserInfo.initializationUser(this@Starter) {
            if (currentUser == null) {

                binding = ActivityStarterBinding.inflate(layoutInflater)
                sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

                LoginManager.getInstance().logOut()
                setContentView(binding.root)

                val callbackManager = CallbackManager.Factory.create()


                with(binding) {

                    getStartedGoogle.setOnClickListener {

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.default_web_client_id))
                            .requestServerAuthCode(getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()

                        val mGoogleSignInClient = GoogleSignIn.getClient(this@Starter, gso)
                        mGoogleSignInClient.signOut()

                        val signInIntent = mGoogleSignInClient.signInIntent
                        startActivityForResult(signInIntent, RS_SIGN_IN)
                    }

                    getStartedFacebook.registerCallback(callbackManager,
                        object : FacebookCallback<LoginResult> {
                            override fun onSuccess(result: LoginResult) {
                                handleFacebookAccessToken(result.accessToken)
                            }

                            override fun onCancel() {
                                // User cancelled the login
                            }

                            override fun onError(error: FacebookException) {
                                Toast.makeText(this@Starter, "" + error, Toast.LENGTH_SHORT).show()
                            }
                        })

                }
            } else {

                val nextIntent = if (UserInfo.homes.isEmpty()) {

                    Intent(this@Starter, CreateHomeActivity::class.java)
                } else {
                    Intent(this@Starter, BaseActivity::class.java)
                }
               startActivity(nextIntent)

            }
        }
    }
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    saveUserDataToDatabase(user)
                } else {
                    Toast.makeText(this@Starter, "Err: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode) {
            RS_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleGoogleSignInResult(task)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            Toast.makeText(this,"signInResult:failed code=" + e.statusCode,Toast.LENGTH_SHORT).show()
        }
    }


        private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        saveUserDataToDatabase(user)
                    } else {
                        Toast.makeText(this, ""+task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        private fun saveUserDataToDatabase(user: FirebaseUser?) {
            if (user != null) {


                val image = GlobalObj.storage.child("profilePhoto/${user.uid.toString()}/profileph.jpg")
                uploadProfileImage(user.photoUrl.toString(), image)

                    val editor = sharedPreferences.edit()
                    editor.putString("userId", user.uid)
                    editor.putString("userName", user.displayName)
                    editor.putString("userEmail", user.email)
                    editor.apply()


                saveUser(user.uid, user.displayName, user.email) {

                    UserInfo.initializationUser(this@Starter) {
                        val nextIntent: Intent

                        if (UserInfo.homes.isEmpty()) {
                            nextIntent = Intent(this@Starter, CreateHomeActivity::class.java)
                        } else {
                            nextIntent = Intent(this@Starter, BaseActivity::class.java)
                        }

                        startActivity(nextIntent)
                    }
                }
            }
        }

    fun saveUser(id: String, name: String?, email: String?, callback: () -> Unit){

        val userReference = GlobalObj.db.collection("users").document(id)

        userReference.get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    val userfordb = hashMapOf(
                        "user_id" to id,
                        "name" to name,
                        "email" to email,
                    )
                    userReference.set(userfordb)
                }
                callback()
            }
    }

    fun uploadProfileImage(imageUrl: String, image: StorageReference) {
        Picasso.get()
            .load(imageUrl)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val imageData: ByteArray = baos.toByteArray()


                        image.putBytes(imageData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {

                            }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            })
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
        }
    }
    companion object {
        const val RS_SIGN_IN = 1
    }
}