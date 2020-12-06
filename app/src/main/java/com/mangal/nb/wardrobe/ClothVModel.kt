package com.mangal.nb.wardrobe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mangal.nb.wardrobe.db.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ClothVModel(application: Application) : AndroidViewModel(application)  {

    private val clothDao: ClothesDao = ClothesDb.getDatabase(application).taskDao()
    private val topClothes: LiveData<List<TopClothEntity>>
    private val bottomClothes: LiveData<List<BottomClothEntity>>
    val showFav: MutableLiveData<Boolean>

    init {
        topClothes = clothDao.getAllTopClothes()
        bottomClothes = clothDao.getAllBottomClothes()
        showFav = MutableLiveData()
    }

    fun getClothes(isTop: Boolean) = if(isTop) topClothes else bottomClothes

    fun addCloth(path: String, isTop: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isTop) clothDao.addTopCloth(TopClothEntity(path))
            else clothDao.addBottomCloth(BottomClothEntity(path))
        }
    }

    fun addFavoriteCloth(topClothId: Int, bottomClothId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            clothDao.addFavCloth(FavClothEntity(topClothId, bottomClothId))
        }
    }


    fun isFavoriteCloth(topClothId: Int, bottomClothId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            showFav.postValue(clothDao.isFavExist(topClothId, bottomClothId)>0)
        }
    }

}