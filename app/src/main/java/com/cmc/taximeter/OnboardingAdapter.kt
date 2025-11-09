package com.cmc.taximeter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView

/**
 * OnboardingAdapter
 *
 * RecyclerView adapter for displaying onboarding slides in ViewPager2.
 * Now supports both static images and Lottie animations.
 *
 * @param items List of OnboardingItem objects to display
 * @author BOUGLACE Marouane
 * @version 2.0
 */
class OnboardingAdapter(private val items: List<OnboardingItem>) :
    RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    /**
     * ViewHolder for onboarding page items.
     *
     * Holds references to views within each onboarding page:
     * - Image: Static icon/illustration (for non-animated pages)
     * - LottieAnimation: Animated illustration (for Lottie pages)
     * - Title: Main heading
     * - Description: Explanatory text
     *
     * @param view The root view of the onboarding item layout
     */
    class OnboardingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        /** ImageView for displaying static onboarding icon/illustration */
        val image: ImageView = view.findViewById(R.id.ivOnboarding)

        /** LottieAnimationView for displaying animated illustrations */
        val lottieAnimation: LottieAnimationView = view.findViewById(R.id.lottieAnimation)

        /** TextView for displaying onboarding title */
        val title: TextView = view.findViewById(R.id.tvTitle)

        /** TextView for displaying onboarding description */
        val description: TextView = view.findViewById(R.id.tvDescription)
    }

    /**
     * Creates a new ViewHolder when needed.
     *
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused here - single type)
     * @return A new OnboardingViewHolder that holds an onboarding item View
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    /**
     * Binds data to an existing ViewHolder.
     *
     * Handles both Lottie animations and static images:
     * - If lottieFile is provided: Show Lottie animation, hide ImageView
     * - If image is provided: Show ImageView, hide Lottie animation
     *
     * @param holder The ViewHolder to update with data
     * @param position The position of the item in the data list
     */
    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = items[position]

        // Set title and description
        holder.title.text = item.title
        holder.description.text = item.description

        // Show either Lottie animation or static image
        if (item.lottieFile != null) {
            // Show Lottie animation
            holder.lottieAnimation.visibility = View.VISIBLE
            holder.image.visibility = View.GONE

            holder.lottieAnimation.setAnimation(item.lottieFile)
            holder.lottieAnimation.playAnimation()
        } else if (item.image != null) {
            // Show static image
            holder.image.visibility = View.VISIBLE
            holder.lottieAnimation.visibility = View.GONE

            holder.image.setImageResource(item.image)
        } else {
            // Neither provided - hide both
            holder.image.visibility = View.GONE
            holder.lottieAnimation.visibility = View.GONE
        }
    }

    /**
     * Returns the total number of onboarding items.
     *
     * @return Number of onboarding pages to display
     */
    override fun getItemCount(): Int = items.size
}