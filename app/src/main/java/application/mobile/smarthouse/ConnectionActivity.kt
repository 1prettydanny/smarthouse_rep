package application.mobile.smarthouse


import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import application.mobile.smarthouse.databinding.ActivityConnectionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConnectionActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var connectionStatusTextView: TextView
    private lateinit var binding: ActivityConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = binding.progressBar
        connectionStatusTextView = binding.connectionStatus

        simulateConnection()

        binding.cancelConnect.setOnClickListener{
            finish()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun simulateConnection() {


        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                for (progress in 0..100 step 10) {
                    delay(100)
                    updateProgress(progress)
                }

                connectionStatusTextView.text = "SUCCESS"
            } finally {
                progressBar.visibility = View.GONE
                finish()
            }
        }
    }

    private suspend fun updateProgress(progress: Int) {
        withContext(Dispatchers.Main) {
            progressBar.progress = progress
        }
    }


}