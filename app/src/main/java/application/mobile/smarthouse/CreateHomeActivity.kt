package application.mobile.smarthouse


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import application.mobile.smarthouse.databinding.ActivityCreateHomeBinding
import java.util.Timer
import java.util.TimerTask
import java.util.UUID

private lateinit var binding: ActivityCreateHomeBinding

class CreateHomeActivity : AppCompatActivity() {
    val timer = Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityCreateHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
                                        binding.errorInput.text = getString(R.string.short_home_name)
                                    else
                                        binding.errorInput.text = getString(R.string.long_home_name)

                                    binding.errorInput.visibility = View.VISIBLE
                                }
                            }
                        }
                        timer.schedule(myTask, 1000)
                    } else {
                        myTask?.cancel()
                        binding.errorInput.visibility = View.INVISIBLE
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

            if(home_name.length in 3..25)
            {
                val home_id = UUID.randomUUID().toString()


                val home = hashMapOf(
                    "home_id" to home_id,
                    "home_name" to home_name,
                    "heating_status" to binding.cbControl.isChecked
                )

                GlobalObj.db.collection("homes").add(home)

                UserInfo.addhome(home_id)

                val intent = Intent(this, BaseActivity::class.java)
                intent.putExtra("home_name", home_name)
                startActivity(intent)

            }
            else{
                if(home_name.length <= 3 )
                    binding.errorInput.text = getString(R.string.short_home_name)
                else
                    binding.errorInput.text = getString(R.string.long_home_name)
            }

        }
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if(UserInfo.homes.isEmpty()) {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                startActivity(intent)
        }
        else
            super.onBackPressed()
    }
}