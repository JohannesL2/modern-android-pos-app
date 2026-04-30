package com.example.test_design.utils

import com.example.test_design.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource

object CategoryMapper {
    /**
     * Översätter kategori-ID från databasen till ett visningsnamn.
     * Den letar efter strängar som heter "cat_idnamn" i strings.xml.
     */
    @Composable
    fun getDisplayName(categoryId: String): String {
        val context = LocalContext.current

        // 1. Hantera system-ID:n (specialfall)
        val systemResId = when (categoryId) {
            "all_id" -> R.string.category_all
            "favorites_id" -> R.string.category_favorites
            else -> null
        }

        if (systemResId != null) return stringResource(systemResId)

        // 2. Hantera produkt-kategorier (t.ex. "dryck", "mat")
        val resName = "cat_${categoryId.lowercase()}"
        val resId = context.resources.getIdentifier(resName, "string", context.packageName)

        return if (resId != 0) {
            stringResource(resId)
        } else {
            // Om översättning saknas: Gör "dryck" -> "Dryck"
            categoryId.replaceFirstChar { it.uppercase() }
        }
    }
}

/**
 * Översätter produktnamn baserat på artikelnummer.
 * Letar efter strings som: <string name="prod_100001">Kaffe</string>
 */
@Composable
fun translateProductName(articleNumber: String, defaultName: String): String {
    val context = LocalContext.current
    val resName = "prod_$articleNumber"

    val resId = context.resources.getIdentifier(resName, "string", context.packageName)

    return if (resId != 0) {
        stringResource(resId)
    } else {
        defaultName
    }
}