package com.abdullojon.ndk

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.abdullojon.ndk.databinding.ActivityMainBinding
import com.abdullojon.ndk.databinding.ItemImageBinding
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val imageList = mutableListOf<ImageItem>()
    private lateinit var adapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImages()

        adapter = ImageAdapter(imageList, 
            onJavaClick = { position -> runKotlinGrayscale(position) },
            onCppClick = { position -> runCppGrayscale(position) }
        )
        binding.recyclerView.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_reset) {
            resetAllToZero()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetAllToZero() {
        try {
            imageList.forEach { item ->
                if (!item.javaProcessed.isRecycled && !item.original.isRecycled) {
                    val canvasJava = Canvas(item.javaProcessed)
                    canvasJava.drawBitmap(item.original, 0f, 0f, null)
                } else {
                    item.javaProcessed = item.original.copy(Bitmap.Config.ARGB_8888, true)
                }

                if (!item.cppProcessed.isRecycled && !item.original.isRecycled) {
                    val canvasCpp = Canvas(item.cppProcessed)
                    canvasCpp.drawBitmap(item.original, 0f, 0f, null)
                } else {
                    item.cppProcessed = item.original.copy(Bitmap.Config.ARGB_8888, true)
                }

                item.javaTime = "TIME: 0 ms"
                item.cppTime = "TIME: 0 ms"
            }
            adapter.notifyDataSetChanged()
            Toast.makeText(this, "Barchasi 0 holatiga qaytarildi", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Qayta tiklashda xatolik yuz berdi!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadImages() {
        imageList.clear()
        val resIds = listOf(R.drawable.image, R.drawable.image1, R.drawable.image2)
        resIds.forEach { resId ->
            decodeSampledBitmapFromResource(resId, 1080, 1080)?.let {
                imageList.add(ImageItem(
                    original = it,
                    javaProcessed = it.copy(Bitmap.Config.ARGB_8888, true),
                    cppProcessed = it.copy(Bitmap.Config.ARGB_8888, true)
                ))
            }
        }
    }

    private fun runKotlinGrayscale(position: Int) {
        val item = imageList.getOrNull(position) ?: return
        try {
            val bitmap = item.original.copy(Bitmap.Config.ARGB_8888, true)
            val time = measureTimeMillis {
                convertToGrayKotlin(bitmap)
            }
            item.javaProcessed = bitmap
            item.javaTime = "TIME: $time ms"
            adapter.notifyItemChanged(position)
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, "Kotlin: Xotira yetmadi!", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Kotlin: Xatolik!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun runCppGrayscale(position: Int) {
        val item = imageList.getOrNull(position) ?: return
        try {
            val bitmap = item.original.copy(Bitmap.Config.ARGB_8888, true)
            val time = measureTimeMillis {
                convertToGrayNative(bitmap)
            }
            item.cppProcessed = bitmap
            item.cppTime = "TIME: $time ms"
            adapter.notifyItemChanged(position)
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, "C++: Xotira yetmadi!", Toast.LENGTH_SHORT).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "C++: Xatolik!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun convertToGrayKotlin(bitmap: Bitmap) {
        val width = bitmap.width
        val height = bitmap.height
        val rowPixels = IntArray(width)
        for (y in 0 until height) {
            bitmap.getPixels(rowPixels, 0, width, 0, y, width, 1)
            for (x in 0 until width) {
                val pixel = rowPixels[x]
                val a = (pixel shr 24) and 0xff
                val r = (pixel shr 16) and 0xff
                val g = (pixel shr 8) and 0xff
                val b = pixel and 0xff
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                rowPixels[x] = (a shl 24) or (gray shl 16) or (gray shl 8) or gray
            }
            bitmap.setPixels(rowPixels, 0, width, 0, y, width, 1)
        }
    }

    private fun decodeSampledBitmapFromResource(resId: Int, reqWidth: Int, reqHeight: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(resources, resId, options)
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false
            BitmapFactory.decodeResource(resources, resId, options)
        } catch (e: Exception) { null }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    external fun convertToGrayNative(bitmap: Bitmap)

    companion object {
        init { System.loadLibrary("ndk") }
    }

    data class ImageItem(
        val original: Bitmap,
        var javaProcessed: Bitmap,
        var cppProcessed: Bitmap,
        var javaTime: String = "TIME: 0 ms",
        var cppTime: String = "TIME: 0 ms"
    )

    class ImageAdapter(
        private val items: List<ImageItem>,
        private val onJavaClick: (Int) -> Unit,
        private val onCppClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.binding.ivJava.setImageBitmap(item.javaProcessed)
            holder.binding.ivCpp.setImageBitmap(item.cppProcessed)

            holder.binding.tvJavaTime.text = item.javaTime
            holder.binding.tvCppTime.text = item.cppTime

            holder.binding.btnJava.setOnClickListener { onJavaClick(position) }
            holder.binding.btnCpp.setOnClickListener { onCppClick(position) }
        }

        override fun getItemCount() = items.size
    }
}