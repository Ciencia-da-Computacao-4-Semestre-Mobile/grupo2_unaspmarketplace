package com.unasp.unaspmarketplace.utils

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.unasp.unaspmarketplace.R

object CartBadgeManager {
    private var currentBottomNav: BottomNavigationView? = null

    fun setupCartBadge(bottomNavigationView: BottomNavigationView) {
        currentBottomNav = bottomNavigationView
    }

    fun updateBadge(count: Int) {
        currentBottomNav?.let { nav ->
            val badge = nav.getOrCreateBadge(R.id.nav_cart)
            if (count > 0) {
                badge.isVisible = true
                badge.number = count
                badge.backgroundColor = android.graphics.Color.RED
            } else {
                badge.isVisible = false
            }
        }
    }

    fun hideBadge() {
        currentBottomNav?.let { nav ->
            nav.removeBadge(R.id.nav_cart)
        }
    }

    fun showBadge(count: Int) {
        updateBadge(count)
    }
}
