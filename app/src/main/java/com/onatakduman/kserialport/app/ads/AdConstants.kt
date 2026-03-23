package com.onatakduman.kserialport.app.ads

import com.onatakduman.kserialport.app.BuildConfig

object AdConstants {
    // Real IDs are loaded from local.properties via BuildConfig.
    // If local.properties doesn't define them, Google's test IDs are used as fallback.
    val BANNER_AD_UNIT_ID: String = BuildConfig.ADMOB_BANNER_ID
    val INTERSTITIAL_AD_UNIT_ID: String = BuildConfig.ADMOB_INTERSTITIAL_ID
}
