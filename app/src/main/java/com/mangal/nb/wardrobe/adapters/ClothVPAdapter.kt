package com.mangal.nb.wardrobe.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mangal.nb.wardrobe.ClothFragment
import com.mangal.nb.wardrobe.db.BaseClothInterface

class ClothVPAdapter(fa: FragmentActivity,
                     private var items: List<BaseClothInterface>): FragmentStateAdapter(fa) {

    constructor(fa: FragmentActivity):this(fa, ArrayList())

    fun updateData(items: List<BaseClothInterface>){
        this.items = items
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun createFragment(position: Int): Fragment {
        return ClothFragment.newInstance(items[position].path)
    }
}