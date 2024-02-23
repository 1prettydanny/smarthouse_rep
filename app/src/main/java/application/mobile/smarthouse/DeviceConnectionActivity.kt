package application.mobile.smarthouse

import android.app.Activity
import android.app.ActivityOptions
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.databinding.ActivityDeviceConnectionBinding
import java.util.UUID

data class ConnectCategory(val name: String, val icon: Int, val devices: MutableList<ConnectDevice> = mutableListOf())
data class ConnectDevice(var name: String, var icon: Int, val category: String)
class DeviceConnectionActivity : AppCompatActivity(), DeviceConnection {

    private lateinit var binding: ActivityDeviceConnectionBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var roomDevicesRV: RecyclerView
    private lateinit var roomId: String
    private lateinit var roomName: String
    private lateinit var roomType: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceConnectionBinding.inflate(layoutInflater)

        setContentView(binding.root)


        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        Device.countersClear()
        roomType = intent.getStringExtra("room_type").toString()
        val roomPicture = intent.getStringExtra("room_image").toString()
        roomId = intent.getStringExtra("room_id").toString()
        roomName = intent.getStringExtra("room_name").toString()

      binding.headerImage.setImageDrawable(ContextCompat.getDrawable(this, GlobalFun.getImage(roomPicture)))

        with(binding) {

            roomTitle.text = roomName
            intent.getStringExtra("room_name")
            val roomTypeIcon = GlobalFun.getTypeIcon(roomType)
            roomImage.setImageDrawable(ContextCompat.getDrawable(this@DeviceConnectionActivity, roomTypeIcon))


            val categories = mutableListOf(
                getCategory("Lighting"),
                getCategory("Climate"),
                getCategory("Energy"),
                getCategory("Security"),
                getCategory("Entertainment"),
                getCategory("Other")
            )

            if(roomType != "Classic"){
                categories.add(0, getCategory(roomType))
            }


            val categoriesAdapter = CategoryConnectionAdapter(categories, this@DeviceConnectionActivity)
            categoriesRecyclerView.adapter = categoriesAdapter

            roomContainer.setOnDragListener(dragListener)

            roomDevicesRV = roomDevicesRecyclerView
            val roomDevices = mutableListOf<Device>()

            GlobalObj.db.collection("devices")
                .whereEqualTo("room_id", roomId)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val id = document["device_id"].toString()
                        val name = document["device_name"].toString()
                        val category = document["device_category"].toString()
                        val order = document["device_order"].toString().toInt()
                        roomDevices.add(Device(id, name, order, category))
                    }
                    roomAdapter = RoomAdapter(roomDevices, this@DeviceConnectionActivity)
                    roomDevicesRV.layoutManager = GridLayoutManager(this@DeviceConnectionActivity, 4)
                    roomDevicesRV.adapter = roomAdapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@DeviceConnectionActivity, "err: " + exception.message, Toast.LENGTH_SHORT)
                        .show()
                }


            backBtn.setOnClickListener {
                onBackPressedCallback.handleOnBackPressed()
            }

            cancelBtn.setOnClickListener {
                val text = ContextCompat.getString(this@DeviceConnectionActivity,
                    R.string.dialog_delete_room_text) + " \"${roomName}\" ?"
                
                GlobalFun.dialogDelete(this@DeviceConnectionActivity, text) {delete->
                    if(delete){
                    GlobalFun.deleteRoom(roomId) {
                        val intent = Intent(this@DeviceConnectionActivity, BaseActivity::class.java)
                        val animator = ActivityOptions.makeCustomAnimation(
                            this@DeviceConnectionActivity,
                            R.anim.slide_in_left, R.anim.stay
                        ).toBundle()
                        startActivity(intent, animator)
                        finish()
                        }
                    }
                }
            }

            plusButton.setOnClickListener {

                roomAdapter.roomDevices.forEach {
                        val device = hashMapOf(
                            "device_id" to it.id,
                            "room_id" to roomId,
                            "device_name" to it.name,
                            "device_order" to it.order,
                            "device_category" to it.category
                        )
                        GlobalObj.db.collection("devices").add(device)
                }

                val intent = Intent(this@DeviceConnectionActivity, BaseActivity::class.java)
                startActivity(intent)
            }

        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun getCategory(name: String): ConnectCategory{
        return ConnectCategory(name, GlobalFun.getIconCategory(name), createDevList(name))
    }

    private fun getDevice(name: String, category: String): ConnectDevice{
        return ConnectDevice(name, GlobalFun.getIconDevice(name), category)
    }

    private fun createDevList(category: String): MutableList<ConnectDevice> {
        val devicesList = mutableListOf<ConnectDevice>()
        when(category){
            "Lighting" -> {
                devicesList.addAll(mutableListOf(
                    getDevice("Desktop lamp", category),
                    getDevice("Chandelier", category),
                    getDevice("Bulb", category),
                    getDevice("Spotlight", category)
                    )
                )
            }
            "Climate" ->{
                devicesList.addAll(mutableListOf(
                    getDevice("Radiator", category),
                    getDevice("Cooling system", category),
                    getDevice("Heating floor",  category),
                    getDevice("Ceiling fan", category),
                    getDevice("Desktop fan", category),
                    getDevice("Thermo regulator", category),
                    getDevice("Humidifier", category)
                    )
                )
            }
            "Energy" -> {
                devicesList.addAll(mutableListOf(
                    getDevice("Switch", category),
                    getDevice("Plug",  category),
                    getDevice("Socket", category),
                    )
                )
            }
            "Entertainment" -> {
                devicesList.addAll(mutableListOf(
                    getDevice("TV",category),
                    getDevice("Radio", category),
                    getDevice("Speaker", category),
                    getDevice("Projector",category),
                    )
                )
            }
            "Security" -> {
                devicesList.addAll(mutableListOf(
                    getDevice("Camera", category),
                    getDevice("Lock", category),
                    )
                )
            }
            "Kitchen" ->{
                devicesList.addAll(mutableListOf(
                    getDevice("Fridge", category),
                    getDevice("Coffee machine", category),
                    getDevice("Stove", category),
                    )
                )
            }
            "Bathroom" ->{
                devicesList.addAll(mutableListOf(
                    getDevice("Washing machine", category),
                    getDevice("Tap", category),
                    getDevice("Shower", category)
                    )
                )
            }
            "Other" -> {
                devicesList.addAll(mutableListOf(
                    getDevice("Wi-Fi", category),
                    getDevice("Moped",  category),
                    getDevice("Scooter", category),
                    getDevice("Printer", category)
                    )
                )
            }
        }
        return devicesList
    }

    private val dragListener = View.OnDragListener { view, event ->
        when(event.action){
            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
                view.invalidate()
                true
            }
            DragEvent.ACTION_DROP ->{
                val name = event.clipData.getItemAt(0).toString()
                val category = event.clipData.getItemAt(1).toString()
                addDevice(name, category)
                true
            }
            DragEvent.ACTION_DRAG_ENDED ->{

                true
            }
            else -> false
        }
    }


    override fun addIndent() {
        if (roomAdapter.itemCount == 1){
            val button = findViewById<ImageButton>(R.id.plus_button)
            button.visibility = View.GONE
        }
        val textView = findViewById<TextView>(R.id.drag_device_text)
        textView.visibility = View.VISIBLE
    }
    private fun connect() {
        val intent = Intent(this, ConnectionActivity::class.java)
        startActivity(intent)
    }

    override fun addDevice(name: String, category: String) {
       connect()
        if(roomAdapter.itemCount == 0){
            val textView = findViewById<TextView>(R.id.drag_device_text)
            textView.visibility = View.GONE
            val button = findViewById<ImageButton>(R.id.plus_button)
            button.visibility = View.VISIBLE
        }
        val deviceId = UUID.randomUUID().toString()
        roomAdapter.addNewDeviceToRoom(Device(deviceId,name, Device.getNextId(name), category), 0)
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val intent = Intent(this@DeviceConnectionActivity, CreateRoomActivity::class.java)

            val animationBundle = ActivityOptions.makeCustomAnimation(
                this@DeviceConnectionActivity,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            ).toBundle()
            intent.putExtra("room_id", roomId)
            intent.putExtra("room_name", roomName)
            intent.putExtra("room_type", roomType)
            startActivity(intent, animationBundle)
        }
    }

}


class CategoryConnectionAdapter(private val categories: MutableList<ConnectCategory>, private val listener: DeviceConnection) : RecyclerView.Adapter<CategoryConnectionAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_category_connection, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(connectCategory: ConnectCategory) {
            val categoryName: TextView = itemView.findViewById(R.id.category_title)
            val devicesRecyclerView: RecyclerView = itemView.findViewById(R.id.devices_recycler_view)
            val collapse: ImageButton = itemView.findViewById(R.id.collapse_button)
            val image: ImageView = itemView.findViewById((R.id.category_image))



            categoryName.text = connectCategory.name
            image.setImageDrawable(ContextCompat.getDrawable(itemView.context, connectCategory.icon))
            val deviceConnectionAdapter = DeviceConnectionAdapter(connectCategory.devices,listener)
            devicesRecyclerView.layoutManager = GridLayoutManager(itemView.context, 4)
            devicesRecyclerView.adapter = deviceConnectionAdapter


            collapse.setOnClickListener{
                if(devicesRecyclerView.visibility == View.GONE) {
                    devicesRecyclerView.visibility = View.VISIBLE
                    collapse.setImageDrawable(ContextCompat.getDrawable(itemView.context ,R.drawable.arrow_up))
                }
                else {
                    devicesRecyclerView.visibility = View.GONE
                    collapse.setImageDrawable(ContextCompat.getDrawable(itemView.context ,R.drawable.arrow_down))
                }
            }
            itemView.setOnClickListener{
                collapse.performClick()
            }
        }
    }
}

class RoomAdapter(val roomDevices: MutableList<Device>, val listener: DeviceConnection) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_room_device, parent, false)
        return RoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        holder.bind(roomDevices[position])
    }

    override fun getItemCount(): Int {
        return roomDevices.size
    }
    fun addNewDeviceToRoom(device: Device, position: Int) {
            roomDevices.add(position,device)
            notifyItemInserted(position)
    }
    inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val deviceImageView: ImageView = itemView.findViewById(R.id.deviceImageView)
        fun bind(device: Device) {
            val fullName = device.name + " ${device.order}"
            deviceName.text = fullName

            deviceImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, GlobalFun.getIconDevice(device.name)))


            itemView.setOnClickListener{
                val text = ContextCompat.getString(itemView.context, R.string.dialog_delete_device_text) + " \"${fullName}\" ?"
                GlobalFun.dialogDelete(itemView.context as Activity, text) {delete->
                    if(delete) {
                        Device.getLastId(device.name)
                        if (roomDevices.size == 1) {
                            listener.addIndent()
                        } else {
                            roomDevices.subList(0, bindingAdapterPosition)
                                .forEachIndexed { index, dev ->
                                    if (dev.category == device.category) {
                                        dev.order = dev.order -1
                                        notifyItemChanged(index)
                                    }
                                }
                        }

                        roomDevices.removeAt(bindingAdapterPosition)
                        notifyItemRemoved(bindingAdapterPosition)
                    }
                }

            }
        }

    }
}
class DeviceConnectionAdapter(private val devices: MutableList<ConnectDevice>, private val listener: DeviceConnection) : RecyclerView.Adapter<DeviceConnectionAdapter.DeviceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_device_connection, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(devices[position])
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceName: TextView = itemView.findViewById(R.id.deviceName)
        private val deviceImageView: ImageView = itemView.findViewById(R.id.deviceImageView)
        fun bind(device: ConnectDevice) {
            deviceName.text = device.name

         deviceImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context, device.icon))

            itemView.setOnClickListener {
                listener.addDevice(device.name, device.category)
            }

            itemView.setOnLongClickListener {

                val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
                val item1 = ClipData.Item(device.name)
                val item2 = ClipData.Item(device.category)

                val data = ClipData(device.name, mimeTypes, item1)
                data.addItem(item2)

                val dragShadowBuilder = View.DragShadowBuilder(it)
                it.startDragAndDrop(data,dragShadowBuilder,it,0)
                true
            }

        }

    }


}

interface DeviceConnection {
    fun addDevice(name: String,category: String)
    fun addIndent()
}
