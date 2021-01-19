package com.cnx.nextvpublisher_vxg2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore

object FirebasePublisherListener {

    fun shouldPublish(): LiveData<Boolean> {
        val startPublishing: MutableLiveData<Boolean> = MutableLiveData()

        FirebaseFirestore.getInstance().collection("tvs").document("1234").collection("sur")
            .document("primary")
            .collection("cams")
            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (value!!.documents.size > 0) {
                    val bool = value.documents[0].getBoolean("shouldPublish")
                    startPublishing.postValue(bool)
                }

            }

        return startPublishing
    }

}