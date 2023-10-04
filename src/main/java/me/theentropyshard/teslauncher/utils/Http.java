/*
 * Copyright 2023 TheEntropyShard (https://github.com/TheEntropyShard)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.theentropyshard.teslauncher.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Http {
    public static final String USER_AGENT = "TESLauncher/1.0.0";

    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";

    public static void downloadFile(String url, Path savePath) throws IOException {
        Http.downloadFile(url, savePath, new NopProgressListener());
    }

    public static void downloadFile(String url, Path savePath, ProgressListener listener) throws IOException {
        if (!Files.exists(savePath)) {
            PathUtils.createDirectoryIfNotExists(savePath.getParent());
        }

        HttpURLConnection c = Http.buildConnection(url, Http.GET_METHOD);
        c.connect();
        long contentLengthLong = c.getContentLengthLong();

        try (ReadableByteChannel rbc = Channels.newChannel(c.getInputStream());
             FileOutputStream fos = new FileOutputStream(savePath.toFile());
             FileChannel channel = fos.getChannel()) {
            long bytesWritten = 0;
            for (; bytesWritten < contentLengthLong; ) {
                bytesWritten += channel.transferFrom(rbc, bytesWritten, contentLengthLong - bytesWritten);
                listener.onData(contentLengthLong, bytesWritten, false);
            }
            listener.onData(contentLengthLong, bytesWritten, true);
        }
    }

    private static HttpURLConnection buildConnection(String url, String method) throws IOException {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", Http.USER_AGENT);

        return c;
    }

    public static byte[] get(String url) throws IOException {
        return Http.get(url, new NopProgressListener());
    }

    public static byte[] get(String url, ProgressListener listener) throws IOException {
        HttpURLConnection c = Http.buildConnection(url, Http.GET_METHOD);

        return Http.inputStreamToByteArray(Http.getNeededInputStream(c), listener, c);
    }

    public static byte[] post(String url, String contentType, byte[] payload) throws IOException {
        HttpURLConnection c = Http.buildConnection(url, Http.POST_METHOD);
        c.setRequestProperty("Content-Type", contentType);
        c.setRequestProperty("Content-Length", String.valueOf(payload.length));
        c.setDoOutput(true);

        OutputStream outputStream = c.getOutputStream();
        outputStream.write(payload);
        outputStream.flush();

        return Http.inputStreamToByteArray(Http.getNeededInputStream(c), new NopProgressListener(), c);
    }

    private static InputStream getNeededInputStream(HttpURLConnection c) throws IOException {
        return c.getErrorStream() == null ? c.getInputStream() : c.getErrorStream();
    }

    private static byte[] inputStreamToByteArray(InputStream inputStream, ProgressListener listener, HttpURLConnection c) throws IOException {
        long contentLengthLong = c.getContentLengthLong();

        byte[] buffer = new byte[8192];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (BufferedInputStream input = new BufferedInputStream(inputStream)) {
            long count = 0L;
            int numRead;
            do {
                numRead = input.read(buffer, 0, 8192);
                if (numRead != -1) {
                    count += numRead;
                    output.write(buffer, 0, numRead);
                }
                listener.onData(contentLengthLong, count, numRead == -1);
            } while (numRead != -1);
        }

        return output.toByteArray();
    }

    public interface ProgressListener {
        void onData(long totalBytes, long currentBytes, boolean done);
    }

    public static final class NopProgressListener implements ProgressListener {
        public void onData(long totalBytes, long currentBytes, boolean done) {
            // No-operation progress listener
        }
    }

    private Http() {
        throw new UnsupportedOperationException();
    }
}