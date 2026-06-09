package com.example.aol_blate_mobprog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.aol_blate_mobprog.models.Message;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private ArrayList<Message> messageList;
    private EditText etInput;
    private ImageView btnSend, btnBack;
    private TextView tvName;
    private String name, imageStr;
    private ImageView btnOpenProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // inisialisasi
        recyclerView = findViewById(R.id.rvMessages);
        etInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvChatName);
        btnOpenProfile = findViewById(R.id.OpenProfileChatDetailImg);


        // get nama dari chatactivity
        name = getIntent().getStringExtra("userName");
        imageStr = getIntent().getStringExtra("userImage");
        if(name != null) tvName.setText(name);

        // setup rv
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, this, imageStr, name);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 4. load dummy hardcoded Data
        loadDummyMessages();

        //setup button send
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = etInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    sendMessage(content);
                }
            }
        });

        // setup button back
        btnBack.setOnClickListener(v -> finish());

        if (btnOpenProfile != null) {
            btnOpenProfile.setOnClickListener(v -> {
                Intent intent = new Intent(ChatDetailActivity.this, ChatProfileActivity.class);
                intent.putExtra("userName", tvName.getText().toString());

                startActivity(intent);
            });
        }
    }

    private void loadDummyMessages() {
        messageList.add(new Message("Hey! Saw your profile and you seem really cool. What are you up to this weekend?", "10:30 AM", false));
        messageList.add(new Message("Hey " + name + " ! Thanks 😊 Just chilling this weekend. Maybe some hiking. How about you?", "10:32 AM", true));
        messageList.add(new Message("Hiking sounds awesome! I was thinking of checking out that new cafe downtown. We should go sometime!", "10:33 AM", false));

        adapter.notifyDataSetChanged();
    }

    private void sendMessage(String content) {
        String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        messageList.add(new Message(content, currentTime, true));

        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.smoothScrollToPosition(messageList.size() - 1);

        etInput.setText("");
    }
}