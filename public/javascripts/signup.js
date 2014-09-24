/** @jsx React.DOM */
// React.renderComponent(
//   <h4>Choose A Sport</h4>,
//   document.getElementById('example')
// );

$(document).ready(function () {

  $('#sport').change(function() {
    if ( $(this).val() === "Basketball" ) {
      $('#position_field').removeClass("hidden");
    } else {
      $('#position_field').addClass("hidden");
    }
  });

});