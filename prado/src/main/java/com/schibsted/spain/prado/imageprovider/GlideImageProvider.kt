package com.schibsted.spain.prado.imageprovider

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.github.chrisbanes.photoview.PhotoViewAttacher
import com.schibsted.spain.prado.R
import java.io.*


class GlideImageProvider : ImageProvider {

  companion object {
    private val IMAGE_QUALITY_1_PERCENT = 0.01f
  }

  override fun loadError(context: Context, imageView: ImageView) {
    loadImage(context, "", imageView, "")
  }

  override fun loadImage(context: Context, imageUrl: String, imageView: ImageView, url: String) {
    Log.d("imageUrl", imageUrl)
    if (verifyIfImageExists(imageUrl)) {

      val file = File(pathToSDCard(), imageUrl)

      Glide.with(context)
          .load(file)
          .error(R.drawable.nophoto)
          .placeholder(R.drawable.placeholder)
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .skipMemoryCache(true)
          .into(GlideDrawablePhotoViewAttacherTarget(imageView, PhotoViewAttacher(imageView)))
    }
    else {
      Log.d("URL", url)
      Log.d("Endereco", url+imageUrl)
      Glide.with(context)
          .load(url + imageUrl)
          .asBitmap()
          .error(R.drawable.nophoto)
          .placeholder(R.drawable.placeholder)
          .diskCacheStrategy(DiskCacheStrategy.NONE)
          .skipMemoryCache(true)
          .into(object: BitmapImageViewTarget(imageView) {

          override fun onResourceReady(resource: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
            saveImage(resource, imageUrl)
            imageView.scaleType = ImageView.ScaleType.MATRIX
            imageView.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
            val photoViewAttacher = PhotoViewAttacher(imageView)
            photoViewAttacher.update()
            super.onResourceReady(resource, glideAnimation)

          }
      })
    }

  }

  fun verifyIfImageExists(file: String) : Boolean {
    val file = File(pathToSDCard(), file)
    Log.d("File", file.toString())
    Log.d("Exite", file.exists())
    return file.exists()
  }

  fun pathToSDCard() : String {
    return Environment.getExternalStorageDirectory().toString() + "/ReyTemper/"
  }

  fun saveImage(resource: Bitmap?, file: String) {

    val file = File(pathToSDCard(), file)
    val dir = file.parentFile
    try {
      if (!dir.mkdirs() && (!dir.exists() || !dir.isDirectory())) {
        throw IOException("Cannot ensure parent directory for file " + file)
      }

      val s = BufferedOutputStream(FileOutputStream(file))

      val stream = ByteArrayOutputStream()
      resource!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
      val byteArray = stream.toByteArray()

      s.write(byteArray)
      s.flush()
      s.close()
    } catch (e: IOException) {
      e.printStackTrace()
    }
  }

}