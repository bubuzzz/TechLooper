angular.module('Jobs').controller('searchFormController', function ($scope, searchBoxService, jsonValue) {
  searchBoxService.initSearchTextbox($scope);

  $scope.skills = jsonValue.technicalSkill;

  searchBoxService.openSearchForm($(window).height());
  $(window).resize(function () {
    searchBoxService.openSearchForm($(window).height());
  });
  searchBoxService.hightlightSKill();
  searchBoxService.alignButtonSeatch();

  $scope.closeSearchForm = function () {
      var isVideoHide = $("#companyVideoInfor").attr("aria-hidden");
      if ($(".btn-close").is(":visible") && (isVideoHide == undefined || isVideoHide == "true")) {
        $('.btn-close').click();
      }
  };
  $scope.backPage = function(){
    history.back();
    return false;    
  }
});