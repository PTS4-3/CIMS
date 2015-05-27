<%-- 
    Document   : news
    Created on : 21-mei-2015, 10:16:42
    Author     : Linda
--%>

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
        <link href="style.css" rel="stylesheet" type="text/css" />
        <%
            webController controller = new webController();
            String ID = request.getParameter("id");
            NewsItem item = null;
            try{
            item = controller.getNewsWithID(ID);
            }
            catch(Exception ex){
                item=null;
            }
        %>
        <link href="lightbox/css/lightbox.css" rel="stylesheet" />

        <script src="lightbox/js/jquery-1.11.0.min.js"></script>
        <script src="lightbox/js/lightbox.min.js"></script>
    </head>	
    <body>	
        <div id="page">		
            <section class="center">			
                <article class="newsitem">				
                    <% if (item != null) {%>
                    <h1><%= item.getTitle()%></h1>
                    <p class="date"><%= item.getDate().toString()%></p>
                    <p><%= item.getLocation()%> - <%= item.getDescription()%></p>
                    <%//if(item.getPictures()!= null){ } %>

                    <!--met foto-->
                    <!--<h2>Foto's</h2>
                    <br />
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>-->
                    <%} else{%>
                    <h1>Titel1</h1>
                    <p class="date"><%= new Date()%></p>
                    <p>Plaats - Beschrijving</p>                    
                    <!--met foto-->
                    <h2>Foto's</h2>
                    <br />
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                    <a href="images/foto1.jpg" data-lightbox="news">
                        <img src="images/foto1.jpg" alt="foto"/></a>
                        <%}%>
                </article>
                <article class="advice">
                    <% if (item != null) {%>
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
                        <li><%= ad.getDescription()%></li>
                            <%}
                                    }%>
                    </ul>
                    <%} else {%>
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