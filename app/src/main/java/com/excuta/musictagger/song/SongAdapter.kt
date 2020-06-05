package com.excuta.musictagger.song

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.excuta.musictagger.R
import kotlinx.android.synthetic.main.item_song.view.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class SongAdapter(val listener: (Song) -> Unit) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val executor = Executors.newSingleThreadExecutor()
    private val uiHandler = Handler(Looper.getMainLooper())
    var order: Order = Order.Title
        set(value) {
            field = value
            replace(fullList)
        }

    private var fullList = mutableListOf<Song>() // beacuse sorted"list" isn't a "list"
    private val items = SortedList(Song::class.java, object : SortedList.Callback<Song>() {
        override fun areItemsTheSame(item1: Song?, item2: Song?): Boolean {
            return item1!!.id == item2!!.id
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

        override fun compare(o1: Song?, o2: Song?): Int {
            return when (order) {
                Order.Id -> o1!!.id.compareTo(o2!!.id)
                Order.Title -> o1!!.title.compareTo(o2!!.title)
                Order.Artist -> o1!!.artist.compareTo(o2!!.artist)
            }
        }

        override fun areContentsTheSame(oldItem: Song?, newItem: Song?): Boolean {
            return Objects.equals(oldItem, newItem)
        }
    })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(parent.inflate(R.layout.item_song))
    }

    override fun getItemCount(): Int {
        return items.size()
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(position)
    }

    fun add(collection: Collection<Song>) {
        fullList.addAll(collection)
        items.addAll(collection)
    }

    fun replace(collection: Collection<Song>) {
        fullList = ArrayList(collection)
        items.replaceAll(collection)
    }

    fun clear() {
        fullList.clear()
        items.clear()
    }

    fun update(updatedSongs: HashMap<Int, Song>) {
        executor.execute {
            updatedSongs.keys.forEach {
                if (it in 0 until itemCount) {
                    fullList.removeAt(it)
                    fullList.add(it, updatedSongs[it]!!)
                }
            }
            uiHandler.post { replace(fullList) }
        }
    }

    fun indexOf(it: Song): Int {
        return fullList.indexOf(it)
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(position: Int) {
            val item = items[position]
            with(itemView) {
                identifier.text = item.id.toString()
                title.text = item.title
                name.text = item.fileName
                artist.text = item.artist
                album.text = item.album
                data.text = item.data
            }
            itemView.setOnClickListener {
                listener(item)
            }
        }
    }

    enum class Order {
        Id, Title, Artist
    }
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}