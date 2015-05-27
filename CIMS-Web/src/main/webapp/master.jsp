<%-- 
    Document   : master
    Created on : 23-mei-2015, 13:28:36
    Author     : Alexander
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<%@taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fn" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>CIMS 112 Nieuws - <decorator:title/></title>
        <link href="style.css" rel="stylesheet" type="text/css" />
        <link href="lightbox/css/lightbox.css" rel="stylesheet" type="text/css" />
    </head>
    <body>
        <header>
            <div class="center">
                <div id="logo">
                    <img src="images/logo2.png" alt="cover" class="logo"/>	
                </div>
                <nav>
                    <ul>
                        <li><a href="index.jsp">Home</a></li>
                    </ul>
                </nav>
            </div>
        </header>

        <div id="page">		
            <section class="center">			
                <decorator:body/>
            </section>
        </div>	

        <footer>
            <div class = "center">
                &copy; CIMS
            </div>
        </footer>
    </body>
</html>
