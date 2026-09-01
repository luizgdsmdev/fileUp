package com.bytebybyte.fileup.Application.Utils.Upload;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


@Component
public class UploadHashProcess {
    private static final String SHA_256 = "SHA-256";
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    private UploadHashProcess() {
    }

    /**
     * Public method to calculate the SHA-256 hash of the file at the given path.
     * @param path Path type from the request
     * @return The SHA-256 hash of the file as a hexadecimal string
     */
    public static String pathToHash(Path path) {
        try{
            // Read the file into bytes
            return hashToSha256(Files.readAllBytes(path));

        } catch (IOException e) {
            throw new RuntimeException("Error while reading data for hash calculation at utils_upload_pathToHash_method: ", e);
        } catch (Exception e) {
            throw new RuntimeException("Error while calculating hash at utils_upload_pathToHash_method: ", e);
        }
    }

    /**
     * Method to calculate the SHA-256 hash of the given bytes.
     * It catches any exceptions that may occur during the hash calculation.
     * @param bytes The bytes to be hashed, from upload chunk being processed
     * @return The SHA-256 hash of the bytes as a hexadecimal string
     */
    public static String hashToSha256(byte[] bytes) {
        try {
            return calculatesHash(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Error while trying to calculate data hash at utils_upload_hashToSHA256_method: ", e);
        }
    }


    /**
     * Calculates the SHA-256 hash of the given bytes.
     * @param bytes The bytes to be hashed, from upload chunk being processed
     * @return The SHA-256 hash of the bytes as a hexadecimal string
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available
     */
    private static String calculatesHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
        byte[] hash = messageDigest.digest(bytes);
        return HEX_FORMAT.formatHex(hash);
    }

}
