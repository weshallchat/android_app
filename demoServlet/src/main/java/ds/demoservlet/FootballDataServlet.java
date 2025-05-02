// Name: Vishal Chatterjee
// AndrewID: vchatter

package ds.demoservlet;

import java.io.*;
import java.net.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

@WebServlet("/fetchFootballData") // Maps this servlet to handle /fetchFootballData requests
public class FootballDataServlet extends HttpServlet {

    // Base URL and API key for Football-Data.org
    private static final String FOOTBALL_API_URL = "https://api.football-data.org/v4/";
    private static final String API_KEY = "b58e0c6465154f76858a820969274490";

    // MongoDB URI for logging request analytics
    private static final String MONGO_URI = "mongodb+srv://vchatter:vchatter12345@cluster-task4.tqhcj1k.mongodb.net/?retryWrites=true&w=majority&appName=Cluster-task4";

    /**
     * Handles GET requests for fetching football data and responding with JSON.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String dataType = request.getParameter("type");

        // If the user just wants a list of top competitions
        if ("list".equalsIgnoreCase(dataType)) {
            JSONArray competitions = getTopCompetitions();
            response.getWriter().write(competitions.toString());
            return;
        }

        // Parse competition ID either directly or by name
        int compId = -1;
        String compIdParam = request.getParameter("competitionId");
        String competitionQuery = request.getParameter("competition");

        if (compIdParam != null) {
            try {
                compId = Integer.parseInt(compIdParam);
            } catch (NumberFormatException ignored) {
                compId = -1;
            }
        } else if (competitionQuery != null && !competitionQuery.isEmpty()) {
            compId = getCompID(competitionQuery);
        }

        // Validate required inputs
        if (compId == -1 || dataType == null || dataType.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing or invalid 'competitionId' or 'type' parameter\"}");
            return;
        }

        // Based on dataType, call the appropriate method
        JSONArray result = null;
        switch (dataType.toLowerCase()) {
            case "matches":
                result = getMatches(compId);
                break;
            case "teams":
                result = getTeams(compId);
                break;
            case "topscorers":
                result = getTopScorer(compId);
                break;
            default:
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid type parameter. Use 'matches', 'teams', or 'topscorers'\"}");
                return;
        }

        // Handle response output
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to fetch data from Football-Data API\"}");
        } else {
            response.getWriter().write(result.toString());
            String compName = getCompetitionName(compId); // Get full name for logs
            logToMongoDB(compName, dataType, compId, result); // Store request analytics in MongoDB
        }
    }

    /**
     * Looks up a competition ID by name (used when ID isn't directly passed).
     */
    private int getCompID(String compName) {
        String urlString = FOOTBALL_API_URL + "competitions";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(urlString).openConnection().getInputStream()))) {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray competitions = new JSONObject(jsonBuilder.toString()).getJSONArray("competitions");
            for (int i = 0; i < competitions.length(); i++) {
                JSONObject comp = competitions.getJSONObject(i);
                if (comp.optString("name", "").toLowerCase().contains(compName.toLowerCase().trim())) {
                    return comp.optInt("id");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Fetches a short list of 5 top football competitions.
     */
    private JSONArray getTopCompetitions() {
        int[] topIds = {2021, 2002, 2014, 2019, 2001}; // EPL, Bundesliga, La Liga, Serie A, Ligue 1
        JSONArray top5 = new JSONArray();
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(FOOTBALL_API_URL + "competitions").openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONArray comps = new JSONObject(jsonBuilder.toString()).getJSONArray("competitions");
            for (int id : topIds) {
                for (int i = 0; i < comps.length(); i++) {
                    JSONObject comp = comps.getJSONObject(i);
                    if (comp.getInt("id") == id) {
                        JSONObject info = new JSONObject();
                        info.put("id", comp.getInt("id"));
                        info.put("name", comp.getString("name"));
                        top5.put(info);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return top5;
    }

    /**
     * Fetches the next 5 scheduled matches for a competition.
     */
    private JSONArray getMatches(int compId) {
        String url = FOOTBALL_API_URL + "competitions/" + compId + "/matches?status=SCHEDULED&limit=5";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line);

            JSONArray matches = new JSONObject(jsonBuilder.toString()).getJSONArray("matches");
            JSONArray result = new JSONArray();

            for (int i = 0; i < matches.length(); i++) {
                JSONObject match = matches.getJSONObject(i);
                JSONObject matchObj = new JSONObject();
                matchObj.put("date", match.optString("utcDate"));
                matchObj.put("homeTeam", match.getJSONObject("homeTeam").optString("name"));
                matchObj.put("awayTeam", match.getJSONObject("awayTeam").optString("name"));
                result.put(matchObj);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches all teams in a competition.
     */
    private JSONArray getTeams(int compId) {
        String url = FOOTBALL_API_URL + "competitions/" + compId + "/teams";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line);

            JSONArray teams = new JSONObject(jsonBuilder.toString()).getJSONArray("teams");
            JSONArray result = new JSONArray();

            for (int i = 0; i < teams.length(); i++) {
                JSONObject team = teams.getJSONObject(i);
                JSONObject teamObj = new JSONObject();
                teamObj.put("name", team.optString("name"));
                teamObj.put("stadium", team.optString("venue"));
                teamObj.put("manager", team.getJSONObject("coach").optString("name"));
                result.put(teamObj);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches top scorers from a competition.
     */
    private JSONArray getTopScorer(int compId) {
        String url = FOOTBALL_API_URL + "competitions/" + compId + "/scorers";
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line);

            JSONArray scorers = new JSONObject(jsonBuilder.toString()).getJSONArray("scorers");
            JSONArray result = new JSONArray();

            for (int i = 0; i < scorers.length(); i++) {
                JSONObject scorer = scorers.getJSONObject(i);
                JSONObject player = scorer.getJSONObject("player");

                JSONObject playerObj = new JSONObject();
                playerObj.put("name", player.optString("name"));
                playerObj.put("goals", scorer.optInt("goals"));
                playerObj.put("matches", scorer.optInt("playedMatches", 0));
                result.put(playerObj);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches the name of the competition by its ID (used for MongoDB logs).
     */
    private String getCompetitionName(int compId) {
        String url = FOOTBALL_API_URL + "competitions/" + compId;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-Auth-Token", API_KEY);
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonBuilder.append(line);

            JSONObject obj = new JSONObject(jsonBuilder.toString());
            return obj.optString("name", "unknown");

        } catch (Exception e) {
            e.printStackTrace();
            return "unknown";
        }
    }

    /**
     * Logs query metadata to MongoDB for dashboard analytics.
     */
    private void logToMongoDB(String competitionQuery, String dataType, int compId, JSONArray result) {
        try (MongoClient mongoClient = MongoClients.create(MONGO_URI)) {
            MongoDatabase db = mongoClient.getDatabase("football");
            MongoCollection<Document> logs = db.getCollection("logs");

            Document log = new Document("competition", competitionQuery)
                    .append("type", dataType)
                    .append("competitionId", compId)
                    .append("resultCount", result != null ? result.length() : 0)
                    .append("timestamp", System.currentTimeMillis());

            logs.insertOne(log);
        } catch (Exception e) {
            System.out.println("MongoDB logging failed: " + e.getMessage());
        }
    }
}
