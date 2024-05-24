package digital.parivartan.beacon

import android.content.Context

object AppInfoHelper {
    private var appName: String? = null
    private var appIcon: Int? = null

    fun getAppName(context: Context): String {
        if (appName == null) {
            val applicationInfo = context.applicationInfo
            appName = if (applicationInfo.labelRes != 0) {
                context.getString(applicationInfo.labelRes)
            } else {
                applicationInfo.nonLocalizedLabel.toString()
            }
        }
        return appName!!
    }

    fun getAppIcon(context: Context): Int {
        if (appIcon == null) {
            val applicationInfo = context.applicationInfo
            appIcon = applicationInfo.icon
        }
        return appIcon!!
    }
}

// TODO: Worst case can raise NullPointerException because of Elvis operator
