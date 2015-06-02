<%-- 
    Document   : news
    Created on : 21-mei-2015, 10:16:42
    Author     : Linda
--%>

<%@page import="java.util.HashSet"%>
<%@page import="java.util.Calendar"%>
<%@page import="Shared.Data.INewsItem"%>
<%@page import="java.util.Date"%>
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
        <%
            webController controller = new webController();
            String ID = request.getParameter("newsid");
            INewsItem item = null;
            try {
                item = controller.getNewsWithID(ID);
            } catch (Exception ex) {
                item = null;
            }
        %>
    </head>	
    <body>	
        <div id="page">		
            <section class="center">			
                <article class="newsitem">				
                    <% if (item != null) {%>
                    <h1><%= item.getTitle()%></h1>
                    <p class="date"><%= item.getDateString() %></p>
                    <p><%= item.getLocation().toUpperCase() %> - <%= item.getDescription()%></p>
                    <!--met foto-->
                    <div id="pics" >
                    </div>

                    <div id="map" >
                        <h2>Locatie</h2>
                        <div id="mapcanvas">
                        </div>
                    </div>
                    <%} else {%>
                    <h1>Titel1</h1>
                    <p class="date"><%= controller.getDate() %>
                    </p>
                    <p>PLAATS - Beschrijving</p>                    
                    <!--met foto-->
                    <div id="pics" >
                        <h2>Foto's</h2>
                        <a href="images/foto1.jpg" data-lightbox="news">
                            <img src="images/foto1.jpg" alt="foto"/>
                        </a>
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
                        </div>

                    <div id="map" >
                        <h2>Locatie</h2>
                        <div id="mapcanvas">
                        </div>
                    </div>
                        <%}%>
                </article>
                <article class="advice">
                    <% if (item != null) {%>
                    <!-- item is not null -->
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
                        <% 
                            HashSet<Advice> set = controller.getAdvices(item);

                            for(Advice ad : set){%>
                            <li><%= ad.getDescription() %></li>
                            <% } %>
                    </ul>
                    
                    <%} else {%>
                    
                    <!-- item is null-->
                    <h2>Informatie</h2>
                    <p>Slachtoffers: 1</p>

                    <h3>Situatie</h3>
                    <br />

                    <div class="icon">
                        <img src="images/icon_stoffen.png" alt="icon"/>
                        <p>&nbsp;Gevaarlijke stoffen</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_inbraak.png" alt="icon"/>
                        <p>&nbsp;Inbraak</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_leven.png" alt="icon"/>
                        <p>&nbsp;Levensgevaar</p>
                    </div>				
                    <div class="icon">
                        <img src="images/icon_aanslag.png" alt="icon"/>
                        <p>&nbsp;Terroristische aanslag</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_brand.png" alt="icon"/>
                        <p>&nbsp;Brand</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_instorting.png" alt="icon"/>
                        <p>&nbsp;Instortingsgevaar</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_orde.png" alt="icon"/>
                        <p>&nbsp;Ordeverstoring</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_verkeer.png" alt="icon"/>
                        <p>&nbsp;Verkeersongeval</p>
                    </div>
                    <div class="icon">
                        <img src="images/icon_weer.png" alt="icon"/>
                        <p>&nbsp;Extreem weer</p>
                    </div>

                    <br />
                    <h3>Advies</h3>
                    <ul>
                        <li>Sluit ramen en deuren.</li>
                        <li>Ga naar een goed af te sluiten kamer waar het niet tocht, liefst midden in het huis of gebouw.</li>
                        <li>Houd de vluchtstrook vrij voor brandweer, politie en ambulance.</li>
                    </ul>
                    <%}%>
                </article>
            </section>
        </div>	
    </body>
</html>