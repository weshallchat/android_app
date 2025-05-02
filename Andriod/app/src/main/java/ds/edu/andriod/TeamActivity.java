// Name: Vishal Chatterjee
// AndrewID: vchatter

package ds.edu.andriod;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class TeamActivity extends AppCompatActivity {
    // UI elements
    TextView teamText;
    Button viewfixturesButton;
    Button viewScorersButton;

    // OkHttp client for network requests
    OkHttpClient client = new OkHttpClient();

    // Competition ID received from previous activity
    int competitionId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team); // Set the layout for this activity

        // Bind UI elements to their respective views
        teamText = findViewById(R.id.teamText);
        viewfixturesButton = findViewById(R.id.viewfixturesButton);
        viewScorersButton = findViewById(R.id.viewScorersButton);

        // Retrieve competitionId from the intent (passed from previous activity)
        competitionId = getIntent().getIntExtra("competitionId", -1);

        // Fetch and display the list of teams
        fetchTeams();

        // Navigate to FixturesActivity with competitionId when "View Fixtures" is clicked
        viewfixturesButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeamActivity.this, FixturesActivity.class);
            intent.putExtra("competitionId", competitionId);
            startActivity(intent);
        });

        // Navigate to TopScorersActivity with competitionId when "View Scorers" is clicked
        viewScorersButton.setOnClickListener(v -> {
            Intent intent = new Intent(TeamActivity.this, TopScorersActivity.class);
            intent.putExtra("competitionId", competitionId);
            startActivity(intent);
        });
    }

    /**
     * Fetch the list of teams for the given competitionId from the servlet backend.
     * Parses the response and updates the TextView with formatted team details.
     */
    private void fetchTeams(){
        // Construct the backend API URL using the competitionId
        String url = "https://miniature-space-zebra-jv4x5gvvppg2j6jj-8080.app.github.dev/fetchFootballData?competitionId=" + competitionId + "&type=teams";

        // Create a GET request
        Request request = new Request.Builder().url(url).build();

        // Asynchronous HTTP call
        client.newCall(request).enqueue(new Callback(){
            @Override
            public void onFailure(Call call, IOException e){
                // Handle failure on UI thread
                runOnUiThread(() -> teamText.setText("Error fetching teams: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // Get response body as string
                    String body = response.body() != null ? response.body().string() : "";

                    // Convert response to JSON array
                    JSONArray teams = new JSONArray(body);

                    // Build a formatted string of team details
                    StringBuilder sb = new StringBuilder("Teams in Competition " + competitionId + ":\n\n");
                    for (int i = 0; i < teams.length(); i++) {
                        JSONObject team = teams.getJSONObject(i);

                        // Append each team’s name, stadium, and manager
                        sb.append("Name: ").append(team.optString("name", "N/A")).append("\n");
                        sb.append("Stadium: ").append(team.optString("stadium", "N/A")).append("\n");
                        sb.append("Manager: ").append(team.optString("manager", "N/A")).append("\n\n");
                    }

                    // Update UI with the result
                    String finalOutput = sb.toString();
                    runOnUiThread(() -> teamText.setText(finalOutput));
                } catch (Exception e) {
                    // Handle parsing errors or incorrect input
                    runOnUiThread(() -> teamText.setText("Please enter a correct competition ID."));
                }
            }
        });
    }
}
