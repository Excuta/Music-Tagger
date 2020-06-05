package com.excuta.musictagger.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import com.excuta.musictagger.R
import kotlinx.android.synthetic.main.item_song.view.*
import java.util.*

class SongAdapter(val chooseListener: (Song) -> Unit) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private val items = SortedList<Song>(Song::class.java, object :
        SortedList.BatchedCallback<Song>(object : SortedList.Callback<Song>() {
            override fun areItemsTheSame(item1: Song?, item2: Song?): Boolean {
                return item1?.id == item2?.id
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
                val compareTo = o1?.name?.compareTo(o2?.name ?: "")
                return compareTo ?: 0
            }

            override fun areContentsTheSame(oldItem: Song?, newItem: Song?): Boolean {
                return Objects.equals(oldItem, newItem)
            }

        }){})

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        return SongViewHolder(parent.inflate(R.layout.item_song))
    }

    override fun getItemCount(): Int {
        return items.size()
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(position)
    }

    fun add(list: List<Song>) {
        items.addAll(list)
    }

    fun replace(list: List<Song>) {
        items.replaceAll(list)
    }

    fun clear() {
        items.clear()
    }

    inner class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {


        fun bind(position: Int) {
            val item = items[position]
            with(itemView) {
                identifier.text = item.id
                title.text = item.title
                name.text = item.name
                artist.text = item.artist
                album.text = item.album
                data.text = item.data
            }
        }

    }
}

fun ViewGroup.inflate(@LayoutRes layoutId: Int): View {
    return LayoutInflater.from(context).inflate(layoutId, this, false)
}