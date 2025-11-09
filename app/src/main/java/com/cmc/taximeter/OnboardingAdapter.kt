package com.cmc.taximeter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * OnboardingAdapter
 *
 * RecyclerView adapter for displaying onboarding slides in ViewPager2.
 *
 * Purpose:
 * - Manages onboarding page views
 * - Binds onboarding data to each page
 * - Used by OnboardingActivity with ViewPager2
 *
 * Each onboarding item contains:
 * - Image (icon/illustration)
 * - Title text
 * - Description text
 *
 * RecyclerView.Adapter methods:
 * - onCreateViewHolder: Inflates page layout
 * - onBindViewHolder: Populates page with data
 * - getItemCount: Returns number of pages
 *
 * @param items List of OnboardingItem objects to display
 *
 * @author BOUGLACE Marouane
 * @version 1.0
 */
class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    /**
     * ViewHolder for onboarding page items.
     *
     * Holds references to views within each onboarding page:
     * - Image: Icon or illustration
     * - Title: Main heading
     * - Description: Explanatory text
     *
     * @param view The root view of the onboarding item layout
     */
    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** ImageView for displaying onboarding icon/illustration */
        val image: ImageView = view.findViewById(R.id.ivOnboarding)

        /** TextView for displaying onboarding title */
        val title: TextView = view.findViewById(R.id.tvTitle)

        /** TextView for displaying onboarding description */
        val description: TextView = view.findViewById(R.id.tvDescription)
    }

    /**
     * Creates a new ViewHolder when needed.
     *
     * Called by RecyclerView when it needs a new ViewHolder to represent an item.
     *
     * Process:
     * 1. Inflate item_onboarding layout
     * 2. Create OnboardingViewHolder with inflated view
     * 3. Return ViewHolder
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused here - single type)
     * @return A new OnboardingViewHolder that holds an onboarding item View
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        // Inflate the onboarding item layout
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)

        // Create and return ViewHolder
        return OnboardingViewHolder(view)
    }

    /**
     * Binds data to an existing ViewHolder.
     *
     * Called by RecyclerView to display data at a specific position.
     *
     * Process:
     * 1. Get OnboardingItem for this position
     * 2. Set image resource
     * 3. Set title text
     * 4. Set description text
     *
     * @param holder The ViewHolder to update with data
     * @param position The position of the item in the data list
     */
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        // Get the onboarding item for this position
        val item = items[position]

        // Populate ViewHolder with item data
        holder.image.setImageResource(item.image)
        holder.title.text = item.title
        holder.description.text = item.description
    }

    /**
     * Returns the total number of onboarding items.
     *
     * @return Number of onboarding pages to display
     */
    override fun getItemCount(): Int = items.size
}