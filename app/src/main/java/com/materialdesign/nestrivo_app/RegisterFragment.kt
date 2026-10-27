package com.materialdesign.nestrivo_app

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.materialdesign.nestrivo_app.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        // Clear errors when the user starts typing
        binding.etFullName.doOnTextChanged { _, _, _, _ -> binding.tilFullName.error = null }
        binding.etEmail.doOnTextChanged { _, _, _, _ -> binding.tilEmail.error = null }
        binding.etPassword.doOnTextChanged { _, _, _, _ -> binding.tilPassword.error = null }
        binding.etConfirmPassword.doOnTextChanged { _, _, _, _ -> binding.tilConfirmPassword.error = null }

        binding.cbTerms.setOnCheckedChangeListener { _, _ ->
            binding.tvTermsError.visibility = View.GONE
        }

        binding.btnRegister.setOnClickListener {
            if (validateForm()) {
                performRegistration()
            }
        }

        binding.btnGoogleSignUp.setOnClickListener {
            // TODO: Implement Google Sign Up flow
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString() ?: ""
        val isTermsChecked = binding.cbTerms.isChecked

        // Full Name Validation
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.error_field_required)
            isValid = false
        }

        // Email Validation
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.error_field_required)
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.error_invalid_email)
            isValid = false
        }

        // Password Validation
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = getString(R.string.error_password_length)
            isValid = false
        }

        // Confirm Password Validation
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = getString(R.string.error_field_required)
            isValid = false
        } else if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.error_password_mismatch)
            isValid = false
        }

        // Terms and Conditions Validation
        if (!isTermsChecked) {
            binding.tvTermsError.visibility = View.VISIBLE
            isValid = false
        }

        return isValid
    }

    private fun performRegistration() {
        // Form is locally valid.
        // Disable the button and show a loading state before initiating the network call.
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating account..."

        // TODO: Execute backend API integration here.
        // On success -> Navigate to Dashboard/Home
        // On failure -> Re-enable button and display error
    }

    private fun navigateToLogin() {
        // If the Register fragment was opened from the Login fragment,
        // popping the back stack is the safest way to prevent a circular backstack.
        // Alternatively, use findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        // if explicitly defined in your nav_graph.xml.
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}