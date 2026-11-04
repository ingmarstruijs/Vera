package net.moonbaseone.vera.shared.vault

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import net.moonbaseone.vera.shared.db.VeraDatabase

fun createAndroidVeraDatabase(context: Context, name: String = "vera_vault.db"): VeraDatabase {
    val driver = AndroidSqliteDriver(
        schema = VeraDatabase.Schema,
        context = context.applicationContext,
        name = name,
    )
    return VeraDatabase(driver)
}
