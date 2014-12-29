package com.b5m.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by boguang on 14/12/23.
 */
public class EmbedHttpServer  implements Runnable{

    private int port;
    private ServerSocket serverSocket;

    public EmbedHttpServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        if (null == serverSocket) {
            serverSocket = new ServerSocket(port);
            new Thread(this,"embed-http-server").start();
        }
    }

    public void stop() throws IOException {
        if (null != serverSocket) {
            serverSocket.close();
            serverSocket = null;
        }
    }

    protected void handle(String method, String path ,Map<String,String> header,
                          InputStream ins, ResponseOutputStream response) throws IOException {

    }

    @Override
    public void run() {
        final ServerSocket ss = serverSocket;
        while (ss == serverSocket) {
            Socket conn =null;
            try {
                conn = serverSocket.accept();
                String method = null;
                String path = null;
                HashMap<String, String > header = new HashMap<String, String>();
                InputStream ins = conn.getInputStream();
                StringBuilder sb = new StringBuilder(512);
                int l ;
                while ((l = ins.read()) != -1) {
                    if ('\n' == l ) {
                        if (sb.length() > 0 && sb.charAt(sb.length() -1) == '\r')
                            sb.setLength(sb.length() -1 );
                        if (sb.length() == 0)
                            break;
                        else if (method == null) {
                            int i = sb.indexOf(" ");
                            method = sb.substring(0 , i );
                            int j = sb.lastIndexOf(" HTTP/");
                            path = sb.substring(i + 1, j).trim();
                        }
                        else {
                            int i = sb.indexOf(":");
                            String name = sb.substring(0 , i).trim();
                            String value = sb.substring(i+1).trim();
                            header.put(name,value);
                        }
                        sb.setLength(0);
                    } else {
                        sb.append((char)l);
                    }
                }
                int contentLength = 0;
                String str = header.get("Content-Length");
                if (null != str) {
                    contentLength = Integer.parseInt(str);
                }

                OutputStream os = conn.getOutputStream();
                str = header.get("Expect");
                if ("100-Continue".equalsIgnoreCase(str)) {
                    os.write("HTTP/1.1 100 Continue\r\n\r\n".getBytes("ASCII"));
                    os.flush();
                }
                BodyInputStream inputStream = new BodyInputStream(ins,contentLength);
                ResponseOutputStream response = new ResponseOutputStream(os);
                handle(method,path,header,inputStream,response);
                response.close();
                conn.close();
                conn = null;

            } catch (IOException e) {
                if (null != conn)
                    try {
                        conn.close();
                        conn = null;
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                e.printStackTrace();
            }
        }
        if (!ss.isBound() || ss.isClosed()) {
            serverSocket = null;
        }
    }

    private static class BodyInputStream extends InputStream {
        private InputStream ins;
        private int n;

        public BodyInputStream(InputStream ins, int n) {
            this.ins = ins;
            this.n = n;
        }

        @Override
        public int available() throws IOException {
            return n;
        }

        @Override
        public void close() throws IOException {
            ins.close();
        }

        @Override
        public void mark(int readlimit) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        @Override
        public int read() throws IOException {
            if (n <= 0)
                return -1;
            int r = ins.read();
            if (-1 != r)
                n--;
            return r;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {

            if (n <= 0)
                return -1;
            int r = ins.read(buffer,offset,length < n ? length : n);
               n -= r;
            return r;
        }

        @Override
        public synchronized void reset() throws IOException {
            super.reset();
        }

        @Override
        public long skip(long byteCount) throws IOException {
            throw  new IOException("unsported");
        }
    }

    public static class ResponseOutputStream extends OutputStream {
        private static final byte[] CRLF = {(byte)'\r',(byte)'\n'};
        private OutputStream os;
        /*
        * 0: statusline
        * 1:headers
        * 2:body
        * 3:closed
        * */
        private int lv ;

        public ResponseOutputStream(OutputStream os) {
            this.os = os;
        }

        public void setStatusCode(int statusCode) throws IOException {
            switch (statusCode) {
                case 200:
                    setStatusLine("200 OK");
                    break;
                case 201:
                    setStatusLine("201 Created");
                    break;
                case 202:
                    setStatusLine("202 Accepted");
                    break;
                case 301:
                    setStatusLine("301 Moved Permanently");
                    break;
                case 304:
                    setStatusLine("304 Not Modified");
                    break;
                case 400:
                    setStatusLine("400 Bad Request");
                    break;
                case 401:
                    setStatusLine("401 Unauthorized");
                    break;
                case 403:
                    setStatusLine("403 Forbidden");
                    break;
                case 404:
                    setStatusLine("404 Not Found");
                    break;
                case 405:
                    setStatusLine("405 Method Not Allowed");
                    break;
                case 500:
                    setStatusLine("500 Internal Server Error");
                    break;
                case 501:
                    setStatusLine("501 Not Implemented");
                    break;
                default:
                    setStatusLine(String.valueOf(statusCode));
                    break;
            }
        }

        public void setStatusLine(String statusLine) throws IOException {
            if (0 == lv) {
                os.write("HTTP/1.1".getBytes("ASCII"));
                os.write(statusLine.getBytes("ASCII"));
                os.write(CRLF);
                lv = 1;
            } else {
                throw new IOException("status line is already set!");
            }
        }

        public void setHeader(String name, String value) throws IOException {
            if (lv < 1)
                setStatusCode(200);
            if (1 == lv) {
                os.write(name.getBytes("ASCII"));
                os.write(':');
                os.write(' ');
                os.write(value.getBytes("ASCII"));
                os.write(CRLF);
            } else {
                throw new IOException("header is already set!");
            }
        }

        public void setContentLength(int length) throws IOException {
            setHeader("Content-Length", String.valueOf(length));
        }

        public void setContentEncoding(String value) throws IOException {
            setHeader("Content-Encoding", value);

        }

        public void setContentType(String value) throws IOException {
            setHeader("Content-Type", value);
        }

        /**
         * Content-Type: text/plain
         */
        public void setContentTypeText() throws IOException {
            setContentType("text/plain");
        }

        /**
         * Content-Type: text/plain; charset=utf-8
         */
        public void setContentTypeTextUtf8() throws IOException {
            setContentType("text/plain; charset=utf-8");
        }

        /**
         * Content-Type: text/html
         */
        public void setContentTypeHtml() throws IOException {
            setContentType("text/html");
        }

        /**
         * Content-Type: text/html; charset=utf-8
         */
        public void setContentTypeHtmlUtf8() throws IOException {
            setContentType("text/html; charset=utf-8");
        }

        /**
         * Content-Type: application/octet-stream
         */
        public void setContentTypeBinary() throws IOException {
            setContentType("application/octet-stream");
        }

        /**
         * Content-Type: application/json
         */
        public void setContentTypeJson() throws IOException {
            setContentType("application/json");
        }

        /**
         * Content-Type: text/xml
         */
        public void setContentTypeXml() throws IOException {
            setContentType("text/xml");
        }

        /**
         * Content-Type: application/zip
         */
        public void setContentTypeZip() throws IOException {
            setContentType("application/zip");
        }

        /**
         * Content-Type: image/jpeg
         */
        public void setContentTypeJpeg() throws IOException {
            setContentType("image/jpeg");
        }

        /**
         * Content-Type: image/png
         */
        public void setContentTypePng() throws IOException {
            setContentType("image/png");
        }

        @Override
        public void write(int oneByte) throws IOException {
            if (lv < 1)
                setStatusCode(200);
            if (lv < 2) {
                os.write(CRLF);
                lv = 2;
            }
            os.write(oneByte);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            if (lv < 1)
                setStatusCode(200);
            if (lv < 2) {
                os.write(CRLF);
                lv = 2;
            }
            os.write(buffer,offset,count);
        }

        @Override
        public void close() throws IOException {
            if (lv < 1)
                setStatusCode(200);
            if (lv < 2) {
                os.write(CRLF);
                lv = 2;
            }
            if (lv < 3 ) {
                os.close();
                lv = 3;
            }
        }

        @Override
        public void flush() throws IOException {
            os.flush();
        }
    }
}
