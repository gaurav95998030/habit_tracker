package io.github.gaurav95998030.habittracker.commons.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File

/**
 * Writes [bitmap] to cache/shared/ and opens the system share sheet for it.
 * Served through the app's FileProvider (`${applicationId}.fileprovider`).
 */
fun shareBitmap(context: Context, bitmap: Bitmap, fileName: String, chooserTitle: String) {
    val dir = File(context.cacheDir, "shared").apply { mkdirs() }
    val file = File(dir, "$fileName.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
