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

<%@page import="Shared.Data.INewsItem"%>
<%@page import="Controller.webController"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>CIMS 112 Nieuws - <decorator:title/></title>
        <link href="style.css" rel="stylesheet" type="text/css" />
	<link href="lightbox/css/lightbox.css" rel="stylesheet" />
        
        <%
            String ID = request.getParameter("newsid");
            String location = "";
            
            if(ID != null && !ID.isEmpty()) {
                webController controller = new webController();
                INewsItem item = null;
                try {
                    item = controller.getNewsWithID(ID);
                } catch (Exception ex) {
                    item = null;
                }

                if(item != null) {
                    if (!item.getStreet().equals("") && !item.getCity().equals("")) {
                        location = item.getLocation();
                    }
                    else if (!item.getCity().equals("")) {
                        location = item.getCity();
                    }
                }
            }
        %>

        <script type='text/javascript'  src="lightbox/js/jquery-1.11.0.min.js"></script>
        <script type='text/javascript'  src="lightbox/js/lightbox.min.js"></script>

        <!--Google maps link-->
        <script  type='text/javascript'  src="https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false"></script>
        <script type='text/javascript' >
            function loadMaps() {
                <% if(ID != null && !ID.isEmpty()) { %>
                    geocoder = new google.maps.Geocoder();
                    var mapCanvas = document.getElementById('mapcanvas');
                    var mapOptions = {
                        zoom: 14,
                        mapTypeId: google.maps.MapTypeId.ROADMAP
                    };
                    var map = new google.maps.Map(mapCanvas, mapOptions);

                    var addressFromDB = "<%= location %>";
                    var address = addressFromDB + ", Nederland";
                    geocoder.geocode({'address': address}, function (results, status) {
                        if (status === google.maps.GeocoderStatus.OK) {
                            map.setCenter(results[0].geometry.location);

                            //Add location mark                            
                            var marker = new google.maps.Marker({
                                map: map,
                                position: results[0].geometry.location
                            });
                            
                            <% if(!location.equals("")) { %>
                                var addressinfo = addressFromDB;
                                <% if(location.contains(",")) { %>
                                    addressinfo = addressFromDB.replace(",", "<br />");
                                <% } %>
                                infowindow = new google.maps.InfoWindow({content: addressinfo});                                 
                                google.maps.event.addListener(marker, "click", function () {
                                    infowindow.open(map, marker);
                                });
                                infowindow.open(map, marker);
                            <% } %>
                        } else {
                            alert("Geocode was not successful for the following reason: " + status);
                        }
                    });
                <% } %>
            }
        </script>
    </head>
    <body onload="loadMaps()">
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
