package application.mobile.smarthouse


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import application.mobile.smarthouse.databinding.ActivityCreateHomeBinding
import application.mobile.smarthouse.ui.home.HomeFragment
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

class CreateHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateHomeBinding
    val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityCreateHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


        var myTask : TimerTask? =null


        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if ( s!!.length < 3 || s!!.length>=25) {
                        myTask?.cancel()

                        myTask = object : TimerTask() {
                            override fun run() {
                                runOnUiThread {
                                    if(s!!.length <= 3 )
                                        binding.homeErrorInput.text = getString(R.string.short_home_name_error)
                                    else
                                        binding.homeErrorInput.text = getString(R.string.long_home_name_error)

                                    binding.homeErrorInput.visibility = View.VISIBLE
                                }
                            }
                        }
                        timer.schedule(myTask, 1000)
                    } else {
                        myTask?.cancel()
                        binding.homeErrorInput.visibility = View.INVISIBLE
                        binding.errorInput.visibility = View.GONE
                        binding.homeNameInput.background = ContextCompat.getDrawable(this@CreateHomeActivity,R.drawable.rounded_selected_edit_text)
                    }
                }catch (_:Exception){

                }

            }

            override fun afterTextChanged(s: Editable?) {
            }
        }




        binding.homeNameInput.addTextChangedListener(textWatcher)

        binding.createHomeBtn.setOnClickListener{

            val home_name: String = binding.homeNameInput.text.toString().trim()

            if(home_name.length in 3..25 && !binding.agreeCheckbox.isActivated)
            {
                uploadHomeToDatabase(home_name)
            }
            else{
                if(binding.agreeCheckbox.isActivated){
                    binding.errorInput.visibility = View.VISIBLE
                }
                    if (home_name.length <= 3)
                        binding.homeErrorInput.text = getString(R.string.short_home_name_error)
                    else
                        binding.homeErrorInput.text = getString(R.string.long_home_name_error)

            }

        }
    }

    private fun uploadHomeToDatabase(home_name: String){
        val home_id = UUID.randomUUID().toString()

        val home = hashMapOf(
            "home_id" to home_id,
            "user_id" to UserInfo.user_id,
            "home_name" to home_name
        )

        GlobalObj.db.collection("homes").add(home)

            UserInfo.selected_home = home_id

            val userReference = GlobalObj.db.collection("users").document(UserInfo.user_id)
            userReference.update("selected_home", home_id)
            .addOnCompleteListener {
                UserInfo.selected_home = home_id
                val intent = Intent(this, BaseActivity::class.java)
                intent.putExtra("home_name", home_name)
                // HomeFragment().arguments?.getString(home_name)
                startActivity(intent)
            }
    }


    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
        }
    }
}