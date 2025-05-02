import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;

public class FootballDataFetcher {

    // Replace with your actual API key from football-data.org
    private static final String API_KEY = "09805a93662345b7b629d815eeb84ac4";
    private static final String BASE_URL = "https://api.football-data.org/v4/";

    public static void main(String[] args) {
        try {
            // Fetch list of competitions
            String competitionsData = fetchData("competitions");

            // Parse JSON response
            Gson gson = new Gson();
            CompetitionsResponse response = gson.fromJson(competitionsData, CompetitionsResponse.class);
            List<Competition> competitions = response.getCompetitions();

            // Extract and print competition information
            System.out.println("Available Football Competitions:");
            System.out.println("--------------------------------");

            for (Competition competition : competitions) {
                String name = competition.getName();
                String code = competition.getCode();
                String area = competition.getArea().getName();

                System.out.println(name + " (" + code + ") - " + area);
            }

        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String fetchData(String endpoint) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set request method
        connection.setRequestMethod("GET");

        // Set headers
        connection.setRequestProperty("X-Auth-Token", API_KEY);
        connection.setRequestProperty("Accept", "application/json");

        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("HTTP Error: " + responseCode);
        }

        // Read response
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }
}