package com.materialdesign.nestrivo_app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import com.materialdesign.nestrivo_app.databinding.FragmentSplashBinding

/**
 * Branded splash screen, the start destination of nav_graph.
 *
 * Timeline (3 seconds total, then navigation to HomeFragment):
 *  - 0.0s - 1.2s  logo, accent rule, tagline and routes animate in
 *  - 0.7s - 1.0s  the three loading dots fade in (all idle)
 *  - 1.0s - 3.0s  loading dots light up one after another, two full rounds
 */
class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private var introAnimator: ValueAnimator? = null
    private var introFinished = false
    private var navigated = false

    private var previousLightStatusBars = false
    private var previousLightNavigationBars = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Splash background is always light, so force dark system bar icons while it is visible.
        val window = requireActivity().window
        val controller = WindowCompat.getInsetsController(window, view)
        previousLightStatusBars = controller.isAppearanceLightStatusBars
        previousLightNavigationBars = controller.isAppearanceLightNavigationBars
        controller.isAppearanceLightStatusBars = true
        controller.isAppearanceLightNavigationBars = true

        // Keep the horizontal margin from XML and add system bar / cutout insets on top of it.
        val container = binding.contentContainer
        val baseStart = container.paddingStart
        val baseEnd = container.paddingEnd
        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            v.setPaddingRelative(baseStart + bars.left, bars.top, baseEnd + bars.right, bars.bottom)
            insets
        }

        // Navigate only while the view is at least STARTED (never from the background).
        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                navigateIfReady()
            }
        })

        if (ValueAnimator.areAnimatorsEnabled()) {
            render(0f)
            startIntro()
        } else {
            // Animations disabled system-wide: show the final state and move on immediately.
            render(ENTRANCE_MS)
            introFinished = true
        }
    }

    override fun onDestroyView() {
        introAnimator?.run {
            removeAllListeners()
            removeAllUpdateListeners()
            cancel()
        }
        introAnimator = null

        // Give the next screens the system bar appearance they had before the splash.
        val window = requireActivity().window
        val controller = WindowCompat.getInsetsController(window, binding.root)
        controller.isAppearanceLightStatusBars = previousLightStatusBars
        controller.isAppearanceLightNavigationBars = previousLightNavigationBars

        _binding = null
        super.onDestroyView()
    }

    private fun startIntro() {
        introAnimator = ValueAnimator.ofFloat(0f, TOTAL_MS).apply {
            duration = TOTAL_MS.toLong()
            interpolator = LinearInterpolator()
            addUpdateListener { render(it.animatedValue as Float) }
            addListener(object : AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: Animator) {
                    cancelled = true
                }

                override fun onAnimationEnd(animation: Animator) {
                    if (cancelled) return
                    introFinished = true
                    navigateIfReady()
                }
            })
            start()
        }
    }

    /** Draws one frame of the splash timeline for [elapsedMs] milliseconds since it started. */
    private fun render(elapsedMs: Float) {
        val b = _binding ?: return
        val enterOffset = resources.getDimension(R.dimen.splash_logo_enter_offset)

        // Entrance choreography, spread over the first ENTRANCE_MS.
        val t = (elapsedMs / ENTRANCE_MS).coerceIn(0f, 1f)

        val logo = easeOut(segment(t, 0f, 0.45f))
        b.ivLogo.alpha = logo
        b.ivLogo.scaleX = 0.94f + 0.06f * logo
        b.ivLogo.scaleY = 0.94f + 0.06f * logo
        b.ivLogo.translationY = (1f - logo) * enterOffset

        val rule = easeOut(segment(t, 0.30f, 0.55f))
        b.viewAccentRule.alpha = rule
        b.viewAccentRule.scaleX = rule

        b.tvTagline.alpha = easeOut(segment(t, 0.40f, 0.78f))


        // Loading dots: fade in while idle, then light up one by one in rounds of DOTS_CYCLE_MS.
        b.loadingDots.alpha = segment(elapsedMs, DOTS_FADE_IN_START_MS, DOTS_START_MS)
        b.loadingDots.cycle = if (elapsedMs < DOTS_START_MS) {
            -1f
        } else {
            ((elapsedMs - DOTS_START_MS) / DOTS_CYCLE_MS) % 1f
        }
    }

    private fun navigateIfReady() {
        if (navigated || !introFinished || _binding == null) return
        if (!viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return

        val navController = findNavController()
        if (navController.currentDestination?.id != R.id.splashFragment) return

        navigated = true
        navController.navigate(R.id.action_splashFragment_to_homeFragment)
    }

    private fun segment(t: Float, start: Float, end: Float): Float =
        ((t - start) / (end - start)).coerceIn(0f, 1f)

    private fun easeOut(x: Float): Float {
        val inv = 1f - x
        return 1f - inv * inv * inv
    }

    private companion object {
        /** Total splash duration before navigating to HomeFragment. */
        const val TOTAL_MS = 3000f

        /** Duration of the entrance animation (logo, tagline, routes). */
        const val ENTRANCE_MS = 1200f

        const val DOTS_FADE_IN_START_MS = 700f
        const val DOTS_START_MS = 1000f

        /** One full round of dot 1 -> dot 2 -> dot 3. (3000 - 1000) / 1000 = exactly 2 rounds. */
        const val DOTS_CYCLE_MS = 1000f
    }
}