package com.excuta.musictagger

import android.Manifest
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import com.excuta.musictagger.permission.Granted
import com.excuta.musictagger.permission.PermissionFragment
import com.excuta.musictagger.song.Song
import com.excuta.musictagger.song.SongAdapter
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import jp.wasabeef.recyclerview.animators.ScaleInRightAnimator
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val adapter = SongAdapter() {

    }
    private lateinit var permissionFragment: PermissionFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecycler()
        initPermissionFragment()
        scanClickListener()
        scroller.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    position.animate().run {
                        alpha(1f)
                        duration = 150
                        start()
                    }
                    scroller.animate().run {
                        alpha(0.5f)
                        duration = 150
                        start()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val percent = event.y / scroller.height
                    val index = (adapter.itemCount - 1) * percent
                    var indexInt = index.toInt()
                    if (indexInt < 0) indexInt = 0
                    if (indexInt > adapter.itemCount - 1) indexInt = adapter.itemCount - 1
                    recyclerView.scrollToPosition(indexInt)
                    position.text = indexInt.inc().toString()
                }
                MotionEvent.ACTION_UP -> {
                    position.animate().run {
                        alpha(0f)
                        duration = 150
                        start()
                    }
                    scroller.animate().run {
                        alpha(1f)
                        duration = 150
                        start()
                    }
                    scroller.performClick()
                }
            }
            true
        }
    }

    private fun initRecycler() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = ScaleInRightAnimator()
    }


    private fun scanClickListener() {
        scanBtn.setOnClickListener {
            permissionFragment.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            with(scanBtn.animate()) {
                alpha(0f)
                duration = 200
                start()
            }
        }
    }

    private fun initPermissionFragment() {
        permissionFragment = PermissionFragment.Provider(supportFragmentManager).get()
        permissionFragment.permissionLiveData.observe(this, Observer {
            if (it is Granted) LoaderManager.getInstance(this).initLoader(1, null, this)
        })
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM
        )

        return CursorLoader(
            this,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            null,
            null
        )
    }

    override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
        Flowable.create<Song>(
            {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        it.onNext(
                            Song(
                                cursor.getLong(0),// id
                                cursor.getString(2),// name
                                cursor.getString(4),// display name
                                cursor.getString(1),// artist
                                cursor.getString(5),// album
                                cursor.getString(3) // uri
                            )
                        )
                    }
                }
            },
            BackpressureStrategy.BUFFER
        ).buffer(10)
            .subscribe({
                adapter.add(it)
                updateCount(it)
            }, {
                error.text = it.toString()
            })
    }

    private fun updateCount(it: MutableList<Song>) {
        var count = songCount.text.toString().toIntOrNull()
        if (count == null) count = 0
        count += it.size
        songCount.text = count.toString()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        adapter.clear()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_id -> adapter.order = SongAdapter.Order.Id
            R.id.sort_title -> adapter.order = SongAdapter.Order.Title
            R.id.sort_artist -> adapter.order = SongAdapter.Order.Artist
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_sort, menu)
        return true
    }

}