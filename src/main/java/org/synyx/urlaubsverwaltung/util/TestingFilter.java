package org.synyx.urlaubsverwaltung.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


public final class TestingFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
        ServletException {

        ByteResponseWrapper brw = new ByteResponseWrapper((HttpServletResponse) response);
        chain.doFilter(request, brw);

        String out = new String(brw.getBytes());

//        out = out.replace("<body>",
//                "<body><p><a href=\"http://www.thevadersong.com/\"><img src=\"http://farm3.static.flickr.com/2248/2282734669_a7f431e660_o.jpg\"></a></p>");

        response.getWriter().print(out);
    }


    public void destroy() {
    }


    public void init(FilterConfig filterConfig) {
    }

    static class ByteResponseWrapper extends HttpServletResponseWrapper {

        private PrintWriter writer;
        private ByteOutputStream output;

        public ByteResponseWrapper(HttpServletResponse response) {

            super(response);
            output = new ByteOutputStream();
            writer = new PrintWriter(output);
        }

        public byte[] getBytes() {

            writer.flush();

            return output.getBytes();
        }


        @Override
        public PrintWriter getWriter() {

            return writer;
        }


        @Override
        public ServletOutputStream getOutputStream() throws IOException {

            return output;
        }
    }

    static class ByteOutputStream extends ServletOutputStream {

        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        @Override
        public void write(int b) throws IOException {

            bos.write(b);
        }


        public byte[] getBytes() {

            return bos.toByteArray();
        }
    }
}
