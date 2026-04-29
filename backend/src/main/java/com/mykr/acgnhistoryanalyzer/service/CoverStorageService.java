package com.mykr.acgnhistoryanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class CoverStorageService {

    public static final String COVER_ASSET_PREFIX = "/api/assets/covers/";

    private static final long MAX_COVER_BYTES = 8L * 1024L * 1024L;

    private final Path coverRoot;
    private final HttpClient httpClient;

    public CoverStorageService(
            @Value("${app.assets.cover-dir:uploads/covers}") String coverDir
    ) {
        this.coverRoot = Path.of(coverDir).toAbsolutePath().normalize();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public String cacheCover(String coverUrl, String sourcePlatform,
                             String sourceId, String displayTitle) {
        if (!hasText(coverUrl)) {
            return null;
        }

        String normalizedUrl = coverUrl.trim();
        if (normalizedUrl.startsWith(COVER_ASSET_PREFIX) || !isRemoteHttpUrl(normalizedUrl)) {
            return normalizedUrl;
        }

        try {
            HttpResponse<byte[]> response = downloadCover(normalizedUrl);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return normalizedUrl;
            }

            byte[] body = response.body();
            if (body == null || body.length == 0 || body.length > MAX_COVER_BYTES) {
                return normalizedUrl;
            }

            Files.createDirectories(coverRoot);

            String contentType = response.headers()
                    .firstValue("content-type")
                    .orElse(null);
            String extension = inferExtension(normalizedUrl, contentType);
            String fileName = buildFileName(normalizedUrl, sourcePlatform, sourceId, displayTitle, extension);
            Path target = coverRoot.resolve(fileName).normalize();

            if (!target.startsWith(coverRoot)) {
                return normalizedUrl;
            }

            if (!Files.exists(target)) {
                Path temporaryFile = coverRoot.resolve(fileName + ".download");
                Files.write(temporaryFile, body);
                moveDownloadedFile(temporaryFile, target);
            }

            return COVER_ASSET_PREFIX + fileName;
        } catch (Exception ex) {
            return normalizedUrl;
        }
    }

    private void moveDownloadedFile(Path temporaryFile, Path target) throws IOException {
        try {
            Files.move(
                    temporaryFile,
                    target,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Path getCoverRoot() {
        return coverRoot;
    }

    private HttpResponse<byte[]> downloadCover(String coverUrl)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(coverUrl))
                .GET()
                .timeout(Duration.ofSeconds(20))
                .header("User-Agent", "ACGN-History-Analyzer/1.0")
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }

    private boolean isRemoteHttpUrl(String value) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private String buildFileName(String coverUrl, String sourcePlatform,
                                 String sourceId, String displayTitle,
                                 String extension) {
        String key;
        if (hasText(sourceId)) {
            String platform = hasText(sourcePlatform) ? sourcePlatform : "source";
            key = sanitizeFilePart(platform.toLowerCase(Locale.ROOT))
                    + "-"
                    + sanitizeFilePart(sourceId);
        } else if (hasText(displayTitle)) {
            key = sanitizeFilePart(displayTitle) + "-" + hashShort(coverUrl);
        } else {
            key = "cover-" + hashShort(coverUrl);
        }

        if (!hasText(key) || "-".equals(key)) {
            key = "cover-" + hashShort(coverUrl);
        }

        return key + "." + extension;
    }

    private String sanitizeFilePart(String value) {
        if (!hasText(value)) {
            return "";
        }

        String sanitized = value.trim()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5._-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (sanitized.length() > 80) {
            return sanitized.substring(0, 80);
        }

        return sanitized;
    }

    private String inferExtension(String coverUrl, String contentType) {
        if (contentType != null) {
            String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
            if (normalizedContentType.contains("image/png")) {
                return "png";
            }
            if (normalizedContentType.contains("image/webp")) {
                return "webp";
            }
            if (normalizedContentType.contains("image/gif")) {
                return "gif";
            }
            if (normalizedContentType.contains("image/jpeg") || normalizedContentType.contains("image/jpg")) {
                return "jpg";
            }
        }

        String path = URI.create(coverUrl).getPath();
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < path.length() - 1) {
            String extension = path.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
            if (extension.equals("jpg")
                    || extension.equals("jpeg")
                    || extension.equals("png")
                    || extension.equals("webp")
                    || extension.equals("gif")) {
                return extension.equals("jpeg") ? "jpg" : extension;
            }
        }

        return "jpg";
    }

    private String hashShort(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException ex) {
            return String.valueOf(Math.abs(value.hashCode()));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
