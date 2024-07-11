package org.remast.baralga.repository;

import java.io.IOException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.GzipSource;
import okio.Okio;
public class GzipInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder newRequestBuilder = originalRequest.newBuilder()
                .addHeader("Accept-Encoding", "gzip");

        Response response = chain.proceed(newRequestBuilder.build());

        if (isGzipped(response)) {
            return unzip(response);
        } else {
            return response;
        }
    }

    private Response unzip(Response response) throws IOException {
        if (response.body() == null) {
            return response;
        }

        GzipSource gzipSource = new GzipSource(response.body().source());
        String bodyString = Okio.buffer(gzipSource).readUtf8();

        Headers strippedHeaders = response.headers().newBuilder()
                .removeAll("Content-Encoding")
                .removeAll("Content-Length")
                .build();

        ResponseBody responseBody = ResponseBody.create(bodyString, response.body().contentType());

        return response.newBuilder()
                .headers(strippedHeaders)
                .body(responseBody)
                .build();
    }

    private boolean isGzipped(Response response) {
        String contentEncoding = response.header("Content-Encoding");
        return contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip");
    }
}
