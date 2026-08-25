package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kas XI-F4", appName)
  }

  @Test
  fun `test working days calculation excludes weekends`() {
    // 2026-08: August 2026 has 31 days. August 1 is Saturday, August 2 is Sunday.
    // 21 or 22 working days in August 2026.
    val workingDays = DateUtils.getWorkingDaysForYearMonth("2026-08")
    assertTrue(workingDays.isNotEmpty())
    workingDays.forEach { date ->
      assertFalse(DateUtils.isWeekend(date))
    }
  }
}
