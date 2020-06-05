package com.excuta.musictagger

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.excuta.musictagger.permission.Granted
import com.excuta.musictagger.permission.PermissionFragment


class MainActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    private val songs: MutableList<String> = ArrayList()
    private val permissionFragment = PermissionFragment.Provider(supportFragmentManager).get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionFragment.permissionLiveData.observe(this, Observer {
            if (it is Granted) LoaderManager.getInstance(this).initLoader(1, null, this)
        })
        permissionFragment.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        if (cursor != null) {
            while (cursor.moveToNext()) {
                songs.add(
                    cursor.getString(0)
                        .toString() + "||" + cursor.getString(1) + "||" + cursor.getString(2) + "||" + cursor.getString(
                        3
                    ) + "||" + cursor.getString(4) + "||" + cursor.getString(5)
                )
            }
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        songs.clear()
    }
}