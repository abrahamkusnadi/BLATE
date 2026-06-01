package com.example.aol_blate_mobprog;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aol_blate_mobprog.models.Chat;
import com.example.aol_blate_mobprog.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private ArrayList<Chat> chatList;
    private FirebaseFirestore db;
    private FirestoreManager firestoreManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatList = new ArrayList<>();
        recyclerView = findViewById(R.id.rvChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatAdapter(chatList, this);
        recyclerView.setAdapter(adapter);

        // inisialisasi database firabasenya
        db = FirebaseFirestore.getInstance();
        firestoreManager = FirestoreManager.getInstance();

        fetchDataFromFirebase();
        setupNavbar();
        showHelpDialog();
    }

    private void fetchDataFromFirebase() {
        // 1. Panggil manager HANYA untuk mendapatkan Email/ID sesi yang sedang aktif
        firestoreManager.getCurrentUser(new FirestoreManager.FirestoreCallback() {
            @Override
            public void onSuccess(Object result) {
                User cachedUser = (User) result;

                // Ambil identifier unik user (sesuaikan jika di modelmu namanya getId())
                String userEmail = cachedUser.getEmail();

                // 2. TEMBAK ULANG KE FIRESTORE! Minta data paling "fresh" langsung dari server
                db.collection("user")
                        .whereEqualTo("email", userEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {

                                // Ambil dokumen user terbaru dari internet
                                QueryDocumentSnapshot freshUserDoc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                                // Tarik array 'accepted' yang baru
                                List<String> freshAcceptedIds = (List<String>) freshUserDoc.get("accepted");

                                // Cek apakah listnya masih kosong
                                if (freshAcceptedIds == null || freshAcceptedIds.isEmpty()) {
                                    chatList.clear();
                                    adapter.notifyDataSetChanged();
                                    Log.d("CHAT", "User hasn't accepted anyone yet (Fresh check).");
                                    return;
                                }

                                // 3. Jika ada isinya, barulah panggil fungsi pencarian
                                fetchMatches(freshAcceptedIds);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ChatActivity.this, "Gagal menyegarkan data chat", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method
    private void fetchMatches(List<String> acceptedIds) {
        // 1. Bersihkan semua ID yang masuk dari database agar tidak ada spasi/enter
        List<String> cleanAcceptedIds = new ArrayList<>();
        for (String id : acceptedIds) {
            cleanAcceptedIds.add(id.trim()); // .trim() akan membuang spasi/enter yang tidak sengaja tersimpan
        }

        Log.d("CHAT_DEBUG", "Cleaning IDs... Clean list: " + cleanAcceptedIds.toString());

        db.collection("person")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chatList.clear();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String personId = doc.getId().trim(); // Bersihkan juga ID dari dokumen

                            // 2. Sekarang bandingkan dengan list yang sudah bersih
                            if (cleanAcceptedIds.contains(personId)) {
                                Log.d("CHAT_DEBUG", "MATCH FOUND! Adding " + personId);

                                // ... (lanjutkan logika pengambilan data name, about, profile)
                                String name = doc.getString("name");
                                String about = doc.getString("about");
                                String profile = doc.getString("profile");

                                chatList.add(new Chat(name, about, profile));
                            } else {
                                Log.d("CHAT_DEBUG", "No match for " + personId);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupNavbar() {
        ImageView navProfile = findViewById(R.id.ProfileNav);
        ImageView navHistory = findViewById(R.id.HistoryNav);
        ImageView navDiscover = findViewById(R.id.DiscoverNav);

        if(navProfile != null) navProfile.setOnClickListener(v-> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
        if(navHistory != null) navHistory.setOnClickListener(v->{
            startActivity(new Intent(this, HistoryActivity.class));
        });
        if(navDiscover != null) navDiscover.setOnClickListener(v->{
            startActivity(new Intent(this, DiscoverActivity.class));
        });
    }

    private void showHelpDialog(){
        ImageView btnHelp = findViewById(R.id.btnHelp);
        if (btnHelp != null) {
            btnHelp.setOnClickListener(v->{
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.dialog_help);
                if (dialog.getWindow() != null)
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                TextView tvTitle = dialog.findViewById(R.id.tvDialogTitle);
                TextView tvMessage = dialog.findViewById(R.id.tvDialogMessage);
                Button btnClose = dialog.findViewById(R.id.btnCloseDialog);

                tvTitle.setText("Chat Safety");
                tvMessage.setText("Stay polite when chatting. Never share financial information or passwords with anyone.");

                btnClose.setOnClickListener(view -> {
                    dialog.dismiss();
                });
                dialog.show();
            });
        }
    }
}