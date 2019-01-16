package serverSide.code;

import org.apache.catalina.*;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.juli.AsyncFileHandler;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class HTTPManager {

    public HTTPManager() throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.getConnector();

        String contextPath = "/";
        String docBase = new File(".").getAbsolutePath();

        Context context = tomcat.addContext(contextPath, docBase);

        Servlet servlet = new HttpServlet() {

            @Override
            public void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException
            {
                req.startAsync();
                RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
                System.out.println("we did it");
                HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
                    public String getServletPath() { return ""; }
                };
                rd.forward(wrapped, resp);
            }
        };


        String servletName = "default";
        String urlPattern = "/";

        tomcat.addServlet(contextPath, servletName, servlet);
        context.addServletMappingDecoded(urlPattern, servletName);

        tomcat.start();
        tomcat.getServer().await();
    }





//        Tomcat server = new Tomcat();
//        server.setBaseDir("temp");
//        server.setPort(8080);
//        server.getConnector();
//        Context context = server.addContext("/", new File(".").getAbsolutePath());
//
//        Wrapper defaultServlet = context.createWrapper();
//        defaultServlet.setName("default");
//        defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
//        defaultServlet.addInitParameter("debug", "0");
//        defaultServlet.addInitParameter("listings", "false");
//        defaultServlet.setLoadOnStartup(1);
//        context.addChild(defaultServlet);
//        context.addServletMappingDecoded("/", "default");
//
//        Wrapper jspServlet = context.createWrapper();
//        jspServlet.setName("jsp");
//        jspServlet.setServletClass("org.apache.jasper.servlet.JspServlet");
//        jspServlet.addInitParameter("fork", "false");
//        jspServlet.addInitParameter("xpoweredBy", "false");
//        jspServlet.setLoadOnStartup(2);
//        context.addChild(jspServlet);
//        context.addServletMappingDecoded("*.jsp", "jsp");
//        try {
//            server.start();
//            server.getServer().await();
//        } catch (LifecycleException e) {
//            e.printStackTrace();
//        }
    }

