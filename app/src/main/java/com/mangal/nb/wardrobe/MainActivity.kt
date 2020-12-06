package com.mangal.nb.wardrobe

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.mangal.nb.wardrobe.adapters.ClothVPAdapter
import com.mangal.nb.wardrobe.db.BaseClothInterface
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var clothVModel: ClothVModel
    private lateinit var clothTopVPAdapter: ClothVPAdapter
    private lateinit var clothBottomVPAdapter: ClothVPAdapter
    private var currentTopId: Int = -1
    private var currentBottomId: Int = -1
    private var photoUri: Uri? = null


    companion object {
        const val PERMISSION_REQUEST = 120
        const val PICK_TOP_IMAGE = 122
        const val PICK_BOTTOM_IMAGE = 123
        const val CAPTURE_BOTTOM_IMAGE = 126
        const val CAPTURE_TOP_IMAGE = 127
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clothVModel = ViewModelProvider(this).get(ClothVModel::class.java)
        clothTopVPAdapter = ClothVPAdapter(this)
        clothBottomVPAdapter = ClothVPAdapter(this)
        vp_top.adapter = clothTopVPAdapter
        vp_bottom.adapter = clothBottomVPAdapter


        clothVModel.getClothes(true).observe(this) {
            if (it.isNotEmpty()) {
                tv_top_empty_view.visibility = View.GONE
                clothTopVPAdapter.updateData(ArrayList<BaseClothInterface>(it))
                vp_top.setCurrentItem(it.size - 1, false)
            }

        }

        clothVModel.getClothes(false).observe(this) {
            if (it.isNotEmpty()) {
                tv_bottom_empty_view.visibility = View.GONE
                clothBottomVPAdapter.updateData(ArrayList<BaseClothInterface>(it))
                vp_bottom.setCurrentItem(it.size - 1, false)
            }
        }

        clothVModel.showFav.observe(this) {
            if (it) {
                fab_fav.setImageResource(R.drawable.ic_favorite_fill_24)
            } else {
                fab_fav.setImageResource(R.drawable.ic_favorite_border_24)
            }
        }

        vp_top.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentTopId = clothVModel.getClothes(true).value?.get(position)?.id ?: -1
                checkIsFav()
            }
        })

        vp_bottom.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentBottomId = clothVModel.getClothes(false).value?.get(position)?.id ?: -1
                checkIsFav()
            }
        })

        fab_add_top.setOnClickListener { view ->
            getImage(true)
        }

        fab_add_trouser.setOnClickListener { view ->
            getImage(false)
        }

        fab_fav.setOnClickListener { view ->
            if (currentBottomId != -1 && currentTopId != -1 && !clothVModel.showFav.value!!) {
                clothVModel.addFavoriteCloth(currentTopId, currentBottomId)
                fab_fav.setImageResource(R.drawable.ic_favorite_fill_24)
            }
        }

        fab_shuffle.setOnClickListener { view ->
            val totalTop = clothVModel.getClothes(true).value?.size!!
            val totalBottom = clothVModel.getClothes(false).value?.size!!

            if (totalTop > 1) {
                vp_top.currentItem = (0 until totalTop).random()

            }
            if (totalBottom > 1) {
                vp_bottom.currentItem = (0 until totalBottom).random()
            }
        }

        requestPermission()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA), PERMISSION_REQUEST
            )
        }
    }

    private fun getImage(isTop: Boolean) {
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1).also {
            it.add("Camera")
            it.add("Gallery")
        }
        val builder = AlertDialog.Builder(this)
            .setTitle(R.string.choose_image_from)
            .setAdapter(adapter) { _: DialogInterface, pos: Int ->
                if (pos == 0) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        dispatchTakePictureIntent(isTop)
                    } else {
                        Toast.makeText(this, R.string.allow_permission, Toast.LENGTH_LONG).show()
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(android.Manifest.permission.CAMERA), PERMISSION_REQUEST
                        )
                    }
                } else {
                    pickImage(isTop)
                }
            }
        builder.create().show()
    }

    private fun checkIsFav() {
        clothVModel.isFavoriteCloth(currentTopId, currentBottomId)
    }

    private fun pickImage(isTop: Boolean) {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, if (isTop) PICK_TOP_IMAGE else PICK_BOTTOM_IMAGE)
    }


    private fun dispatchTakePictureIntent(isTop: Boolean) {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.mangal.nb.wardrobe.fileprovider",
                        it
                    )
                    photoUri = photoURI
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        if (isTop) CAPTURE_TOP_IMAGE else CAPTURE_BOTTOM_IMAGE
                    )
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.UK).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) {
            return
        }

        if (requestCode == PICK_TOP_IMAGE || requestCode == PICK_BOTTOM_IMAGE) {
            val uri = data?.data
            if (uri != null) {
                clothVModel.addCloth(uri.toString(), requestCode == PICK_TOP_IMAGE)
            }

        } else if (requestCode == CAPTURE_TOP_IMAGE || requestCode == CAPTURE_BOTTOM_IMAGE) {
            clothVModel.addCloth(photoUri.toString(), requestCode == CAPTURE_TOP_IMAGE)
        }

    }
}