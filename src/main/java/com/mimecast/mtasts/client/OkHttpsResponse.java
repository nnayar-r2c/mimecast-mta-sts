package com.mimecast.mtasts.client;

import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OK HTTPS Response.
 * <p>Wrapper for HttpsPolicyClient response.
 *
 * @author "Vlad Marian" <vmarian@mimecast.com>
 * @link http://mimecast.com Mimecast
 */
public class OkHttpsResponse implements HttpsResponse {
    private static final Logger log = LogManager.getLogger(OkHttpsResponse.class);

    private String body = null;
    private boolean successful = false;
    private int code = 0;
    private String message = null;
    private boolean handshake = false;
    private List<Certificate> certificates = new ArrayList<>();
    private Map<String, String> headers = new HashMap<>();

    /**
     * Constructs a new HttpsResponse instance with given OK HTTP Response.
     * <p>Wrapper for OkHttpPolicyCLient response.
     *
     * @param response Response instance.
     */
    public OkHttpsResponse(Response response) throws IOException {
        if (response != null) {
            body = bufferResponseBody(response);
            successful = response.isSuccessful();
            code = response.code();
            message = response.message();

            if (response.handshake() != null) {
                handshake = true;
                try {
                    certificates = response.handshake().peerCertificates();
                } catch (Exception e) {
                    log.error("Found no peer certificate chain");
                }
            }

            response.headers().iterator().forEachRemaining(header -> headers.put(header.getFirst().toLowerCase(), header.getSecond()));
        }
    }

    /**
     * Is successful.
     *
     * @return Boolean.
     */
    @Override
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets code.
     *
     * @return Integer.
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * Gets message.
     *
     * @return Message string.
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Is handshake.
     *
     * @return Boolean.
     */
    @Override
    public boolean isHandshake() {
        return handshake;
    }

    /**
     * Gets peer certificates.
     *
     * @return List of Certificate.
     */
    @Override
    @SuppressWarnings("squid:S1168")
    public List<Certificate> getPeerCertificates() {
        return certificates;
    }

    /**
     * Gets header.
     *
     * @param name Header name string.
     * @return Header value string.
     */
    @Override
    public String getHeader(String name) {
        return headers.get(name.toLowerCase());
    }

    /**
     * Gets body.
     *
     * @return Body string.
     */
    @Override
    public String getBody() {
        return body;
    }

    /**
     * Feeds a response body into a buffer and returns it as a string.
     *
     * @author "Andrew Havis" <ahavis@mimecast.com>
     * @param response HTTP response.
     * @return Body string.
     */
    @Nullable
    private String bufferResponseBody(@NotNull Response response) throws IOException {
        final int BUFFER_SIZE = 32;
        if (response.body() != null) {
            Buffer buffer = new Buffer();
            BufferedSource bufferedSource = response.body().source();
            if (bufferedSource.isOpen()) {
                while (!bufferedSource.exhausted()) {
                    bufferedSource.read(buffer, BUFFER_SIZE);
                }
                bufferedSource.close();
            }

            response.body().close();
            return buffer.size() > 0 ? buffer.readString(Charset.defaultCharset()) : "";
        }
        return null;
    }
}
