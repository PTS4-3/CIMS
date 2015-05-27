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
public class ServerMainServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        ServerMain.startDatabases(getServletContext().getRealPath("/WEB-INF/"));
        ServerMain.startConnection();
    }

    @Override
    public void destroy() {
//        System.out.println("------------ SERVLET DESTROYED");
        ServerMain.connectionHandler.close();
        super.destroy();
    }

}
