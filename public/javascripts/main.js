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