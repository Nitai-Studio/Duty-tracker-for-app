package com.example.ui

import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.PreferenceHelper
import com.startapp.sdk.adsbase.Ad
import com.startapp.sdk.adsbase.StartAppAd
import com.startapp.sdk.adsbase.StartAppSDK
import com.startapp.sdk.adsbase.VideoListener
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener
import com.startapp.sdk.adsbase.adlisteners.AdEventListener

object StartIoManager {
    private const val TAG = "StartIoManager"
    private const val APP_ID = "205257935"

    private var interstitialAd: StartAppAd? = null
    private var rewardedAd: StartAppAd? = null
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        try {
            // Force disable return ads and splash ads to avoid blocking the user
            StartAppSDK.init(context, APP_ID, true)
            StartAppSDK.enableReturnAds(false)
            
            interstitialAd = StartAppAd(context)
            rewardedAd = StartAppAd(context)
            isInitialized = true
            Log.d(TAG, "Start.io SDK initialized with App ID $APP_ID.")
            preloadInterstitial()
            preloadRewarded()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Start.io: ${e.message}", e)
        }
    }

    fun preloadInterstitial() {
        try {
            interstitialAd?.loadAd(object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.d(TAG, "Interstitial ad loaded.")
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.w(TAG, "Failed to load Interstitial ad.")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Preload Interstitial error: ${e.message}", e)
        }
    }

    fun preloadRewarded() {
        try {
            rewardedAd?.loadAd(StartAppAd.AdMode.REWARDED_VIDEO, object : AdEventListener {
                override fun onReceiveAd(ad: Ad) {
                    Log.d(TAG, "Rewarded ad loaded.")
                }

                override fun onFailedToReceiveAd(ad: Ad?) {
                    Log.w(TAG, "Failed to load Rewarded ad.")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Preload Rewarded error: ${e.message}", e)
        }
    }

    fun showInterstitial(context: Context, prefs: PreferenceHelper, onAdClosed: () -> Unit) {
        init(context)

        // Disable ads completely if user is premium
        if (prefs.isPremium.value) {
            Log.d(TAG, "User is Premium, skipping interstitial.")
            onAdClosed()
            return
        }

        val ad = interstitialAd
        if (ad != null && ad.isReady) {
            try {
                ad.showAd(object : AdDisplayListener {
                    override fun adHidden(ad: Ad?) {
                        Log.d(TAG, "Interstitial dismissed.")
                        preloadInterstitial()
                        onAdClosed()
                    }

                    override fun adDisplayed(ad: Ad?) {
                        Log.d(TAG, "Interstitial displayed.")
                    }

                    override fun adClicked(ad: Ad?) {
                        Log.d(TAG, "Interstitial clicked.")
                    }

                    override fun adNotDisplayed(ad: Ad?) {
                        Log.w(TAG, "Interstitial not displayed.")
                        preloadInterstitial()
                        onAdClosed()
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error showing Interstitial: ${e.message}", e)
                preloadInterstitial()
                onAdClosed()
            }
        } else {
            Log.d(TAG, "Interstitial not ready, calling action directly.")
            preloadInterstitial()
            onAdClosed()
        }
    }

    fun showRewarded(context: Context, prefs: PreferenceHelper, onRewardEarned: () -> Unit, onAdClosed: () -> Unit) {
        init(context)

        // Disable ads completely if user is premium
        if (prefs.isPremium.value) {
            Log.d(TAG, "User is Premium, granting reward and skipping rewarded ad.")
            onRewardEarned()
            onAdClosed()
            return
        }

        val ad = rewardedAd
        if (ad != null && ad.isReady) {
            var completed = false
            try {
                ad.setVideoListener(object : VideoListener {
                    override fun onVideoCompleted() {
                        Log.d(TAG, "Rewarded video completed.")
                        completed = true
                    }
                })

                ad.showAd(object : AdDisplayListener {
                    override fun adHidden(ad: Ad?) {
                        Log.d(TAG, "Rewarded video dismissed.")
                        if (completed) {
                            onRewardEarned()
                        } else {
                            // If user completed most of it or as safety fallback, reward them
                            onRewardEarned()
                        }
                        preloadRewarded()
                        onAdClosed()
                    }

                    override fun adDisplayed(ad: Ad?) {
                        Log.d(TAG, "Rewarded displayed.")
                    }

                    override fun adClicked(ad: Ad?) {
                        Log.d(TAG, "Rewarded clicked.")
                    }

                    override fun adNotDisplayed(ad: Ad?) {
                        Log.w(TAG, "Rewarded not displayed.")
                        preloadRewarded()
                        onRewardEarned() // Safety fallback
                        onAdClosed()
                    }
                })
            } catch (e: Exception) {
                Log.e(TAG, "Error showing Rewarded: ${e.message}", e)
                preloadRewarded()
                onRewardEarned() // Safety fallback
                onAdClosed()
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready, granting reward directly.")
            preloadRewarded()
            onRewardEarned() // Safety fallback
            onAdClosed()
        }
    }
}

@Composable
fun StartIoBannerAd(modifier: Modifier = Modifier, prefs: PreferenceHelper) {
    val isPremium by prefs.isPremium.collectAsState()

    if (isPremium) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                StartIoManager.init(context)
                try {
                    com.startapp.sdk.ads.banner.Banner(context)
                } catch (e: Exception) {
                    Log.e("StartIoBannerAd", "Error creating Banner: ${e.message}", e)
                    View(context)
                }
            }
        )
    }
}
