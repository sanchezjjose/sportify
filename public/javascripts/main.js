
// http://appendto.com/2010/10/how-good-c-habits-can-encourage-bad-javascript-habits-part-1/
(function( Sportify, $, undefined ) {

    //Private Property
    var isHot = true;

    //Public Property
    Sportify.user = "Bacon Strips";

    //Public Method
    Sportify.fry = function() {
        var oliveOil;

        addItem( "\t\n Butter \n\t" );
        addItem( oliveOil );
        console.log( "Frying " + Sportify.ingredient );
    };

    //Private Method
    function addItem( item ) {
        if ( item !== undefined ) {
            console.log( "Adding " + $.trim(item) );
        }
    }
}( window.Sportify = window.Sportify || {}, jQuery ));

// window.fbAsyncInit = function() {

//   FB.init({
//       appId      : @Config.fbAppId,
//       channelUrl : '//@routes.Assets.at("javascripts/channel.html")',
//       status     : true,
//       cookie     : true,
//       xfbml      : true
//   });

//   FB.Event.subscribe('auth.authResponseChange', function(response) {
//     var button = document.getElementById('btn-logout');
//     if (response.status === 'connected') {
//       button.onclick = function() {
//         console.log("Logging out of facebook now...")
//         FB.logout(function(response) {
//           Log.info('FB.logout callback', response);
//         });
//       };
//     }
//   });
// };

// // Load the SDK asynchronously
// (function(d){
//   var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
//   if (d.getElementById(id)) {return;}
//   js = d.createElement('script'); js.id = id; js.async = true;
//   js.src = "//connect.facebook.net/en_US/all.js";
//   ref.parentNode.insertBefore(js, ref);
// }(document));


// $("#create-fb-event").on('click', function() {
//   var href = "/home/facebook/event/" + @game._id;

//   window.location = href;
// });