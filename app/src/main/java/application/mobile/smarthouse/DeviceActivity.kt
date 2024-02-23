package application.mobile.smarthouse

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import application.mobile.smarthouse.databinding.ActivityDeviceBinding
import application.mobile.smarthouse.databinding.ActivityRoomBinding

class DeviceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeviceBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.updatesBtn.setOnClickListener {
            onBackPressed()
        }
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@DeviceActivity, RoomActivity::class.java)
            val anim = ActivityOptions.makeCustomAnimation(this@DeviceActivity, R.anim.slide_out_right, R.anim.slide_in_right)
            startActivity(intent, anim.toBundle())
        }
    }

}