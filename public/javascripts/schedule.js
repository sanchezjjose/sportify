$(document).ready(function () {

  function init () {
    var seasonId = $('.season.regular').attr('id');
    var teamId = $('.main').attr('id');

    // rsvp for a game
    $('.schedule > .btn').click(function (ev) {
      ev.preventDefault();
      $.ajax({
          url: $(this).attr('href'),
          success: function (data) {
              $('.alert').remove();
              if (data.status === 'in') {
                $('.season:first-child').before('<div class="game-status-msg alert alert-info">'+data.msg+'</div>');
              } else {
                $('.season:first-child').before('<div class="game-status-msg alert alert-danger" >'+data.msg+'</div>');
                $('.game-status-msg').addClass('alert-danger');
              }
              window.scrollTo(0, 0);
              setTimeout(function clearMsg () {
                  $('.alert').fadeOut('1000', function (el) {
                      $(el).remove();
                  });
              }, 5000);
          }
      });
    });

    function displayAddGameModal(isPlayoffs) {

      // clear inputs
      $('form input').val("")

      $('#new-game form').get(0).setAttribute('action', '/team/' + teamId + '/schedule/save/' + seasonId + '/' + isPlayoffs);
      $('#new-game form').get(0).setAttribute('method', 'post');

      // open modal
      window.location = "#new-game";
    }

    // open add new game modal
    $('.regular .add').click(function (ev) {
      ev.preventDefault();
      displayAddGameModal(false);
    });

    // open add new game modal
    $('.playoffs .add').click(function (ev) {
      ev.preventDefault();
      displayAddGameModal(true);
    });

    // open and set fields for the edit game modal
    $('.regular .edit').click(function (ev) {
      ev.preventDefault();
      $.ajax({
        url: $(this).attr('href'),
        success: function (data) {
          $(".number input").val(data.number);
          $(".start_time input").val(data.start_time);
          $(".address input").val(data.address);
          $(".gym input").val(data.gym);
          $(".location_details input").val(data.location_details);
          $(".opponent input").val(data.opponent);
          $(".result input").val(data.result);

          $('#edit-game form').get(0).setAttribute('action', '/team/' + data.team_id + '/schedule/update/' + data.game_id + '/' + false);
          $('#edit-game form').get(0).setAttribute('method', 'post');

          // open and fill form
          window.location = "#edit-game";
        }
      });
    });

    // open and set fields for the edit game modal
    $('.playoffs .edit').click(function (ev) {
      ev.preventDefault();
      $.ajax({
        url: $(this).attr('href'),
        success: function (data) {
          $(".number input").val(data.number);
          $(".start_time input").val(data.start_time);
          $(".address input").val(data.address);
          $(".gym input").val(data.gym);
          $(".location_details input").val(data.location_details);
          $(".opponent input").val(data.opponent);
          $(".result input").val(data.result);

          $('#edit-game form').get(0).setAttribute('action', '/team/' + data.team_id + '/schedule/update/' + data.game_id + '/' + true);
          $('#edit-game form').get(0).setAttribute('method', 'post');

          // open and fill form
          window.location = "#edit-game";
        }
      });
    });
  }

  init();
});

function deleteGame(teamId, seasonId, gameId) {
  var shouldDelete = confirm('Are you sure you want to delete this game?');
  if (shouldDelete) {
    window.location = "/team/" + teamId + "/schedule/delete/" + seasonId + "/" + gameId;
  }
};
