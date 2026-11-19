package org.fossify.gallery.dialogs

import android.app.Activity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import org.fossify.gallery.extensions.config
import org.fossify.gallery.extensions.directoryDB
import org.fossify.gallery.extensions.mediaDB

/**
 * Gizli tarih filtresi ayarlari. Ana ekrandaki klasor aramasina gizli kod yazilinca acilir.
 */
class HiddenFilterDialog(private val activity: Activity, private val onSaved: () -> Unit) {
    init {
        val options = listOf(
            1 to "Son 1 ay",
            3 to "Son 3 ay",
            6 to "Son 6 ay",
            12 to "Son 1 yıl",
            0 to "Tümü (filtre kapalı)"
        )
        val density = activity.resources.displayMetrics.density
        val pad = (20 * density).toInt()

        val group = RadioGroup(activity)
        options.forEach { (months, label) ->
            group.addView(RadioButton(activity).apply {
                id = View.generateViewId()
                tag = months
                text = label
                isChecked = months == activity.config.hiddenMonths
            })
        }

        val codeInput = EditText(activity).apply {
            setSingleLine()
            setText(activity.config.hiddenCode)
        }

        val hint = TextView(activity).apply {
            text = "Gizli kod (aramaya yazınca bu ekran açılır):"
            setPadding(0, pad / 2, 0, 0)
        }

        val container = LinearLayout(activity).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad / 2, pad, 0)
            addView(group)
            addView(hint)
            addView(codeInput)
        }

        AlertDialog.Builder(activity)
            .setTitle("Gösterilecek süre")
            .setView(container)
            .setPositiveButton("Kaydet") { _, _ ->
                val checked = group.findViewById<RadioButton>(group.checkedRadioButtonId)
                val newMonths = (checked?.tag as? Int) ?: activity.config.hiddenMonths
                val newCode = codeInput.text.toString().trim()
                val changed = newMonths != activity.config.hiddenMonths

                activity.config.hiddenMonths = newMonths
                if (newCode.isNotEmpty()) {
                    activity.config.hiddenCode = newCode
                }

                if (changed) {
                    // Eski onbellek yeni filtreye gore yeniden olussun
                    Thread {
                        try {
                            activity.mediaDB.clearNonDeleted()
                            activity.directoryDB.clearAll()
                        } catch (ignored: Exception) {
                        }
                        activity.runOnUiThread { onSaved() }
                    }.start()
                } else {
                    onSaved()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }
}
