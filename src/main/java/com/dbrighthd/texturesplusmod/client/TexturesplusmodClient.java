package com.dbrighthd.texturesplusmod.client;

import com.dbrighthd.texturesplusmod.client.pojo.LatestCommit;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fabricmc.api.ClientModInitializer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.zip.*;
import java.io.*;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class TexturesplusmodClient implements ClientModInitializer {


    public static final Logger LOGGER = LoggerFactory.getLogger("texturesplusmod");
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
            e.printStackTrace();
        }
    }
    public static void downloadResourcePack(String pack)
    {
        String hash = null;
        String mainOrMaster = "main";
        if (pack.equals("creatures"))
            mainOrMaster = "master";
        try {
            URL url = new URL("https://api.github.com/repos/dbrighthd/" + pack + "/commits/" + mainOrMaster);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            String json = content.toString();
            JsonNode node = new ObjectMapper().readTree(json);

            if (node.has("sha")) {
                String sha = node.get("sha").toString();
                hash = sha.substring(1, 7);
            }
            in.close();
            con.disconnect();
            File oldPackCommit = new File("resourcepacks/" + pack + "plus/commit_hash.txt");
            if (oldPackCommit.exists())
            {
                BufferedReader brTest = new BufferedReader(new FileReader(oldPackCommit));
                String currentHash = brTest .readLine();
                brTest.close();
                if (currentHash.equals(hash))
                {
                    LOGGER.info(pack + "+ is already up to date!");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File tempFile = new File("resourcepacks/" + pack + "temp.zip");
            if (tempFile.exists())
            {
                FileUtils.delete(tempFile);
            }
            File tempFile2 = new File("resourcepacks/" + pack + "temp");
            if (tempFile2.exists())
            {
                FileUtils.deleteDirectory(tempFile2);
            }
            File oldPack = new File("resourcepacks/" + pack + "plus");
            if (oldPack.exists())
            {
                FileUtils.deleteDirectory(oldPack);
            }
            LOGGER.info("Starting download for " + pack + "+...");


            downloadFile(new URL("https://github.com/dbrighthd/" + pack + "/archive/refs/heads/" + mainOrMaster + ".zip/"), "resourcepacks/" + pack + "temp.zip");
            LOGGER.info("Starting extraction for " + pack + "+...");
            unzipFile(Paths.get("resourcepacks/" + pack + "temp.zip"));
            Files.move(new File("resourcepacks/" + pack + "temp/" + pack + "-" + mainOrMaster).toPath(), new File("resourcepacks/" + pack + "plus").toPath(), StandardCopyOption.REPLACE_EXISTING);
            FileUtils.deleteDirectory(new File("resourcepacks/" + pack + "temp"));
            FileUtils.delete(new File("resourcepacks/" + pack + "temp.zip"));
            PrintWriter writer = new PrintWriter("resourcepacks/" + pack + "plus/" + "commit_hash.txt", "UTF-8");
            writer.println(hash);
            writer.close();
            LOGGER.info("Finished downloading and extracting " + pack + "+!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void downloadAllPacks()
    {
        downloadResourcePack("elytras");
        downloadResourcePack("pumpkins");
        downloadResourcePack("weapons");
        downloadResourcePack("creatures");
    }

    @Override
    public void onInitializeClient() {
        downloadAllPacks();
    }
}
