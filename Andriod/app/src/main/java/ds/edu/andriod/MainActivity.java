// Name: Vishal Chatterjee
// AndrewID: vchatter

package ds.edu.andriod;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // UI components
    Spinner competitionSpinner;
    Button fetchFixturesButton, fetchTeamsButton, viewTopScorersButton;

    // HTTP client for API requests
    OkHttpClient client = new OkHttpClient();

    // List to hold competition names for the spinner
    ArrayList<String> competitionNames = new ArrayList<>();

    // Map to link competition name to its ID
    HashMap<String, Integer> competitionIdMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for the activity

        // Bind UI elements to their IDs
        competitionSpinner = findViewById(R.id.competitionSpinner);
        fetchFixturesButton = findViewById(R.id.fetchFixturesButton);
        fetchTeamsButton = findViewById(R.id.fetchTeamsButton);
        viewTopScorersButton = findViewById(R.id.viewTopScorersButton);

        // Disable buttons until data is loaded from server
        fetchFixturesButton.setEnabled(false);
        fetchTeamsButton.setEnabled(false);
        viewTopScorersButton.setEnabled(false);

        // Fetch list of competitions from server
        fetchCompetitionsFromServer();

        // Handle "View Teams" button click
        fetchTeamsButton.setOnClickListener(v -> {
            if (competitionSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Competition list is still loading. Please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get selected competition name and ID
            String selectedName = competitionSpinner.getSelectedItem().toString();
            int selectedId = competitionIdMap.getOrDefault(selectedName, -1);

            // Start TeamActivity with selected competition ID
            Intent intent = new Intent(MainActivity.this, TeamActivity.class);
            intent.putExtra("competitionId", selectedId);
            startActivity(intent);
        });

        // Handle "View Fixtures" button click
        fetchFixturesButton.setOnClickListener(v -> {
            if (competitionSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Competition list is still loading. Please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedName = competitionSpinner.getSelectedItem().toString();
            int selectedId = competitionIdMap.getOrDefault(selectedName, -1);

            // Start FixturesActivity with selected competition ID
            Intent intent = new Intent(MainActivity.this, FixturesActivity.class);
            intent.putExtra("competitionId", selectedId);
            startActivity(intent);
        });

        // Handle "Top Scorers" button click
        viewTopScorersButton.setOnClickListener(v -> {
            if (competitionSpinner.getSelectedItem() == null) {
                Toast.makeText(this, "Competition list is still loading. Please wait...", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedName = competitionSpinner.getSelectedItem().toString();
            int selectedId = competitionIdMap.getOrDefault(selectedName, -1);

            // Start TopScorersActivity with selected competition ID
            Intent intent = new Intent(MainActivity.this, TopScorersActivity.class);
            intent.putExtra("competitionId", selectedId);
            startActivity(intent);
        });
    }

    /**
     * Fetches the list of football competitions from the backend server.
     * Populates the spinner and enables buttons once data is loaded.
     */
    private void fetchCompetitionsFromServer() {
        // API URL for fetching competitions
        String url = "https://miniature-space-zebra-jv4x5gvvppg2j6jj-8080.app.github.dev/fetchFootballData?type=list";

        // Create a GET request
        Request request = new Request.Builder().url(url).build();

        // Execute request asynchronously
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Log error and show toast if request fails
                Log.e("FETCH_COMP", "Request failed", e);
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to load competitions", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // Log and parse response
                    Log.d("FETCH_COMP", "Response code: " + response.code());
                    String body = response.body() != null ? response.body().string() : "";
                    Log.d("FETCH_COMP", "Response body: " + body);

                    // Convert response to JSON array
                    JSONArray competitions = new JSONArray(body);

                    // Temporary structures to hold names and IDs
                    ArrayList<String> names = new ArrayList<>();
                    HashMap<String, Integer> idMap = new HashMap<>();

                    // Extract name and ID of each competition
                    for (int i = 0; i < competitions.length(); i++) {
                        JSONObject comp = competitions.getJSONObject(i);
                        String name = comp.getString("name");
                        int id = comp.getInt("id");
                        names.add(name);
                        idMap.put(name, id);
                    }

                    // Update UI with data on the main thread
                    runOnUiThread(() -> {
                        competitionNames.clear();
                        competitionNames.addAll(names);

                        competitionIdMap.clear();
                        competitionIdMap.putAll(idMap);

                        // Populate the spinner with competition names
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, competitionNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        competitionSpinner.setAdapter(adapter);

                        // Enable buttons now that data is available
                        fetchFixturesButton.setEnabled(true);
                        fetchTeamsButton.setEnabled(true);
                        viewTopScorersButton.setEnabled(true);
                    });

                } catch (Exception e) {
                    // Handle parsing errors
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error parsing competition list", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}
