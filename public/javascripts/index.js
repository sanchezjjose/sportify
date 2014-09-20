function initialize() {
  var gameAddress = $('#map-canvas').data('game-address');
  var directionsDisplay;
  var map;
  var directionsService = new google.maps.DirectionsService();
  var manhattan = new google.maps.LatLng(40.7711329, -73.9741874);

  directionsDisplay = new google.maps.DirectionsRenderer();
  var mapOptions = {
    zoom: 14,
    center: manhattan,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  }
  map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
  directionsDisplay.setMap(map);

  var request = {
    origin: '2 Park Ave, New York',
    destination: gameAddress + ', New York',
    travelMode: google.maps.DirectionsTravelMode.TRANSIT
  };

  directionsService.route(request, function(response, status) {
    if (status == google.maps.DirectionsStatus.OK) {
      directionsDisplay.setDirections(response);
    }
  });
}

function loadScript() {
  var script = document.createElement("script");
  script.type = "text/javascript";
  script.src = "http://maps.googleapis.com/maps/api/js?sensor=false&callback=initialize";
  document.body.appendChild(script);
}

window.onload = loadScript;