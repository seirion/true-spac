package com.trueedu.spac.repo.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.trueedu.spac.data.log.logD

abstract class FirebaseDatabaseBase {
    protected val database = FirebaseDatabase.getInstance()

    protected suspend fun firebaseCurrentUser(): FirebaseUser? {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            logD("cannot write values: currentUser == null")
            return null
        }
        return currentUser
    }
}
