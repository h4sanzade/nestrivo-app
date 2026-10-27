package com.materialdesign.nestrivo_app

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.materialdesign.nestrivo_app.databinding.FragmentLoginBinding
import kotlin.math.max


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // The auth screens are always light, so keep the system bar icons dark.
        WindowCompat.getInsetsController(requireActivity().window, view).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        // System bars + keyboard: the page scrolls instead of hiding fields behind the keyboard.
        ViewCompat.setOnApplyWindowInsetsListener(binding.loginRoot) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, max(bars.bottom, ime.bottom))
            insets
        }

        ViewCompat.setAccessibilityHeading(binding.tvTitle, true)

        // Password inputs can fall back to a monospace font; keep Poppins in both states.
        val inputFont = ResourcesCompat.getFont(requireContext(), R.font.poppins_regular)
        binding.etEmail.typeface = inputFont
        binding.etPassword.typeface = inputFont

        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
        binding.etPassword.doAfterTextChanged { binding.tilPassword.error = null }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.btnGoogle.setOnClickListener { showPreviewMessage(R.string.preview_google_message) }

        // Register and Forgot Password screens are added in the next steps.
        binding.tvForgotPassword.setOnClickListener { showPreviewMessage(R.string.preview_screen_message) }
        binding.tvSignUp.setOnClickListener { showPreviewMessage(R.string.preview_screen_message) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /** Local field validation only. Nothing is sent anywhere. */
    private fun attemptLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        val emailError = when {
            email.isEmpty() -> getString(R.string.error_email_required)
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> getString(R.string.error_email_invalid)
            else -> null
        }
        val passwordError = if (password.isEmpty()) getString(R.string.error_password_required) else null

        binding.tilEmail.error = emailError
        binding.tilPassword.error = passwordError

        when {
            emailError != null -> binding.etEmail.requestFocus()
            passwordError != null -> binding.etPassword.requestFocus()
            else -> {
                binding.root.clearFocus()
                WindowCompat.getInsetsController(requireActivity().window, binding.root)
                    .hide(WindowInsetsCompat.Type.ime())
                showPreviewMessage(R.string.preview_login_message)
            }
        }
    }

    private fun showPreviewMessage(@StringRes messageRes: Int) {
        Snackbar.make(binding.root, messageRes, Snackbar.LENGTH_SHORT).show()
    }
}