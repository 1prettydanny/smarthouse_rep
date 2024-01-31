package application.mobile.smarthouse

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import application.mobile.smarthouse.databinding.ActivityRoomBinding
import com.bumptech.glide.Glide

class RoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val room_id = intent.getStringExtra("room_id")
        val room_picture = intent.getIntExtra("room_picture", -1)
        val room_name = intent.getStringExtra("room_name")
        val room_type = intent.getIntExtra("room_type",-1)


        binding.roomName.text = room_name
        Glide.with(this)
            .load(room_type)
            .into(binding.roomTypeIcon)

        Glide.with(this)
            .load(room_picture)
            .into(binding.roomHeaderImage)


        binding.backBtn.setOnClickListener{
            onBackPressed()
        }
    }



}