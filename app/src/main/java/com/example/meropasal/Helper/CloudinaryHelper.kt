package com.example.meropasal.Helper

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryHelper {
    
    companion object {
        private var isInitialized = false
        
        fun initCloudinary(context: Context) {
            if (!isInitialized) {
                val config = hashMapOf(
                    "cloud_name" to "dktnwa5xl",
                    "api_key" to "183898239154347",
                    "api_secret" to "-Uzdd7aSn5zPjL1Uehy3CR3oc_o"
                )
                MediaManager.init(context, config)
                isInitialized = true
            }
        }
        
        suspend fun uploadImage(context: Context, imageUri: Uri): String {
            initCloudinary(context)
            
            return suspendCancellableCoroutine { continuation ->
                MediaManager.get().upload(imageUri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                        }
                        
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        }
                        
                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["secure_url"] as? String
                            if (imageUrl != null) {
                                continuation.resume(imageUrl)
                            } else {
                                continuation.resumeWithException(Exception("Failed to get image URL from Cloudinary"))
                            }
                        }
                        
                        override fun onError(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                        }
                        
                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            continuation.resumeWithException(Exception("Cloudinary upload rescheduled: ${error.description}"))
                        }
                    })
                    .dispatch()
            }
        }
    }
}