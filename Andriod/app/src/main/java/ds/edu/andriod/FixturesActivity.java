// Name: Vishal Chatterjee
// AndrewID: vchatter

package ds.edu.andriod;

import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class FixturesActivity extends AppCompatActivity {

    // HTTP client to perform API requests
    OkHttpClient connect = new OkHttpClient();

    // TableLayout to display match fixtures
    TableLayout tableLayout;

    // Competition ID passed from MainActivity
    int competitionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixtures); // Set layout for this activity

        // Initialize UI components
        tableLayout = findViewById(R.id.fixturesTable);

        // Get the competition ID from the intent
        competitionId = getIntent().getIntExtra("competitionId", -1);

        // Fetch and display fixture data
        fetchTable();
    }

    /**
     * Fetches fixture data from the backend server and populates the TableLayout.
     */
    private void fetchTable() {
        // Construct the API URL with competition ID and type=matches
        String url = "https://miniature-space-zebra-jv4x5gvvppg2j6jj-8080.app.github.dev/fetchFootballData?competitionId=" + competitionId + "&type=matches";

        Request request = new Request.Builder().url(url).build();

        // Asynchronous HTTP call using OkHttp
        connect.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure by showing an error message in the table
                runOnUiThread(() -> {
                    TableRow errorRow = new TableRow(FixturesActivity.this);
                    TextView errorText = new TextView(FixturesActivity.this);
                    errorText.setText("Error fetching fixtures: " + e.getMessage());
                    errorRow.addView(errorText);
                    tableLayout.addView(errorRow);
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    // Read response body as a string
                    String body = response.body() != null ? response.body().string() : "";

                    // Parse JSON array of fixtures
                    JSONArray fixtures = new JSONArray(body);

                    runOnUiThread(() -> {
                        // Add header row to the table
                        addHeaderRow();

                        // Loop through each fixture and add it to the table
                        for (int i = 0; i < fixtures.length(); i++) {
                            JSONObject fixture;
                            try {
                                fixture = fixtures.getJSONObject(i);
                            } catch (JSONException e) {
                                throw new RuntimeException(e); // Should be handled more gracefully in production
                            }

                            // Extract date and team names
                            String date = fixture.optString("date", "N/A").substring(0, 10); // Extract YYYY-MM-DD
                            String home = fixture.optString("homeTeam", "N/A");
                            String away = fixture.optString("awayTeam", "N/A");

                            // Create a new table row with fixture info
                            TableRow row = new TableRow(FixturesActivity.this);
                            row.addView(createCell(date));
                            row.addView(createCell(home));
                            row.addView(createCell(away));

                            tableLayout.addView(row);
                        }
                    });

                } catch (Exception e) {
                    // Handle JSON parsing or UI update errors
                    runOnUiThread(() -> {
                        TableRow errorRow = new TableRow(FixturesActivity.this);
                        TextView errorText = new TextView(FixturesActivity.this);
                        errorText.setText("Error parsing fixtures: " + e.getMessage());
                        errorRow.addView(errorText);
                        tableLayout.addView(errorRow);
                    });
                }
            }
        });
    }

    /**
     * Adds a bold header row to the fixtures table with "Date", "Home Team", "Away Team".
     */
    private void addHeaderRow() {
        TableRow header = new TableRow(this);
        header.addView(createCell("Date", true));
        header.addView(createCell("Home Team", true));
        header.addView(createCell("Away Team", true));
        tableLayout.addView(header);
    }

    /**
     * Creates a TextView cell for a table row with optional header styling.
     *
     * @param text      Text to display
     * @param isHeader  Whether the cell is part of the header
     * @return A styled TextView to add to a TableRow
     */
    private TextView createCell(String text, boolean isHeader) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(16, 8, 16, 8); // Add padding for better spacing
        tv.setTextSize(isHeader ? 16 : 14); // Slightly larger font for headers
        if (isHeader) tv.setTypeface(null, android.graphics.Typeface.BOLD); // Make header text bold
        return tv;
    }

    // Overloaded method for non-header cells
    private TextView createCell(String text) {
        return createCell(text, false);
    }
}
