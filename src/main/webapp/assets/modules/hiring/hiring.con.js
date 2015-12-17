techlooper.controller('hiringController', function ($scope, $timeout) {
  $scope.checkingGoogle =  function(){
    ga("send", {
      hitType: "event",
      eventCategory: "onlinecontest",
      eventAction: "click",
      eventLabel: "postnowbtn"
    });
  }
  $timeout(function(){
    var tallest = 0;
    $('.hiring-main-feature-item').each(function () {
      var thisHeight = $(this).height();
      if (thisHeight > tallest)
        tallest = thisHeight;
    });
    $('.hiring-main-feature-item').height(tallest + $('.cta-button').height() + 10);
  }, 100);
  if(localStorage.NG_TRANSLATE_LANG_KEY == 'vi'){
    $('.hiring-how-does-work-content').find('.hiring').addClass('vi');
  }else{
    $('.hiring-how-does-work-content').find('.hiring').removeClass('vi');
  }
  $scope.gotoOtherPage  = function(eventCategory, eventLabel, url){
    ga("send", {
      hitType: "event",
      eventCategory: eventCategory,
      eventAction: "click",
      eventLabel: eventLabel
    });
    window.location.href = url;
  }
});