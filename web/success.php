<!DOCTYPE html>
<?php
    session_start();
    if(!isset($_SESSION['logged in']) || $_SESSION['logged in']==false){
        header("Location: index.php");
        unset($_SESSION);
    }
?>
<html>
    <head>
        <title>name</title>
        <meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>
        <link href="main.css" type="text/css" rel="stylesheet">
    </head>
    <body>
        <!--<script src="https://sdk.amazonaws.com/js/aws-sdk-2.149.0.min.js"></script> -->
        <div class='first' style='width:100vw; height:100vh;'>
            <div class='logout'>
                <a href="logout.php">Logout</a>
            </div> 
            <div class='inside'style='width:100vw; height:100vh;'>
                <H1>
                    PHONE FINDER
                </H1>
                <div class="options">
                <a href="index.php#phone">PHONE</a>
                <a href="index.php#person">PERSON</a>
                </div>
            </div>   
        </div>
          
        
        <div id='phone' style='width:100vw; height:100vh;'>
            <div id='printoutPanel' style='width:20vw;'></div>
            <div id='myMap' style='width: 70vw; height: 100vh;'></div>
            <script type='text/javascript'>
                function loadMapScenario() {
                    var map = new Microsoft.Maps.Map(document.getElementById('myMap'), {});
                    Microsoft.Maps.loadModule('Microsoft.Maps.Search', function () {
                        var searchManager = new Microsoft.Maps.Search.SearchManager(map);
                        var reverseGeocodeRequestOptions = {
                            location: new Microsoft.Maps.Location(47.640049, -122.129797),
                            callback: function (answer, userData) {
                                map.setView({ bounds: answer.bestView });
                                map.entities.push(new Microsoft.Maps.Pushpin(reverseGeocodeRequestOptions.location));
                                document.getElementById('printoutPanel').innerHTML =
                                    answer.address.formattedAddress;
                            }
                        };
                        searchManager.reverseGeocode(reverseGeocodeRequestOptions);
                    });


                }
            </script>
            <script type='text/javascript' src='https://www.bing.com/api/maps/mapcontrol?key=AnvC-Jaa74flVw59lfkM6r0TSQgC4XSSxRRoW2Z6lW5yJv0hTZUue0cvEjZ9RMDD&callback=loadMapScenario' async defer></script>
        </div>
        <div id='person' style='width:100vw; height:100vh;'>
            <h1>person person person</h1>
        </div>
        
    </body>
</html>