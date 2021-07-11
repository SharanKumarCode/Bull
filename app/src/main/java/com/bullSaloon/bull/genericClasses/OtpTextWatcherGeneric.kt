package com.bullSaloon.bull.genericClasses

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.bullSaloon.bull.R
import com.bullSaloon.bull.databinding.FragmentCreateAccountBinding
import com.bullSaloon.bull.databinding.FragmentSignInBinding

class OtpTextWatcherGeneric(view: View, otpEditTextBoxes: MutableList<EditText>):
    TextWatcher {

    private lateinit var _view: View
    private lateinit var _otpEditTextBoxes: MutableList<EditText>
    private lateinit var focusedDrawable: Drawable
    private lateinit var unFocusedDrawable: Drawable
    private lateinit var verifyButton: Button

    constructor(view: View, otpEditTextBoxes: MutableList<EditText>, binding: FragmentCreateAccountBinding):this(view, otpEditTextBoxes){
        this._view = view
        this._otpEditTextBoxes = otpEditTextBoxes
        this.verifyButton = binding.verifyOtpButton
        this.focusedDrawable = binding?.root.resources.getDrawable(R.drawable.bg_otp_box_focused)
        this.unFocusedDrawable = binding?.root.resources.getDrawable(R.drawable.bg_otp_box)
    }

    constructor(view: View, otpEditTextBoxes: MutableList<EditText>, binding: FragmentSignInBinding):this(view, otpEditTextBoxes){
        this._view = view
        this._otpEditTextBoxes = otpEditTextBoxes
        this.verifyButton = binding.verifyOtpButton
        this.focusedDrawable = binding?.root.resources.getDrawable(R.drawable.bg_otp_box_focused)
        this.unFocusedDrawable = binding?.root.resources.getDrawable(R.drawable.bg_otp_box)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun afterTextChanged(p0: Editable?) {

        when(_view.id){
            R.id.otpBox1 -> {
                if (p0.toString().length == 1){
                    _otpEditTextBoxes[1].requestFocus()
                    _otpEditTextBoxes[1].text.clear()
                    _otpEditTextBoxes[1].background = focusedDrawable
                    _otpEditTextBoxes[0].background = unFocusedDrawable
                }
            }
            R.id.otpBox2 -> {
                if (p0.toString().length == 1){
                    _otpEditTextBoxes[2].requestFocus()
                    _otpEditTextBoxes[2].text.clear()
                    _otpEditTextBoxes[2].background = focusedDrawable
                    _otpEditTextBoxes[1].background = unFocusedDrawable
                } else {
                    _otpEditTextBoxes[0].requestFocus()
                    _otpEditTextBoxes[0].background = focusedDrawable
                    _otpEditTextBoxes[1].background = unFocusedDrawable
                }
            }
            R.id.otpBox3 -> {
                if (p0.toString().length == 1){
                    _otpEditTextBoxes[3].requestFocus()
                    _otpEditTextBoxes[3].text.clear()
                    _otpEditTextBoxes[3].background = focusedDrawable
                    _otpEditTextBoxes[2].background = unFocusedDrawable
                } else {
                    _otpEditTextBoxes[1].requestFocus()
                    _otpEditTextBoxes[1].background = focusedDrawable
                    _otpEditTextBoxes[2].background = unFocusedDrawable
                }
            }
            R.id.otpBox4 -> {
                if (p0.toString().length == 1){
                    _otpEditTextBoxes[4].requestFocus()
                    _otpEditTextBoxes[4].text.clear()
                    _otpEditTextBoxes[4].background = focusedDrawable
                    _otpEditTextBoxes[3].background = unFocusedDrawable
                } else {
                    _otpEditTextBoxes[2].requestFocus()
                    _otpEditTextBoxes[2].background = focusedDrawable
                    _otpEditTextBoxes[3].background = unFocusedDrawable
                }
            }
            R.id.otpBox5 -> {
                if (p0.toString().length == 1){
                    _otpEditTextBoxes[5].requestFocus()
                    _otpEditTextBoxes[5].text.clear()
                    _otpEditTextBoxes[5].background = focusedDrawable
                    _otpEditTextBoxes[4].background = unFocusedDrawable
                } else {
                    _otpEditTextBoxes[3].requestFocus()
                    _otpEditTextBoxes[3].background = focusedDrawable
                    _otpEditTextBoxes[4].background = unFocusedDrawable
                }
            }
            R.id.otpBox6 -> {
                if (p0.toString().length == 1){
                    verifyButton.apply {
                        isClickable = true
                        alpha = 1F
                    }
                } else {
                    _otpEditTextBoxes[4].requestFocus()
                    _otpEditTextBoxes[4].background = focusedDrawable
                    _otpEditTextBoxes[5].background = unFocusedDrawable
                    verifyButton.apply {
                        isClickable = false
                        alpha = 0.2F
                    }
                }
            }
        }

    }

}