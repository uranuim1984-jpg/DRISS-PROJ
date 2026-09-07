package com.openclaw.androidapp;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.*;
import java.net.*;
import org.json.*;

public class MainActivity extends AppCompatActivity {

    private EditText inputKeywords;
    private Button searchButton;
    private TextView resultText;
    private ProgressBar progressBar;
    private String[] apis = {
        "https://api.duckduckgo.com/?q=",
        "https://rdap.org/domain/",
        "https://tranco-list.eu/api/ranks/"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputKeywords = findViewById(R.id.inputKeywords);
        searchButton = findViewById(R.id.searchButton);
        resultText = findViewById(R.id.resultText);
        progressBar = findViewById(R.id.progressBar);

        searchButton.setOnClickListener(v -> startSearch());
    }

    private void startSearch() {
        String keywords = inputKeywords.getText().toString().trim();
        if (keywords.isEmpty()) {
            Toast.makeText(this, "Entrez des mots-cles", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);
        resultText.setText("Recherche en cours...");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                StringBuilder results = new StringBuilder();
                results.append("=== NicheHunt Results ===\n\n");
                results.append("Mots-cles: ").append(keywords).append("\n\n");

                // DuckDuckGo search
                results.append("--- DuckDuckGo ---\n");
                String encodedKw = URLEncoder.encode(keywords, "UTF-8");
                String ddgUrl = "https://api.duckduckgo.com/?q=" + encodedKw + "&format=json";
                String ddgResponse = makeRequest(ddgUrl);
                if (ddgResponse != null) {
                    JSONObject ddgJson = new JSONObject(ddgResponse);
                    String abstractText = ddgJson.optString("Abstract", "Pas de resultat");
                    results.append("Resume: ").append(abstractText).append("\n\n");

                    JSONArray results2 = ddgJson.optJSONArray("Results");
                    if (results2 != null) {
                        for (int i = 0; i < Math.min(5, results2.length()); i++) {
                            JSONObject r = results2.getJSONObject(i);
                            results.append("• ").append(r.optString("Text", "")).append("\n");
                        }
                    }
                }

                // Related topics
                results.append("\n--- Sujets associes ---\n");
                if (ddgResponse != null) {
                    JSONObject ddgJson = new JSONObject(ddgResponse);
                    JSONArray related = ddgJson.optJSONArray("RelatedTopics");
                    if (related != null) {
                        for (int i = 0; i < Math.min(5, related.length()); i++) {
                            JSONObject r = related.getJSONObject(i);
                            if (r.has("Text")) {
                                results.append("• ").append(r.getString("Text").substring(0, Math.min(80, r.getString("Text").length()))).append("\n");
                            }
                        }
                    }
                }

                // Niche score
                results.append("\n--- Niche Score ---\n");
                int score = calculateScore(keywords, ddgResponse);
                results.append("Score: ").append(score).append("/100\n");
                results.append("Niveau: ").append(getLevel(score)).append("\n");

                final String finalResults = results.toString();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    searchButton.setEnabled(true);
                    resultText.setText(finalResults);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    searchButton.setEnabled(true);
                    resultText.setText("Erreur: " + e.getMessage());
                });
            }
        });
    }

    private String makeRequest(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private int calculateScore(String keywords, String ddgResponse) {
        int score = 50;
        if (ddgResponse != null && ddgResponse.length() > 500) score += 20;
        if (keywords.split(" ").length >= 2) score += 10;
        if (keywords.length() > 10) score += 10;
        if (score > 100) score = 100;
        return score;
    }

    private String getLevel(int score) {
        if (score >= 80) return "Excellent";
        if (score >= 60) return "Bon";
        if (score >= 40) return "Moyen";
        return "Faible";
    }
}
