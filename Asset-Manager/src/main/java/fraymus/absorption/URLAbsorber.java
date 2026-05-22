package fraymus.absorption;

import fraymus.knowledge.AkashicRecord;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL ABSORBER: THE WEB EATER
 * 
 * "The Internet is just a database waiting to be ingested."
 * 
 * Mechanism:
 * 1. FETCH: Downloads raw HTML stream.
 * 2. STRIP: Removes tags, scripts, and noise.
 * 3. DISTILL: Extracts 'Core Concepts' (Headers) and 'Facts' (Paragraphs).
 * 4. INTEGRATE: Feeds the Akashic Record.
 */
public class URLAbsorber {

    private AkashicRecord akashic;

    public URLAbsorber(AkashicRecord record) {
        this.akashic = record;
        System.out.println("🌐 URL ABSORBER ONLINE. WAITING FOR TARGETS.");
    }

    /**
     * ABSORB: The Ingestion Protocol
     */
    public void absorb(String targetUrl) {
        System.out.println("   >> TARGET LOCKED: " + targetUrl);
        System.out.println("   >> INITIATING DATA SIPHON...");

        try {
            // 1. FETCH RAW DATA
            String rawHtml = fetchHtml(targetUrl);
            
            // 2. THE DIGESTION (Parsing)
            PageContent content = digest(rawHtml);
            
            // 3. THE INTEGRATION (Storage)
            System.out.println("   >> ABSORBING KNOWLEDGE...");
            
            // Store the Title as a High-Level Concept
            akashic.addBlock("CONCEPT", content.title);
            
            // Store Headers as Sub-Concepts
            for (String header : content.headers) {
                akashic.addBlock("SUB_CONCEPT", header);
            }
            
            // Store Paragraphs as Raw Facts
            int factCount = 0;
            for (String paragraph : content.paragraphs) {
                // Only store substantial facts
                if (paragraph.length() > 50) {
                    akashic.addBlock("FACT", paragraph);
                    factCount++;
                }
            }
            
            System.out.println("   ✓ ABSORPTION COMPLETE.");
            System.out.println("   ✓ NEW CONCEPTS: " + content.headers.size());
            System.out.println("   ✓ NEW FACTS: " + factCount);

        } catch (Exception e) {
            System.out.println("   !! CONNECTION FAILED: " + e.getMessage());
        }
    }

    // --- INTERNAL DIGESTION ENGINES ---

    private String fetchHtml(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Fraymus/1.0 (Eyeoverthink Bot)");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        return content.toString();
    }

    private PageContent digest(String html) {
        PageContent page = new PageContent();

        // 0. PRE-CLEAN: Remove scripts, styles, and navigation before parsing
        html = preClean(html);

        // 1. EXTRACT TITLE (The Soul of the Page)
        Matcher titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE).matcher(html);
        if (titleMatcher.find()) page.title = clean(titleMatcher.group(1));

        // 2. EXTRACT HEADERS (The Skeleton)
        Matcher headerMatcher = Pattern.compile("<h[1-3][^>]*>(.*?)</h[1-3]>", Pattern.CASE_INSENSITIVE).matcher(html);
        while (headerMatcher.find()) {
            String header = clean(headerMatcher.group(1));
            if (header.length() > 5 && header.length() < 200) { // Filter noise
                page.headers.add(header);
            }
        }

        // 3. EXTRACT PARAGRAPHS (The Meat)
        Matcher pMatcher = Pattern.compile("<p[^>]*>(.*?)</p>", Pattern.CASE_INSENSITIVE).matcher(html);
        while (pMatcher.find()) {
            String rawText = clean(pMatcher.group(1));
            // Filter: substantial length, not navigation, not empty
            if (rawText.length() > 30 && rawText.length() < 1000 &&
                !rawText.contains("Skip to") &&
                !rawText.contains("Menu") &&
                !rawText.contains("Contact") &&
                !rawText.contains("Login") &&
                !rawText.contains("Sign up")) {
                page.paragraphs.add(rawText);
            }
        }

        // 4. EXTRACT LISTS (Additional facts)
        Matcher liMatcher = Pattern.compile("<li[^>]*>(.*?)</li>", Pattern.CASE_INSENSITIVE).matcher(html);
        while (liMatcher.find()) {
            String item = clean(liMatcher.group(1));
            if (item.length() > 20 && item.length() < 500) {
                page.paragraphs.add(item);
            }
        }

        return page;
    }

    // PRE-CLEAN: Remove noise before parsing
    private String preClean(String html) {
        // Remove script tags and content
        html = html.replaceAll("(?s)<script[^>]*>.*?</script>", "");
        // Remove style tags and content
        html = html.replaceAll("(?s)<style[^>]*>.*?</style>", "");
        // Remove navigation elements
        html = html.replaceAll("(?s)<nav[^>]*>.*?</nav>", "");
        html = html.replaceAll("(?s)<header[^>]*>.*?</header>", "");
        html = html.replaceAll("(?s)<footer[^>]*>.*?</footer>", "");
        // Remove inline styles
        html = html.replaceAll("style=\"[^\"]*\"", "");
        html = html.replaceAll("class=\"[^\"]*\"", "");
        // Remove large JSON blobs (common in modern web apps)
        html = html.replaceAll("\\{[\"']?[a-zA-Z0-9_]+[\"']?\\s*:\\s*\\[", "");
        html = html.replaceAll("\\]\\s*,\\s*\\{[\"']?[a-zA-Z0-9_]+[\"']?\\s*:", "");
        // Remove data attributes
        html = html.replaceAll("data-[a-z-]+=\"[^\"]*\"", "");
        return html;
    }

    // SANITIZER: Removes HTML tags and noise
    private String clean(String dirty) {
        return dirty.replaceAll("<[^>]*>", "") // Remove tags
                    .replaceAll("&nbsp;", " ") // Replace non-breaking spaces
                    .replaceAll("&amp;", "&") // Replace HTML entities
                    .replaceAll("&lt;", "<")
                    .replaceAll("&gt;", ">")
                    .replaceAll("&quot;", "\"")
                    .replaceAll("&apos;", "'")
                    .replaceAll("&[^;]+;", " ") // Remove other HTML entities
                    .replaceAll("\\s+", " ") // Normalize whitespace
                    .replaceAll("\\[.*?\\]", "") // Remove bracketed content (often UI text)
                    .replaceAll("\\{.*?\\}", "") // Remove curly brace content (often JSON)
                    .trim();
    }

    // DATA STRUCTURE
    private static class PageContent {
        String title = "Unknown";
        List<String> headers = new ArrayList<>();
        List<String> paragraphs = new ArrayList<>();
    }
}
