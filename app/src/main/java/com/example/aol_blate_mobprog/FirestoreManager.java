package com.example.aol_blate_mobprog;

import com.example.aol_blate_mobprog.models.Person;
import com.example.aol_blate_mobprog.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    private static FirestoreManager instance;
    private final FirebaseFirestore db;
    private final CollectionReference usersRef;
    private final CollectionReference peopleRef;
    private FirebaseAuth auth;

    // menyimpan login user secara dinamis
    private String currentUserId;

    private FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("user");
        peopleRef = db.collection("person");
        auth = FirebaseAuth.getInstance();
    }

    public static synchronized FirestoreManager getInstance() {
        if (instance == null) instance = new FirestoreManager();
        return instance;
    }

    public interface FirestoreCallback {
        void onSuccess(Object result);
        void onFailure(Exception e);
    }

    // --- USER FEATURES ---

    // 1. Handle Login Query
    public void loginUser(String email, String password, FirestoreCallback callback) {
        usersRef.whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                        User user = doc.toObject(User.class);

                        //Simpan ID untuk deteksi yg login dengan metode (getCurrentUser/swipe)
                        this.currentUserId = doc.getId();

                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User is not registered or wrong password"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    // 2. Register New User
    public void registerNewUser(User newUser, FirestoreCallback callback) {
        // menggunakan String.valueOf(newUser.getId()) untuk membuat ID Dokumen. Memastikan ID cocok dengan Kunci

        usersRef.document(String.valueOf(newUser.getId())).set(newUser)
                .addOnSuccessListener(aVoid -> {
                    // Automatically log them in after registration
                    this.currentUserId = String.valueOf(newUser.getId());
                    callback.onSuccess("Success");
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserData(String documentId, Map<String, Object> updates, FirestoreCallback callback){
        if (documentId == null || documentId.isEmpty()) {
            callback.onFailure(new Exception("Invalid User ID. Cannot update data"));
            return;
        }

        usersRef.document(documentId).update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Update Successful"))
                .addOnFailureListener(callback::onFailure);
    }
    public void getAllCandidates(FirestoreCallback callback) {
        peopleRef.get().addOnSuccessListener(querySnapshot -> {
            List<Person> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot) list.add(doc.toObject(Person.class));
            callback.onSuccess(list);
        });
    }

    // 3. Get Current User
    public void getCurrentUser(FirestoreCallback callback) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            callback.onFailure(new Exception("No user is currently logged in."));
            return;
        }

        usersRef.document(currentUserId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                callback.onSuccess(document.toObject(User.class));
            } else {
                callback.onFailure(new Exception("User data not found in database"));
            }
        }).addOnFailureListener(callback::onFailure);
    }

    // 4. Save Swipe Action
    public void saveSwipeAction(String targetUserId, boolean isLike) {
        if (currentUserId == null) {
            android.util.Log.e("DATING_APP", "Cannot swipe: No user logged in.");
            return;
        }

        String fieldName = isLike ? "accepted" : "rejected";

        usersRef.document(currentUserId).update(fieldName, FieldValue.arrayUnion(targetUserId))
                .addOnSuccessListener(aVoid -> android.util.Log.d("DATING_APP", "Swipe Saved Successfully!"))
                .addOnFailureListener(e -> android.util.Log.e("DATING_APP", "Swipe Failed: " + e.getMessage()));
    }
}
