package serverSide;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HTTPManager implements Closeable {

    private Tomcat tomcat;

    public HTTPManager() {
        try {
            tomcat = new Tomcat();
            tomcat.setPort(4911);
            tomcat.getConnector();

            String contextPath = "/";
            String docBase = new File(".").getAbsolutePath();

            Context context = tomcat.addContext(contextPath, docBase);

            Servlet servlet = new HttpServlet() {

                @Override
                public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
                    if (req.getRequestURI().contains("video")) {
                        File file = new File(new File(ScoutingVars.getVideosDirectory(), req.getParameter("competition")), req.getParameter("game") + ".mp4");
                        if (!file.canRead()) {
                            resp.sendError(404, "Game not found");
                            return;
                        }
                        resp.setHeader("Content-Type", getServletContext().getMimeType(req.getRequestURI()));
                        resp.setHeader("Content-Length", String.valueOf(file.length()));
                        resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");
                        Files.copy(file.toPath(), resp.getOutputStream());
                    }
                }
            };

            String servletName = "default";
            String urlPattern = "/";

            tomcat.addServlet(contextPath, servletName, servlet);
            context.addServletMappingDecoded(urlPattern, servletName);

            tomcat.start();
            tomcat.getServer().await();
        } catch (LifecycleException e){
            throw new Error("HTTP file sharing just went down!", e);
        }
    }

    @Override
    public void close() throws IOException{
        try {
            tomcat.stop();
        } catch (LifecycleException e) {
            throw new IOException("Tomcat refused to close down!", e);
        }
    }
}