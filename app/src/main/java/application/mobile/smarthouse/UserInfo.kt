package application.mobile.smarthouse
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.StorageReference
import java.io.File


object UserInfo {

    var user_id : String = ""
    var name  : String = ""
    var email : String = ""
    var homes : MutableList<String> = mutableListOf()
    var profile_ph: String = ""

    fun initializationUser(context:Context, callback: () -> Unit){

        sharedPreferences = context.getSharedPreferences("user_prefs", AppCompatActivity.MODE_PRIVATE)

        val userId = sharedPreferences.getString("userId", "")
        val userName = sharedPreferences.getString("userName", "")
        val userEmail = sharedPreferences.getString("userEmail", "")
        profile_ph = sharedPreferences.getString("imagePath", "").toString()

        GlobalObj.db.collection("users")
            .whereEqualTo("user_id", userId)
            .whereEqualTo("name", userName)
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                     user_id = document["user_id"].toString()
                     name = document["name"].toString()
                     email = document["email"].toString()
                    if(document["homes"] != null)
                     homes =  document["homes"] as MutableList<String>
                }
                callback()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "errInitU: "+exception.message , Toast.LENGTH_SHORT).show()
                callback()
            }
    }

    fun addhome(home_id: String){

        homes.add(home_id)

        GlobalObj.db.collection("users")
            .whereEqualTo("user_id", user_id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val documentId = document.id
                    val updates = mapOf(
                        "homes" to homes
                    )

                    GlobalObj.db.collection("users")
                        .document(documentId)
                        .update(updates)
                }
            }
    }

    fun saveProfileImage(context: Context, image: StorageReference, callback: () -> Unit) {

            val filePath = File(context.filesDir, "profileph.jpg")

            image.getFile(filePath).addOnSuccessListener {
                profile_ph = filePath.absolutePath
                callback()
            }
                .addOnFailureListener {
                    Toast.makeText(context, ""+ it.message, Toast.LENGTH_SHORT).show()
            }


    }

    fun clearInfo(){
        user_id = ""
        name   = ""
        email  = ""
        homes  = mutableListOf()
        profile_ph = ""
    }
}
private lateinit var sharedPreferences: SharedPreferences