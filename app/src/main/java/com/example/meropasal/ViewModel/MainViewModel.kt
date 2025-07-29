package com.example.meropasal.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.meropasal.Model.CategoryModel
import com.example.meropasal.Model.ItemsModel
import com.example.meropasal.Model.SliderModel
import com.example.meropasal.Repository.MainRepository

class MainViewModel(): ViewModel() {
    private val repository= MainRepository()


    fun loadBanner(): LiveData<MutableList<SliderModel>>{
        return repository.loadBanner()
    }

    fun loadCategory(): LiveData<MutableList<CategoryModel>>{
        return repository.loadCategory()
    }

    fun loadPopular(): LiveData<MutableList<ItemsModel>>{
        return repository.loadPopular()
    }

    fun loadFiltered(id: String): LiveData<MutableList<ItemsModel>>{
        return repository.loadFiltered(id)
    }
}