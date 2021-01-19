package com.cnx.nextvpublisher_vxg2

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FileUploader(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        return storeImage()
    }

    fun storeImage(): Result {
        var result: Result? = null
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("feed/" + "primary" + ".png")
        inputData.getString("snapshot")?.let {
            imageRef.putFile(it.toUri())
                .addOnSuccessListener {
                    Log.d("TESTING_STIGMA", "Success photo saved")
                    imageRef.downloadUrl.addOnSuccessListener {
                        Log.d("TESTING_STIGMA", "Success url")
                        FirebaseFirestore.getInstance().collection("tvs").document("1234")
                            .collection("sur")
                            .document("primary")
                            .collection("cams")
                            .document("1234")
                            .update("imageUrl", it.toString())
                            .addOnSuccessListener {
                                Log.d("TESTING_STIGMA", "Success photo url saved")
                                result = Result.success()
                            }
                            .addOnFailureListener {
                                result = Result.failure()
                            }

                    }
                        .addOnFailureListener {
                            result = Result.failure()
                        }
                }
                .addOnFailureListener {
                    Log.d("TESTING_STIGMA", "failure photo saved")
                    result = Result.failure()
                }
        }

        return result!!
    }

}