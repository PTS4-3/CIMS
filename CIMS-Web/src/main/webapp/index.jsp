<%-- 
    Document   : home
    Created on : 23-mei-2015, 13:51:23
    Author     : Alexander
--%>


<%@page import="com.sun.glass.ui.Application"%>
<%@page import="Shared.Data.INewsItem"%>
<%@page import="Controller.IndexController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>
    <head>
        <title>Algemeen</title>
        <% IndexController controller = new IndexController(application);
           int pagenr = 1;
           int limit = 5;
           int maxPagenr = 1;
           
           if(request.getParameter("pagenr") != null && !request.getParameter("pagenr").isEmpty()) {
               pagenr = Integer.parseInt(request.getParameter("pagenr"));
           }
           
           maxPagenr = (int) Math.ceil((double) controller.getNewsItemCount() / (double) limit);
        %>
    </head>	
    <body>	
        <% for (INewsItem n : controller.getNewsItems(limit * (pagenr - 1), limit)) { 
            if(n != null) { %>

                <article class="news">

                    <% //if(n.getPicture() != null) { 
                        if(!true) {%>
                        <div class="fotodiv">
                            <img src="images/foto1.jpg" alt="cover" class="foto"/>
                        </div>
                    <% } %>

                    <h1><%= n.getTitle() %></h1>
                    <p class ="date"><%= n.getDateString() %></p>
                    <p><% out.println(n.getCity().toUpperCase() +  " - " + n.getDescription()); %></p>
                    <a class="read" href=<% out.println("news.jsp?newsid=" + n.getId()); %>><b>Lees verder</b> &#10162;</a>
                </article>
        <%      }
            } 
        %>
                <div id="pagenumbers">
                    <ul>
                        <li>
                            <% if(pagenr > 1) { %>
                                <a href=<% out.println("index.jsp?pagenr=" + (pagenr - 1)); %>>&#8678; Vorige</a> <%
                            } else { %>
                                &nbsp; <%
                            } %>
                        </li>
                        <li style="width:50px">
                            <% out.print(pagenr); %>
                        </li>
                        <li> <%
                            if(pagenr < maxPagenr) { %>
                                <a href=<% out.println("index.jsp?pagenr=" + (pagenr + 1)); %>>Volgende &#8680;</a> <%
                            } else { %>
                                &nbsp; <%
                            } %>
                        </li>
                    </ul>
                </div>
    </body>
</html>

