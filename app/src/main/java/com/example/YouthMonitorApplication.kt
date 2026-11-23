package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.YouthMonitorRepository

class YouthMonitorApplication : Application() {
  val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
  val repository: YouthMonitorRepository by lazy { YouthMonitorRepository(database) }
}
