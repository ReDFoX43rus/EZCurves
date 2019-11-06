package com.liberaid.ezcurves.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NotifyDialog : DialogFragment() {

    private var title: String? = null
    private var message: String? = null
    private var btnOk: BtnHandler? = null
    private var btnAction: BtnHandler? = null
    private var btnNo: BtnHandler? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = context ?: return super.onCreateDialog(savedInstanceState)

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)

        val btnOk = btnOk
        if(btnOk != null)
            builder.setPositiveButton(btnOk.title) { _, _ ->
                btnOk.action()
            }

        val btnAction = btnAction
        if(btnAction != null)
            builder.setNeutralButton(btnAction.title) { _, _ ->
                btnAction.action()
            }

        val btnNo = btnNo
        if(btnNo != null)
            builder.setNegativeButton(btnNo.title) { _, _ ->
                btnNo.action()
            }

        return builder.create()
    }

    class BtnHandler(val title: String, val action: () -> Unit)

    companion object {
        fun create(title: String, message: String, btnOk: BtnHandler, btnAction: BtnHandler, btnNo: BtnHandler): NotifyDialog {
            val dialog = NotifyDialog()
            dialog.title = title
            dialog.message = message
            dialog.btnOk = btnOk
            dialog.btnAction = btnAction
            dialog.btnNo = btnNo

            return dialog
        }
    }
}