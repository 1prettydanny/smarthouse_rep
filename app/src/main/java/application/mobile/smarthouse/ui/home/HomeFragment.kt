package application.mobile.smarthouse.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import application.mobile.smarthouse.CreateRoomActivity
import application.mobile.smarthouse.GlobalFun
import application.mobile.smarthouse.R
import application.mobile.smarthouse.RoomActivity
import application.mobile.smarthouse.UserInfo
import application.mobile.smarthouse.databinding.FragmentHomeBinding
import java.util.Collections

data class Room(val id: String, var name: String, var image: String, var type: String, var isAnimated: Boolean)
class HomeFragment : Fragment(), MenuProvider, OnStartDragListener {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var touchHelper: ItemTouchHelper
    private lateinit var roomsAdapter: RoomsAdapter



    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


            val title = activity?.findViewById<TextView>(R.id.toolbar_title)

            title?.text = UserInfo.selected_home_name



        activity?.addMenuProvider(this@HomeFragment,viewLifecycleOwner, Lifecycle.State.RESUMED)

        val orderedRoomIds = viewModel.loadRoomsOrderFromSharedPreferences()

        viewModel.getRooms(UserInfo.selected_home_id).observe(viewLifecycleOwner) { rooms ->

            val orderedRooms = mutableListOf<Room>()

            orderedRooms.addAll(orderedRoomIds.mapNotNull { roomId ->
                rooms.find { it.id == roomId }
            })

            val newRooms = rooms.filter { room ->
                orderedRooms.none { it.id == room.id }
            }

            binding.emptyRoomListText.setOnClickListener {
                addRoom()
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
//        try {
//            val displayMetrics = requireContext().resources.displayMetrics
//            val screenHeight = displayMetrics.heightPixels
//            roomsAdapter.rooms.forEach { room ->
//                if (room.isAnimated) {
//                    val viewHolder = binding.roomsRecycleView.findViewHolderForAdapterPosition(
//                        roomsAdapter.rooms.indexOf(room)
//                    )
//                    viewHolder?.let {
//                        room.isAnimated = false
//                        it.itemView.y = -screenHeight.toFloat()
//                        val animator = ValueAnimator.ofFloat(it.itemView.y, 0f)
//                        animator.duration = 600
//                        room.isAnimated = true
//
//
//                        animator.addUpdateListener { animation ->
//                            it.itemView.y = (animation.animatedValue as Float)
//                        }
//                        animator.start()
//
//                    }
//
//                }
//            }
//        }catch(_: Exception) {}
    }
    @SuppressLint("ObjectAnimatorBinding")
    override fun goToRoom(roomItem: View, room: Room) {
        val intent = Intent(context, RoomActivity::class.java)
        intent.putExtra("room_image", room.image)
        intent.putExtra("room_name", room.name)
        intent.putExtra("room_type", room.type)
        intent.putExtra("room_id", room.id)
//            val screenHeight = requireContext().resources.displayMetrics.heightPixels
//        val animator = ValueAnimator.ofFloat(roomItem.y, -screenHeight.toFloat())
//        animator.duration = 600
//        room.isAnimated = true
//
//
//        animator.addUpdateListener { animation ->
//            roomItem.y = (animation.animatedValue as Float)
//        }
//
//        animator.addListener(object : AnimatorListenerAdapter() {
//
//            override fun onAnimationEnd(animation: Animator) {
//
//            }
//        })
//
//        animator.start()
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            val options = ActivityOptions.makeCustomAnimation(requireActivity(), 0, 0)
//            startActivity(intent, options.toBundle())
//        }, 450)

        val animator = ActivityOptions.makeCustomAnimation(context, R.anim.slide_in_up,R.anim.stay)
        startActivity(intent, animator.toBundle())
    }



    override fun showZeroText() {
        binding.emptyRoomListText.visibility = View.GONE
    }



    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.home_page_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.addRoom -> {
                addRoom()
                true
            }

            else -> false
        }

    }

    private fun addRoom(){
        val intent = Intent(requireContext(), CreateRoomActivity::class.java)
        intent.putExtra("home_id", UserInfo.selected_home_id)
        val animationBundle = ActivityOptions.makeCustomAnimation(
            context,
            R.anim.slide_in_right,
            R.anim.slide_out_left
        ).toBundle()
        startActivity(intent, animationBundle)
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_item_room, parent, false)
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


            roomImageView.setImageDrawable(ContextCompat.getDrawable(itemView.context,GlobalFun.getImage(room.image)))

            roomTypeIcon.setImageDrawable(ContextCompat.getDrawable(itemView.context,GlobalFun.getTypeIcon(room.type)))
            roomName.text = room.name

            itemView.setOnClickListener {
                listener.goToRoom(it, room)
            }

            itemView.setOnLongClickListener{
                listener.onStartDrag(this)
                true
            }

            otherButton.setOnClickListener {view ->

                val popup = PopupMenu(view.context, view, Gravity.END)
                MenuInflater(view.context).inflate(R.menu.room_menu, popup.menu)
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {

                        R.id.changePicture -> {

                            var  image = room.image
                            do {
                               image = GlobalFun.randomImage(room.type)
                            }
                            while(image==room.image)


                            GlobalFun.updateRoomImage(room.id, image) {
                                room.image = image
                                notifyItemChanged(bindingAdapterPosition)
                            }
                            true
                        }
                        R.id.renameRoom -> {
                            val text = ContextCompat.getString(itemView.context,R.string.dialog_rename_room_text)
                            GlobalFun.renameItem(itemView.context as Activity, room.name, text){ newName ->
                                if(newName != room.name){
                                    GlobalFun.updateRoomName(room.id, newName){
                                        room.name = newName
                                        notifyItemChanged(bindingAdapterPosition)
                                    }
                                }
                            }
                            true
                        }

                        R.id.deleteRoom -> {
                            val text = ContextCompat.getString(itemView.context, R.string.dialog_delete_room_text) + " \"${room.name}\" ?"
                            GlobalFun.dialogDelete(itemView.context as Activity, text) {delete->
                                if(delete) {
                                    GlobalFun.deleteDevices(room.id)
                                    GlobalFun.deleteRoom(room.id) {
                                        val position = bindingAdapterPosition
                                        rooms.removeAt(position)
                                        notifyItemRemoved(position)
                                        if (rooms.size == 0) {
                                            listener.showZeroText()
                                        }
                                    }
                                }
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
    fun showZeroText()

}
