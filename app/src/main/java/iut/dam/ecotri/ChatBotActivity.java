package iut.dam.ecotri;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import iut.dam.ecotri.data.ApiClient;
import iut.dam.ecotri.data.ChatApi;

import iut.dam.ecotri.data.ChatRequest;
import iut.dam.ecotri.data.ChatResponse;
import iut.dam.ecotri.data.ChatAction;
import iut.dam.ecotri.data.HistoryMessage;

public class ChatBotActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private TextInputEditText etMessage;
    private MaterialButton btnSend;

    private ChatAdapter adapter;
    private final List<ChatMessage> messages = new ArrayList<>();

    private SessionManager session;
    private String conversationId;
    private ChatApi chatApi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        session = new SessionManager(this);
        if (session.getToken() == null || session.getToken().isEmpty()) {
            finish();
            return;
        }

        conversationId = java.util.UUID.randomUUID().toString();
        chatApi = ApiClient.INSTANCE.getChatApi();

        // Toolbar
        MaterialToolbar tb = findViewById(R.id.toolbarChatbot);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        tb.setNavigationOnClickListener(v -> finish());

        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Recycler setup
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // start from bottom like chat
        recyclerChat.setLayoutManager(lm);

        adapter = new ChatAdapter(messages);
        recyclerChat.setAdapter(adapter);

        // Message d'accueil bot
        addBotMessage("Salut 👋 Je suis l’assistant EcoTri.\n" +
                "Pose-moi une question sur le tri, le calendrier, ou les déchèteries !");

        btnSend.setOnClickListener(v -> onSendClicked());
    }

    private void onSendClicked() {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "Écris un message", Toast.LENGTH_SHORT).show();
            return;
        }

        etMessage.setText("");
        addUserMessage(text);

        String token = session.getToken();
        List<HistoryMessage> history = buildHistory();
        ChatRequest body = new ChatRequest(conversationId, text, history);

        btnSend.setEnabled(false);

        chatApi.chat("Bearer " + token, body).enqueue(new retrofit2.Callback<ChatResponse>() {
            @Override
            public void onResponse(retrofit2.Call<ChatResponse> call, retrofit2.Response<ChatResponse> response) {
                btnSend.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse r = response.body();
                    String reply = (r.getReply() != null) ? r.getReply() : "(sans réponse)";
                    addBotMessage(reply);

                    ChatAction act = r.getAction();
                    if (act != null) handleAction(act);
                } else {
                    addBotMessage("❌ Erreur chatbot (" + response.code() + ")");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ChatResponse> call, Throwable t) {
                btnSend.setEnabled(true);
                addBotMessage("❌ Erreur réseau : " + t.getMessage());
            }
        });
    }

    private void handleAction(ChatAction action) {
        if (action.getType() == null) return;

        if ("OPEN_SCREEN".equals(action.getType())) {

            if ("DEMANDE_BAC".equals(action.getScreen())) {
                android.content.Intent i = new android.content.Intent(this, DemandeBacActivity.class);
                startActivity(i);
                return;
            }

            if ("CALENDRIER".equals(action.getScreen())) {
                android.content.Intent i = new android.content.Intent(this, CalendarActivity.class);
                startActivity(i);
                return;
            }

            if ("ACCUEIL".equals(action.getScreen())) {
                android.content.Intent i = new android.content.Intent(this, MainActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            }
        }
    }

    private void fakeBotAnswer(String userText) {
        // Petite "réponse" temporaire pour tester l'UI
        addBotMessage("✅ Bien reçu : \"" + userText + "\"\n" +
                "Bientôt je te réponds via l'IA (backend).");
    }

    private void addUserMessage(String text) {
        messages.add(new ChatMessage(text, true, System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void addBotMessage(String text) {
        messages.add(new ChatMessage(text, false, System.currentTimeMillis()));
        adapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
    }

    private void scrollToBottom() {
        recyclerChat.post(() -> recyclerChat.smoothScrollToPosition(adapter.getItemCount() - 1));
    }

    private List<HistoryMessage> buildHistory() {
        List<HistoryMessage> h = new ArrayList<>();

        // On peut limiter aux 12 derniers comme ton backend
        int start = Math.max(0, messages.size() - 12);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            String role = m.isUser() ? "user" : "assistant";
            h.add(new HistoryMessage(role, m.getText()));
        }
        return h;
    }
}
