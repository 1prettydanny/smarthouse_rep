package application.mobile.smarthouse


import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import application.mobile.smarthouse.databinding.ActivityCreateHomeBinding
import application.mobile.smarthouse.ui.Profile.ProfileFragment
import java.util.UUID

class CreateHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityCreateHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        val textWatcher = GlobalFun.getTextWatcher(binding.homeNameInput,binding.homeErrorInput, this)


            binding.cancelBtn.setOnClickListener {
                    onBackPressedCallback.handleOnBackPressed()
            }

        binding.homeNameInput.addTextChangedListener(textWatcher)

        binding.agreeCheckbox.setOnClickListener {
            binding.errorAgree.visibility = View.GONE
        }

        binding.createHomeBtn.setOnClickListener{

            val homeName: String = binding.homeNameInput.text.toString().trim()

            if(homeName.length in 3..25 && binding.agreeCheckbox.isChecked)
            {
                uploadHomeToDatabase(homeName)
            }
            else{
                if(!binding.agreeCheckbox.isChecked)
                    binding.errorAgree.visibility = View.VISIBLE
                if (homeName.length <= 3)
                    binding.homeErrorInput.text = getString(R.string.short_home_name_error)
                else
                    binding.homeErrorInput.text = getString(R.string.long_home_name_error)

            }

        }
    }

    private fun uploadHomeToDatabase(homeName: String){
        val homeId = UUID.randomUUID().toString()

        val home = hashMapOf(
            "home_id" to homeId,
            "user_id" to UserInfo.user_id,
            "home_name" to homeName
        )

        GlobalObj.db.collection("homes").add(home)

                UserInfo.selected_home_id = homeId
                UserInfo.selected_home_name = homeName
                UserInfo.changeSelectedHome()
                    val intent = Intent(this, BaseActivity::class.java)
                    val anim = ActivityOptions.makeCustomAnimation(
                        this,
                        R.anim.slide_in_down,
                        R.anim.slide_in_down
                    )
                    startActivity(intent, anim.toBundle())

    }


    override fun onDestroy() {
        GlobalFun.stopTimer()
        super.onDestroy()
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (UserInfo.selected_home_id =="") {
                UserInfo.clearInfo()
                GlobalObj.auth.signOut()
                val intent = Intent(this@CreateHomeActivity, Starter::class.java)
                val animator = ActivityOptions.makeCustomAnimation(this@CreateHomeActivity, R.anim.slide_in_down, R.anim.stay)
                startActivity(intent, animator.toBundle())
            }
            else
            {
                val intent = Intent(this@CreateHomeActivity, BaseActivity::class.java)
                val animator = ActivityOptions.makeCustomAnimation(this@CreateHomeActivity, R.anim.slide_in_up, R.anim.slide_out_up)
                startActivity(intent, animator.toBundle())
            }
        }
    }
}