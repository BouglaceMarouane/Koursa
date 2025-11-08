package com.cmc.taximeter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity, userEmail: String) : FragmentStateAdapter(fragmentActivity) {
    private val userEmail: String = userEmail
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MeterFragment()  // Premier fragment
            1 -> MapsFragment()   // Deuxième fragment
            2 -> { // Troisième fragment, UserFragment
                val fragment = UserFragment()
                val bundle = Bundle()
                bundle.putString("userEmail", userEmail)  // Passez l'email à UserFragment
                fragment.arguments = bundle
                fragment
            }
            else -> MeterFragment()  // Par défaut, un autre fragment
        }
    }
}
