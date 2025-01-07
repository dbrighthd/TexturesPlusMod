package com.dbrighthd.texturesplusmod;

import com.dbrighthd.texturesplusmod.client.TexturesPlusModClient;
import com.dbrighthd.texturesplusmod.client.pojo.LatestCommit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PackGetterUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger("texturesplusmod");
    private static final ExecutorService POOL = Executors.newVirtualThreadPerTaskExecutor();

    public static void downloadFile(URL url, String fileName) throws Exception { // this is left alone, it's fine as is.
        try (InputStream in = url.openStream()) {
            Files.copy(in, Paths.get(fileName));
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

    public static String downloadResourcePackSync(String pack) throws Exception {
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
                throw new HttpException("Status Code = " + status + " != 200");
            }

            byte[] bytes = con.getInputStream().readAllBytes();
            commit = new ObjectMapper().readValue(bytes, LatestCommit.class);

            con.disconnect();
            File oldPackCommit = new File("resourcepacks/" + pack + "plus/commit_hash.txt");
            if (oldPackCommit.exists()) {
                if (oldPackCommit.length() > 0) {
                    BufferedReader brTest = new BufferedReader(new FileReader(oldPackCommit));
                    String currentHash = brTest.readLine();
                    brTest.close();
                    if (currentHash.equals(commit.getSha())) {
                        LOGGER.info("{}+ is already up to date!", pack);
                        return pathToPack.toAbsolutePath().toString();
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
            writer.println(commit.getSha());
            writer.close();
            LOGGER.info("Finished downloading and extracting {}+!", pack);
            return pathToPack.toAbsolutePath().toString();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    public static CompletableFuture<String> downloadResourcePackAsync(String pack)
    {
        CompletableFuture<String> future = new CompletableFuture<>();
        POOL.submit(() -> {
            try {
                future.complete(downloadResourcePackSync(pack));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static CompletableFuture<String> downloadResourcePack(String pack, boolean sync) {
        CompletableFuture<String> future;
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

    public static CompletableFuture<Void> downloadAllPacks()
    {
        boolean async = TexturesPlusModClient.getConfig().async;
        return CompletableFuture.allOf(downloadResourcePack("elytras", !async), downloadResourcePack("pumpkins", !async), downloadResourcePack("weapons", !async), downloadResourcePack("creatures", !async));
    }
}
