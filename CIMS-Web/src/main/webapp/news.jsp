<%-- 
    Document   : news
    Created on : 21-mei-2015, 10:16:42
    Author     : Linda
--%>

<%@page import="Shared.Data.Advice"%>
<%@page import="Shared.Data.Situation"%>
<%@page import="Shared.Data.NewsItem"%>
<%@page import="Controller.webController"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>CIMS 112 Nieuws</title>
        <link href="style.css" rel="stylesheet" type="text/css" />
        <%
            webController controller = new webController();
            String ID = request.getParameter("newsId");
            NewsItem item = controller.getNewsWithID(ID);
        %>
        <link href="lightbox/css/lightbox.css" rel="stylesheet" />

        <script src="lightbox/js/jquery-1.11.0.min.js"></script>
        <script src="lightbox/js/lightbox.min.js"></script>
    </head>	
    <body>	
        <div id="page">		
            <section class="center">			
                <article class="newsitem">				

                    <h1><%= item.getTitle()%></h1>
                    <p class="date"><%= item.getDate().toString() %></p>
                    <p><%= item.getLocation()%> - <%= item.getDescription()%></p>				
                    <!--met foto-->
                    <h2>Foto's</h2>
                    <br />
                    <a href="images/foto1.jpg" data-lightbox="news"><img src="images/foto1.jpg" alt="foto"/></a>

                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/>
                    </a>
                    <a href="images/foto2.jpg" data-lightbox="news">
                        <img src="images/foto2.jpg" alt="foto"/>
                    </a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/>
                    </a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/>
                    </a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/>
                    </a>
                    <a href="images/foto2.jpg" data-lightbox="news">
                        <img src="images/foto2.jpg" alt="foto"/>
                    </a>
                </article>
                <article class="advice">
                    <h2>Informatie</h2>
                    <p>Slachtoffers: <%= Integer.toString(item.getVictims())%></p>

                    <h3>Situatie</h3>
                    <br />

                    <%for (Situation sit : item.getSituations()) {%>
                    <div class="icon">
                        <img src="<%= controller.getIconURL(sit)%>" alt="icon"/>
                        <p>&nbsp;<%= sit.getDescription()%></p>
                    </div>
                    <%}%>

                    <br />
                    <h3>Advies</h3>
                    <ul>
                        <% for (Situation sit : item.getSituations()) {
                                for (Advice ad : sit.getAdvices()) {%>
                                <li><%= ad.getDescription() %></li>
                                <%}
                            }%>
                    </ul>
                </article>
            </section>
        </div>	
    </body>
</html>