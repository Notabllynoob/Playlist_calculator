import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaylistAnalyzer {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Please provide a YouTube playlist URL as an argument.");
            System.err.println("Usage: java PlaylistAnalyzer <URL>");
            return;
        }
        String playlistUrl = args[0];

        try {
           
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(playlistUrl))
                    .header("User-Agent", "Mozilla/5.0") // Act as a browser
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String pageSource = response.body();

            
            Pattern videoPattern = Pattern.compile("watch\\?v=");
            Matcher videoMatcher = videoPattern.matcher(pageSource);
            long videoCount = videoMatcher.results().count();

            if (videoCount == 0) {
                System.out.println("No videos found in the playlist, or the playlist is private/invalid.");
                return;
            }

           
            Pattern durationPattern = Pattern.compile("\"label\":\"(\\d+)\\s+(hour|minute|second)s?,?\\s*(\\d*)\\s*(minute|second)?s?,?\\s*(\\d*)\\s*(second)?s?\"");
            Matcher durationMatcher = durationPattern.matcher(pageSource);

            long totalSeconds = 0;
            while (durationMatcher.find()) {
                totalSeconds += parseDurationToSeconds(durationMatcher);
            }

            
            displayResults(videoCount, totalSeconds);

        } catch (IOException | InterruptedException e) {
            System.err.println("An error occurred while trying to fetch the playlist data.");
            e.printStackTrace();
        }
    }

    
     
     
    private static long parseDurationToSeconds(Matcher matcher) {
        long seconds = 0;
        try {
            
            long val1 = Long.parseLong(matcher.group(1));
            String unit1 = matcher.group(2);

            if (unit1.startsWith("hour")) seconds += TimeUnit.HOURS.toSeconds(val1);
            if (unit1.startsWith("minute")) seconds += TimeUnit.MINUTES.toSeconds(val1);
            if (unit1.startsWith("second")) seconds += val1;

            
            if (matcher.group(3) != null && !matcher.group(3).isEmpty()) {
                long val2 = Long.parseLong(matcher.group(3));
                String unit2 = matcher.group(4);
                if (unit2.startsWith("minute")) seconds += TimeUnit.MINUTES.toSeconds(val2);
                if (unit2.startsWith("second")) seconds += val2;
            }
             
            if (matcher.group(5) != null && !matcher.group(5).isEmpty()) {
                 long val3 = Long.parseLong(matcher.group(5));
                 
                 seconds += val3;
            }
        } catch (NumberFormatException e) {
            
        }
        return seconds;
    }

  
    private static String formatDuration(long totalSeconds) {
        long days = TimeUnit.SECONDS.toDays(totalSeconds);
        long hours = TimeUnit.SECONDS.toHours(totalSeconds) % 24;
        long minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60;
        long seconds = totalSeconds % 60;

        if (days > 0) {
            return String.format("%d days, %02d:%02d:%02d", days, hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
    }
    
   
    private static void displayResults(long videoCount, long totalSeconds) {
        System.out.println("\n");
        System.out.println("      Playlist Analysis Results");
        System.out.println("");
        System.out.printf("Number of videos: %d%n", videoCount);
        System.out.printf("Total playlist length: %s%n", formatDuration(totalSeconds));
        
        if (videoCount > 0) {
            long avgSeconds = totalSeconds / videoCount;
            System.out.printf("Average video length: %s%n", formatDuration(avgSeconds));
        }

        System.out.println("\n Playlist Length at Different Speeds ");
        System.out.printf("1.25x Speed: %s%n", formatDuration((long)(totalSeconds / 1.25)));
        System.out.printf("1.50x Speed: %s%n", formatDuration((long)(totalSeconds / 1.50)));
        System.out.printf("1.75x Speed: %s%n", formatDuration((long)(totalSeconds / 1.75)));
        System.out.printf("2.00x Speed: %s%n", formatDuration((long)(totalSeconds / 2.00)));
        System.out.println("");
    }
}
