package com.dbrighthd.texturesplusmod.pack;

import com.dbrighthd.texturesplusmod.client.pojo.LatestCommit;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import oshi.util.tuples.Pair;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.dbrighthd.texturesplusmod.TexturesPlusMod.LOGGER;

public class PackDownloader {

    private static final ExecutorService POOL = Executors.newVirtualThreadPerTaskExecutor();
    private static final Gson GSON = new Gson();

    public static void downloadFile(URL url, String fileName) throws Exception { // this is left alone, it's fine as is.
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void unzipFile(Path filePathToUnzip) {
        Path parentDir = filePathToUnzip.getParent();
        String fileName = filePathToUnzip.toFile().getName();
        Path targetDir = parentDir.resolve(FilenameUtils.removeExtension(fileName));

        //Open the file
        try (ZipFile zip = new ZipFile(filePathToUnzip.toFile())) {
            //We will unzip files in this folder
            if (!targetDir.toFile().isDirectory()
                    && !targetDir.toFile().mkdirs()) {
                throw new IOException("failed to create directory " + targetDir);
            }

            for (ZipEntry entry : Collections.list((zip.entries()))) {
                File f = new File(targetDir.resolve(Path.of(entry.getName())).toString());
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) throw new IOException("Failed to create directory: " + f);
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) throw new IOException("Failed to create parent directory: \n" + parent + "\nfor: \n" + f);
                    try (InputStream in = zip.getInputStream(entry)) {
                        Files.copy(in, f.toPath());
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static Pair<String, Boolean> downloadResourcePackSync(String pack) throws Exception {
        LatestCommit commit;
        String branch = pack.equals("creatures") ? "master" : "main";
        Path pathToPack = Paths.get("resourcepacks/" + pack + "plus/"); // only used for logging.
        try {
            URL url = URI.create("https://api.github.com/repos/dbrighthd/" + pack + "/commits/" + branch).toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            int status = con.getResponseCode();
            if (status != 200) {
                LOGGER.error("Failed to download pack {} from {}, status: {}", pack, url, status);
                throw new IOException("Status Code = " + status + " != 200");
            }

            byte[] bytes = con.getInputStream().readAllBytes();
            commit = GSON.fromJson(new String(bytes, StandardCharsets.UTF_8), LatestCommit.class);

            con.disconnect();
            File oldPackCommit = new File("resourcepacks/" + pack + "plus/commit_hash.txt");
            if (oldPackCommit.exists()) {
                if (oldPackCommit.length() > 0) {
                    BufferedReader brTest = new BufferedReader(new FileReader(oldPackCommit));
                    String currentHash = brTest.readLine();
                    brTest.close();
                    if (currentHash.equals(commit.sha())) {
                        LOGGER.info("{}+ is already up to date!", pack);
                        return new Pair<>(pathToPack.toAbsolutePath().toString(), false);
                    }
                } else {
                    LOGGER.warn("{}+ is missing its hash! Re-downloading...", pack);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        try {
            File tempFile = new File("resourcepacks/" + pack + "temp.zip");
            if (tempFile.exists()) {
                FileUtils.delete(tempFile);
            }
            File tempFile2 = new File("resourcepacks/" + pack + "temp");
            if (tempFile2.exists()) {
                FileUtils.deleteDirectory(tempFile2);
            }
            File oldPack = new File("resourcepacks/" + pack + "plus");
            if (oldPack.exists()) {
                FileUtils.deleteDirectory(oldPack);
            }
            LOGGER.info("Starting download for {}+...", pack);

            downloadFile(URI.create("https://github.com/dbrighthd/" + pack + "/archive/refs/heads/" + branch + ".zip/").toURL(), "resourcepacks/" + pack + "temp.zip");
            LOGGER.info("Starting extraction for {}+...", pack);
            unzipFile(Paths.get("resourcepacks/" + pack + "temp.zip"));
            Files.move(new File("resourcepacks/" + pack + "temp/" + pack + "-" + branch).toPath(), new File("resourcepacks/" + pack + "plus").toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileUtils.deleteDirectory(new File("resourcepacks/" + pack + "temp"));
            FileUtils.delete(new File("resourcepacks/" + pack + "temp.zip"));
            PrintWriter writer = new PrintWriter("resourcepacks/" + pack + "plus/" + "commit_hash.txt", StandardCharsets.UTF_8);
            writer.println(commit.sha());
            writer.close();
            LOGGER.info("Finished downloading and extracting {}+!", pack);
            return new Pair<>(pathToPack.toAbsolutePath().toString(), true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    public static CompletableFuture<Pair<String, Boolean>> downloadResourcePackAsync(String pack)
    {
        CompletableFuture<Pair<String, Boolean>> future = new CompletableFuture<>();
        POOL.submit(() -> {
            try {
                future.complete(downloadResourcePackSync(pack));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<Pair<String, Boolean>> downloadResourcePack(String pack, boolean sync) {
        CompletableFuture<Pair<String, Boolean>> future;
        if (sync) {
            future = new CompletableFuture<>();
            try {
                future.complete(downloadResourcePackSync(pack));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        } else {
            future = downloadResourcePackAsync(pack);
        }
        return future;
    }

    private static boolean didElytrasUpdate = false;
    private static boolean didPumpkinsUpdate = false;
    private static boolean didWeaponsUpdate = false;
    private static boolean didCreaturesUpdate = false;

    public static boolean didAnyUpdate() {
        return didElytrasUpdate || didWeaponsUpdate || didPumpkinsUpdate || didCreaturesUpdate;
    }

    public static CompletableFuture<Void> downloadAllPacks(boolean async) {
        CompletableFuture<Pair<String, Boolean>> elytras = downloadResourcePack("elytras", !async);
        CompletableFuture<Pair<String, Boolean>> pumpkins = downloadResourcePack("pumpkins", !async);
        CompletableFuture<Pair<String, Boolean>> weapons = downloadResourcePack("weapons", !async);
        CompletableFuture<Pair<String, Boolean>> creatures = downloadResourcePack("creatures", !async);
        elytras.whenComplete((s, e) ->
            didElytrasUpdate = s.getB()
        );
        pumpkins.whenComplete((s, e) ->
            didPumpkinsUpdate = s.getB()
        );
        weapons.whenComplete((s, e) ->
            didWeaponsUpdate = s.getB()
        );
        creatures.whenComplete((s, e) ->
            didCreaturesUpdate = s.getB()
        );
        return CompletableFuture.allOf(elytras, pumpkins, weapons, creatures);
    }

    @SuppressWarnings("unused")
    public static String getTexturesPlusPackId(String pack) {
        return getResourcePackIdFromPath(getPathFromTexturesPlusName(pack));
    }

    public static Path getPathFromTexturesPlusName(String name) {
        return Paths.get("resourcepacks/" + name + "plus/");
    }

    public static String getResourcePackIdFromPath(Path path) {
        return "file/" + path.getFileName().toString();
    }
}
