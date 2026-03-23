package com.onatakduman.kserialport.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.onatakduman.kserialport.app.ads.AdConstants
import com.onatakduman.kserialport.app.viewmodel.SerialViewModel

@Composable
fun AdBanner(
    serialViewModel: SerialViewModel,
    modifier: Modifier = Modifier
) {
    val isProUser by serialViewModel.isProUser.collectAsState()
    if (isProUser) return

    val screenWidth = LocalConfiguration.current.screenWidthDp

    AndroidView(
        factory = { context ->
            AdView(context).apply {
                setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidth)
                )
                adUnitId = AdConstants.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}
