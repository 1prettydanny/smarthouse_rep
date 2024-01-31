package application.mobile.smarthouse.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.cardview.widget.CardView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.BaseActivity
import application.mobile.smarthouse.CreateRoomActivity
import application.mobile.smarthouse.GlobalObj
import application.mobile.smarthouse.R
import application.mobile.smarthouse.RoomActivity
import application.mobile.smarthouse.RoomSettingsActivity
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.ActivityRoomBinding
import application.mobile.smarthouse.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import java.util.Collections
import kotlin.random.Random

data class Room(val id: String, var name: String, var image: Int, var typeIcon: Int, var isAnimated: Boolean)
class HomeFragment : Fragment(), MenuProvider, OnStartDragListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var roomsAdapter: RoomsAdapter


    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        BaseActivity.setHomeTitle()

        activity?.addMenuProvider(this@HomeFragment,viewLifecycleOwner, Lifecycle.State.RESUMED)

        val orderedRoomIds = viewModel.loadRoomsOrderFromSharedPreferences()

        viewModel.getRooms(UserInfo.selected_home).observe(viewLifecycleOwner) { rooms ->

            val orderedRooms = mutableListOf<Room>()

            orderedRooms.addAll(orderedRoomIds.mapNotNull { roomId ->
                rooms.find { it.id == roomId }
            })

            val newRooms = rooms.filter { room ->
                orderedRooms.none { it.id == room.id }
            }

            orderedRooms.addAll(0, newRooms)

            roomsAdapter = RoomsAdapter(orderedRooms, this@HomeFragment)

            binding.roomsRecycleView.layoutManager = LinearLayoutManager(context)
            binding.roomsRecycleView.adapter = roomsAdapter
            touchHelper = ItemTouchHelper(ItemMoveCallback(roomsAdapter))
            touchHelper.attachToRecyclerView(binding.roomsRecycleView)

            if (rooms.isNotEmpty())
                binding.emptyRoomListText.visibility = View.GONE
            else
                binding.emptyRoomListText.visibility = View.VISIBLE

        }

        return root
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        touchHelper.startDrag(viewHolder)
    }
    override fun onResume() {
        super.onResume()
        try {
            val displayMetrics = requireContext().resources.displayMetrics
            val screenHeight = displayMetrics.heightPixels
            roomsAdapter.rooms.forEach { room ->
                if (room.isAnimated) {
                    val viewHolder = binding.roomsRecycleView.findViewHolderForAdapterPosition(
                        roomsAdapter.rooms.indexOf(room)
                    )
                    viewHolder?.let {
                        room.isAnimated = false
                        val animator = ObjectAnimator.ofFloat(it.itemView, "translationY", -screenHeight.toFloat(),  0f)
                        animator.duration = 500
                        animator.start()

                    }

                }
            }
        }catch(_: Exception) {}
    }
    @SuppressLint("ObjectAnimatorBinding")
    override fun goToRoom(roomItem: View, room: Room) {
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra("room_picture", room.image)
        intent.putExtra("room_name", room.name)
        intent.putExtra("room_type", room.typeIcon)
        intent.putExtra("room_id", room.id)
        val screenHeight = requireContext().resources.displayMetrics.heightPixels
        val animator = ValueAnimator.ofFloat(roomItem.y, -screenHeight.toFloat())
        animator.duration = 600
        room.isAnimated = true


        animator.addUpdateListener { animation ->
            roomItem.y = (animation.animatedValue as Float)
        }

        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator) {

            }
        })

        animator.start()

        Handler(Looper.getMainLooper()).postDelayed({
            val options = ActivityOptions.makeCustomAnimation(requireActivity(), 0, 0)
            startActivity(intent, options.toBundle())
        }, 450)

        //val intent = Intent(context, RoomActivity::class.java)

      //  startActivity(intent)
    }


    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.home_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.addRoom -> {
                val intent = Intent(requireContext(), CreateRoomActivity::class.java)
                intent.putExtra("home_id", UserInfo.selected_home)
                startActivity(intent)
                true
            }

            R.id.editRoom -> {
                true
            }

            else -> false
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveOrderToSharedPreferences(roomsAdapter.rooms.map { it.id })
    }

}

class RoomsAdapter(val rooms: MutableList<Room>, private val listener: OnStartDragListener) : RecyclerView.Adapter<RoomsAdapter.RoomsViewHolder>(), ItemMoveCallback.ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room, parent, false)
        return RoomsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoomsViewHolder, position: Int) {
        holder.bind(rooms[position])
    }

    override fun getItemCount(): Int {
        return rooms.size
    }


    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(rooms, fromPosition, toPosition)
        notifyItemMoved(toPosition,fromPosition)
    }



    inner class RoomsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val roomName: TextView = itemView.findViewById(R.id.room_name)
        private val roomImageView: ImageView = itemView.findViewById(R.id.room_image_view)
        private val roomTypeIcon: ImageView = itemView.findViewById(R.id.room_icon)
        private val otherButton: ImageView = itemView.findViewById(R.id.room_other_button)


        @SuppressLint("DiscouragedPrivateApi")
        fun bind(room: Room) {


            Glide.with(itemView.context)
                .load(room.image)
                .into(roomImageView)

            Glide.with(itemView.context)
                .load(room.typeIcon)
                .into(roomTypeIcon)
            roomName.text = room.name

            itemView.setOnClickListener {
                itemView.bringToFront()
                listener.goToRoom(it, room)
            }

            itemView.setOnLongClickListener{
                listener.onStartDrag(this)
                true
            }

            otherButton.setOnClickListener {view ->

                val popup = PopupMenu(view.context, view)
                MenuInflater(view.context).inflate(R.menu.room_menu, popup.menu)

                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {

                        R.id.changePicture -> {

                            var  image: Int
                            do {
                               image = randomImage(room.typeIcon)
                            }
                            while(image==room.image)

                            room.image = image


                            val reference = GlobalObj.db.collection("rooms")
                            var docId: String? = null
                            reference.whereEqualTo("room_id", room.id)
                                .get()
                                .addOnSuccessListener {
                                    for(document in it) {
                                        docId = document.id
                                    }
                                    val updates = hashMapOf<String, Any>(
                                        "room_image" to room.image
                                    )
                                    if(docId!=null) {
                                        reference.document(docId!!)
                                            .update(updates)
                                            .addOnCompleteListener {
                                                notifyItemChanged(bindingAdapterPosition)
                                            }
                                    }

                                }
                            true
                        }
                        R.id.editRoom -> {

                            true
                        }
                        R.id.deleteRoom -> {
                            var reference = GlobalObj.db.collection("devices")
                            var docId: String? = null
                            reference.whereEqualTo("room_id", room.id)
                                .get()
                                .addOnSuccessListener {
                                    for (document in it) {
                                        docId = document.id
                                    }
                                    if (docId != null) {
                                        reference.document(docId!!).delete()
                                    }
                                    reference = GlobalObj.db.collection("rooms")

                                    reference.whereEqualTo("room_id", room.id)
                                        .get()
                                        .addOnSuccessListener {
                                            for (document in it) {
                                                docId = document.id
                                            }

                                            if (docId != null) {
                                                reference.document(docId!!).delete()
                                            }
                                        }
                                    val position = bindingAdapterPosition
                                    rooms.removeAt(position)
                                    notifyItemRemoved(position)
                                }


                            true
                        }

                        else -> false
                    }
                }

                try {
                    val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                    fieldMPopup.isAccessible = true
                    val mPopup = fieldMPopup.get(popup)
                    mPopup.javaClass
                        .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                        .invoke(mPopup, true)
                } catch (_: Exception) {

                } finally {
                    popup.show()
                }

            }
        }
        private fun randomImage(icon: Int): Int{

            val imagesArray = when (icon) {
                R.drawable.room_type_classic_ico -> arrayOf(
                    R.drawable.img_classic1,
                    R.drawable.img_classic2,
                    R.drawable.img_classic3,
                    R.drawable.img_classic4
                )

                R.drawable.room_type_kitchen_ico -> arrayOf(
                    R.drawable.img_kitchen1,
                    R.drawable.img_kitchen2,
                    R.drawable.img_kitchen3,
                    R.drawable.img_kitchen4
                )

                R.drawable.room_type_bathroom_ico -> arrayOf(
                    R.drawable.img_bathroom1,
                    R.drawable.img_bathroom2,
                    R.drawable.img_bathroom3,
                    R.drawable.img_bathroom4
                )

                else -> emptyArray()
            }

            return if (imagesArray.isNotEmpty()) {
                imagesArray[Random.nextInt(imagesArray.size)]
            } else {
                -1
            }
        }

    }

}

class ItemMoveCallback(private val adapter: RoomsAdapter) : ItemTouchHelper.Callback() {

    private val ALPHA_FULL = 1.0f

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        adapter.onItemMove(viewHolder.absoluteAdapterPosition, target.absoluteAdapterPosition)

        return true
    }


    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }


    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
             viewHolder.itemView.alpha = ALPHA_FULL
    }

    interface ItemTouchHelperAdapter {
        fun onItemMove(fromPosition: Int, toPosition: Int)

    }
}

interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
    fun goToRoom(roomItem: View, room: Room)
}
