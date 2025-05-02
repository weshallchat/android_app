// Name: Vishal Chatterjee
// AndrewID: vchatter


package ds.edu.andriod;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class TopScorersActivity extends AppCompatActivity {

    // OkHttpClient instance to perform HTTP requests
    OkHttpClient connect = new OkHttpClient();

    // Layout to display the top scorers table
    TableLayout tableLayout;

    // Competition ID passed from MainActivity
    int competitionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_scorers); // Set the layout for top scorers view

        // Initialize table layout view
        tableLayout = findViewById(R.id.topScorersTable);

        // Get competitionId from the Intent extras
        competitionId = getIntent().getIntExtra("competitionId", -1);

        // Fetch and display top scorers data
        fetchScorers();
    }

    /**
     * Fetches top scorer data from the backend and populates the table layout.
     */
    private void fetchScorers() {
        // Construct API endpoint for top scorers
        String url = "https://miniature-space-zebra-jv4x5gvvppg2j6jj-8080.app.github.dev/fetchFootballData?competitionId=" + competitionId + "&type=topscorers";

        // Prepare HTTP request
        Request request = new Request.Builder().url(url).build();

        // Make asynchronous call
        connect.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                // If request fails, show error message in the UI
                runOnUiThread(() -> addErrorRow("Error fetching top scorers: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    // Read and parse the JSON response
                    String body = response.body() != null ? response.body().string() : "";
                    JSONArray players = new JSONArray(body);

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        addHeaderRow(); // Add table headers

                        // Iterate through each player in the JSON array
                        for (int i = 0; i < players.length(); i++) {
                            JSONObject player;
                            try {
                                player = players.getJSONObject(i);
                            } catch (JSONException e) {
                                throw new RuntimeException(e); // Should ideally be handled more gracefully
                            }

                            // Extract player information
                            String name = player.optString("name", "N/A");
                            int matches = player.optInt("matches", 0);
                            int goals = player.optInt("goals", 0);

                            // Add player row to the table
                            addPlayerRow(name, matches, goals);
                        }
                    });

                } catch (Exception e) {
                    // Handle JSON parsing or data extraction errors
                    runOnUiThread(() -> addErrorRow("Not able to get TopScorers data: " + e.getMessage()));
                }
            }
        });
    }

    /**
     * Adds the header row to the table with column names.
     */
    private void addHeaderRow() {
        TableRow row = new TableRow(this);
        String[] headers = {"Name", "Matches", "Goals"};

        for (String header : headers) {
            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setPadding(8, 8, 8, 8);
            row.addView(tv); // Add header cell to the row
        }

        tableLayout.addView(row); // Add header row to the table
    }

    /**
     * Adds a row to the table for a single player.
     *
     * @param name    Player's name
     * @param matches Number of matches played
     * @param goals   Number of goals scored
     */
    private void addPlayerRow(String name, int matches, int goals) {
        TableRow row = new TableRow(this);
        String[] data = {name, String.valueOf(matches), String.valueOf(goals)};

        for (String value : data) {
            TextView tv = new TextView(this);
            tv.setText(value);
            tv.setPadding(8, 8, 8, 8);
            row.addView(tv); // Add data cell to the row
        }

        tableLayout.addView(row); // Add player row to the table
    }

    /**
     * Displays an error message as a row in the table layout.
     *
     * @param errorMsg The error message to be shown
     */
    private void addErrorRow(String errorMsg) {
        TableRow row = new TableRow(this);
        TextView tv = new TextView(this);
        tv.setText(errorMsg);
        row.addView(tv); // Add error cell to the row
        tableLayout.addView(row); // Add error row to the table
    }
}
