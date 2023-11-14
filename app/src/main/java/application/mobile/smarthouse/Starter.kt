package application.mobile.smarthouse

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import application.mobile.smarthouse.databinding.ActivityStarterBinding
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
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.ByteArrayOutputStream
import java.lang.Exception


const val RS_SIGN_IN = 1
const val RS_SIGN_IN_FAIL = -1

class Starter : AppCompatActivity() {

    private lateinit var binding: ActivityStarterBinding
    private lateinit var sharedPreferences: SharedPreferences



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityStarterBinding.inflate(layoutInflater)
        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        UserInfo.initializationUser(this@Starter) {

            if (UserInfo.user_id != "" && UserInfo.name != "" && UserInfo.email != "") {
                if(UserInfo.homes.isNotEmpty()) {
                    val intent = Intent(this@Starter, BaseActivity::class.java)
                    startActivity(intent)
                }
                else
                {
                    val intent = Intent(this@Starter, CreateHomeActivity::class.java)
                    startActivity(intent)
                }
            }
            else {

                setContentView(binding.root)
//                supportActionBar?.setDisplayShowTitleEnabled(false)
//                supportActionBar?.hide()

                val callbackManager = CallbackManager.Factory.create()


                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestServerAuthCode(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()


                val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
                LoginManager.getInstance().logOut()
                mGoogleSignInClient.signOut()

                binding.getStartedGoogle.setOnClickListener {
                    val signInIntent = mGoogleSignInClient.signInIntent
                    startActivityForResult(signInIntent, 1)
                }

                binding.getStartedFacebook.registerCallback(
                    callbackManager,
                    object : FacebookCallback<LoginResult> {

                        override fun onSuccess(result: LoginResult) {

                            val accessToken = result.accessToken
                            val request =  GraphRequest.newMeRequest(accessToken) { jsonObject, _ ->
                                val userId = jsonObject?.getString("id")
                                val userName = jsonObject?.getString("name")
                                val userEmail = jsonObject?.getString("email")
                                val imageUrl = "https://graph.facebook.com/$userId/picture?type=large"
                              
                                val image = GlobalObj.storage.child("profilePhoto/${userId.toString()}/profileph.jpg")

                                val editor = sharedPreferences.edit()
                                editor.putString("userId", userId)
                                editor.putString("userName", userName)
                                editor.putString("userEmail", userEmail)
                                editor.apply()

                                uploadProfileImage(userId.toString(),imageUrl)

                                UserInfo.saveProfileImage(this@Starter, image){}


                                UserInfo.initializationUser(this@Starter) {

                                    if(UserInfo.user_id =="" && UserInfo.name == "" && UserInfo.email == "") {

                                        val user = hashMapOf(
                                            "user_id" to userId,
                                            "name" to userName,
                                            "email" to userEmail,
                                        )

                                        GlobalObj.db.collection("users").add(user)

                                        UserInfo.initializationUser(this@Starter) {
                                            val intent =
                                                Intent(this@Starter, CreateHomeActivity::class.java)
                                            startActivity(intent)
                                        }
                                    }
                                    else{
                                        val intent =
                                            Intent(this@Starter, BaseActivity::class.java)
                                        startActivity(intent)
                                    }
                                }
                            }
                            val parameters = Bundle()
                            parameters.putString("fields", "id,name,email,picture.type(large)")
                            request.parameters = parameters
                            request.executeAsync()
                        }


                        override fun onCancel() {
                        }

                        override fun onError(error: FacebookException) {
                            Toast.makeText(this@Starter, "" + error, Toast.LENGTH_SHORT).show()
                        }
                    })


            }
        }

    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {

            val account = completedTask.getResult(ApiException::class.java)
            val image = GlobalObj.storage.child("profilePhoto/${account.id.toString()}/profileph.jpg")

            uploadProfileImage(account.id.toString(),account.photoUrl.toString())

            UserInfo.saveProfileImage(this, image) {

                val editor = sharedPreferences.edit()
                editor.putString("userId", account.id)
                editor.putString("userName", account.displayName)
                editor.putString("userEmail", account.email)
                editor.putString("imagePath", UserInfo.profile_ph)
                editor.apply()


                UserInfo.initializationUser(this@Starter) {

                    if (UserInfo.user_id == "" && UserInfo.name == "" && UserInfo.email == "") {
                        val user = hashMapOf(
                            "user_id" to account.id,
                            "name" to account.displayName,
                            "email" to account.email,
                        )
                        GlobalObj.db.collection("users").add(user)

                        UserInfo.initializationUser(this) {
                            val intent =
                                Intent(this, CreateHomeActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        if (UserInfo.homes.isNotEmpty()) {
                            val intent =
                                Intent(this, BaseActivity::class.java)
                            startActivity(intent)
                        } else {
                            val intent =
                                Intent(this, CreateHomeActivity::class.java)
                            startActivity(intent)
                        }
                    }

                }

            }
        } catch (e: ApiException) {
            Toast.makeText(this,"signInResult:failed code=" + e.statusCode,Toast.LENGTH_SHORT).show()
        }

    }

    fun uploadProfileImage(userId: String, imageUrl: String) {
        Picasso.get()
            .load(imageUrl)
            .into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    if (bitmap != null) {
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val imageData: ByteArray = baos.toByteArray()

                        val photoRef = GlobalObj.storage.child("profilePhoto/$userId/profileph.jpg")

                        photoRef.putBytes(imageData)
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener { e ->

                            }
                    }
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                }
            })
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode) {
            RS_SIGN_IN -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
            RS_SIGN_IN_FAIL -> {
                Toast.makeText(this, "login failed", Toast.LENGTH_SHORT).show()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }

}