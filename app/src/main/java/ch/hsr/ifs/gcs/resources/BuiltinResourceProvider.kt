package ch.hsr.ifs.gcs.resources

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log

class BuiltinResourceProvider : ContentProvider() {

    companion object {
        private val LOG_TAG = BuiltinResourceProvider::class.simpleName
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun onCreate(): Boolean {

        context.assets.list("resources").forEach {
            Log.d(LOG_TAG, "found resource $it")
        }

        return true
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun getType(uri: Uri?): String? = null

    override fun insert(uri: Uri?, values: ContentValues?): Uri? = null

}