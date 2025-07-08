package com.example.surakshak2;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public abstract class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final Response.Listener<NetworkResponse> mListener;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + DataPart.boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            for (Map.Entry<String, String> entry : getParams().entrySet()) {
                bos.write(("--" + DataPart.boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes());
                bos.write((entry.getValue() + "\r\n").getBytes());
            }

            for (Map.Entry<String, DataPart> entry : getByteData().entrySet()) {
                DataPart dp = entry.getValue();
                bos.write(("--" + DataPart.boundary + "\r\n").getBytes());
                bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dp.getFileName() + "\"\r\n").getBytes());
                bos.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
                bos.write(dp.getContent());
                bos.write("\r\n".getBytes());
            }

            bos.write(("--" + DataPart.boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        mListener.onResponse(response);
    }

    protected abstract Map<String, String> getParams() throws AuthFailureError;

    protected abstract Map<String, DataPart> getByteData() throws AuthFailureError;

    public static class DataPart {
        public static final String boundary = "applogic-surakshak-boundary";

        private final String fileName;
        private final byte[] content;

        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
