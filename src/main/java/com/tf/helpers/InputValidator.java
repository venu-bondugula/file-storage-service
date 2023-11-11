package com.tf.helpers;

import com.tf.exception.EmptyFileException;
import com.tf.exception.FileHashMismatchException;
import com.tf.exception.FileNotProvidedException;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class InputValidator {
    public static void validate(MultipartFile file, String fileHash) {
        if (file == null) {
            throw new FileNotProvidedException();
        }
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }

        if (!validateFileHash(file, fileHash)) {
            throw new FileHashMismatchException();
        }
    }

    private static boolean validateFileHash(MultipartFile file, String fileHash) {
        String uploadedFileHash = calculateFileHash(file);
        System.out.println("file hash is : " + uploadedFileHash);
        return uploadedFileHash.equalsIgnoreCase(fileHash);

    }

    private static String calculateFileHash(MultipartFile file) {
        // Calculate the hash using DigestUtils or another hash algorithm
        try {
            return DigestUtils.md5DigestAsHex(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String calculateSha256Hash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(file.getBytes());

        // Convert the byte array to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xFF & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }



    /**/
    public BoxFile.Info uploadLargeFile(InputStream inputStream, long fileSize)
            throws InterruptedException, IOException {
        URL url = UPLOAD_SESSION_URL_TEMPLATE.build(this.getAPI().getBaseUploadURL(), this.getID());
        return new LargeFileUpload().upload(this.getAPI(), inputStream, url, fileSize);
    }

    private BoxFile.Info uploadHelper(BoxFileUploadSession.Info session, InputStream stream, long fileSize,
                                      Map<String, String> fileAttributes)
            throws InterruptedException {
        //Upload parts using the upload session
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(DIGEST_ALGORITHM_SHA1);
        } catch (NoSuchAlgorithmException ae) {
            throw new BoxAPIException("Digest algorithm not found", ae);
        }
        DigestInputStream dis = new DigestInputStream(stream, digest);
        List<BoxFileUploadSessionPart> parts = this.uploadParts(session, dis, fileSize);

        //Creates the file hash
        byte[] digestBytes = digest.digest();
        String digestStr = Base64.encode(digestBytes);

        //Commit the upload session. If there is a failure, abort the commit.
        try {
            return session.getResource().commit(digestStr, parts, fileAttributes, null, null);
        } catch (Exception e) {
            session.getResource().abort();
            throw new BoxAPIException("Unable to commit the upload session", e);
        }
    }

    private List<BoxFileUploadSessionPart> uploadParts(
            BoxFileUploadSession.Info session, InputStream stream, long fileSize
    ) throws InterruptedException {
        List<BoxFileUploadSessionPart> parts = new ArrayList<>();

        int partSize = session.getPartSize();
        long offset = 0;
        long processed = 0;
        int partPostion = 0;
        //Set the Max Queue Size to 1.5x the number of processors
        double maxQueueSizeDouble = Math.ceil(this.executorService.getMaximumPoolSize() * 1.5);
        int maxQueueSize = Double.valueOf(maxQueueSizeDouble).intValue();
        while (processed < fileSize) {
            //Waiting for any thread to finish before
            long timeoutForWaitingInMillis = TimeUnit.MILLISECONDS.convert(this.timeout, this.timeUnit);
            if (this.executorService.getCorePoolSize() <= this.executorService.getActiveCount()) {
                if (timeoutForWaitingInMillis > 0) {
                    Thread.sleep(LargeFileUpload.THREAD_POOL_WAIT_TIME_IN_MILLIS);
                    timeoutForWaitingInMillis -= THREAD_POOL_WAIT_TIME_IN_MILLIS;
                } else {
                    throw new BoxAPIException("Upload parts timedout");
                }
            }
            if (this.executorService.getQueue().size() < maxQueueSize) {
                long diff = fileSize - processed;
                //The size last part of the file can be lesser than the part size.
                if (diff < (long) partSize) {
                    partSize = (int) diff;
                }
                parts.add(null);
                byte[] bytes = getBytesFromStream(stream, partSize);
                this.executorService.execute(
                        new LargeFileUploadTask(session.getResource(), bytes, offset,
                                partSize, fileSize, parts, partPostion)
                );

                //Increase the offset and proceesed bytes to calculate the Content-Range header.
                processed += partSize;
                offset += partSize;
                partPostion++;
            }
        }
        this.executorService.shutdown();
        this.executorService.awaitTermination(this.timeout, this.timeUnit);
        return parts;
    }
}
