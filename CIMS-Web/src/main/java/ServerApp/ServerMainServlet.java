/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ServerApp;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 *
 * @author Kargathia
 */
public class ServerMainServlet extends HttpServlet{

    @Override
    public void init() throws ServletException {

        System.out.println("ServerMain servlet init");
        ServerMain.startDatabases(getServletContext().getRealPath("/WEB-INF/"));
        ServerMain.startConnection();


        ServletContextListener listener = new ServletContextListener() {

            @Override
            public void contextInitialized(ServletContextEvent sce) {
                System.out.println("-----CONTEXT INIT");
            }

            @Override
            public void contextDestroyed(ServletContextEvent sce) {
                System.out.println("------- CONTEXT DESTROYED");
                System.exit(0);
            }
        };

        getServletContext().addListener(listener);
    }

    @Override
    public void destroy() {
//        super.destroy();
        System.out.println("------------ SERVLET DESTROYED");
        System.exit(0);
    }

}
