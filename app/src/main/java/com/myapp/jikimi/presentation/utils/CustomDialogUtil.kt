package com.myapp.jikimi.presentation.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import com.myapp.jikimi.databinding.CustomDialogBinding

object CustomDialogUtil {

    fun showDialog(
        context: Context,
        message: String,
        positiveText: String,
        negativeText: String = "취소",
        onConfirm: () -> Unit
    ) {
        val dialog = Dialog(context)
        val dialogBinding = CustomDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogBinding.dialogTv.text = message
        dialogBinding.dialogDeleteBtn.text = positiveText
        dialogBinding.dialogCancelBtn.text = negativeText

        dialogBinding.dialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.dialogDeleteBtn.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
    }
}
