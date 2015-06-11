techlooper.directive("srAboutYourReport", function ($http, $location, utils, jsonValue, vnwConfigService) {
  return {
    restrict: "E",
    replace: true,
    templateUrl: "modules/salary-report/sr-about-your-report.tem.html",
    link: function (scope, element, attr, ctrl) {
      var campaign = $location.search();
      //if (!$.isEmptyObject(campaign)) {
      //  for (var prop in campaign) {
      //    try {
      //      campaign[prop] = JSON.parse(campaign[prop]);
      //    }
      //    catch (e) {}
      //  }
      //
      //  if (campaign.id) {
      //    $http.get(jsonValue.httpUri.salaryReview + "/" + campaign.id)
      //      .success(function (data, status, headers, config) {
      //        $scope.salaryReview = data;
      //        //$scope.salaryReview.hasCam = !$.isEmptyObject(campaign);
      //        utils.sendNotification(jsonValue.notifications.loaded);
      //      });
      //  }
      //  else {
      //    $scope.salaryReview = campaign;
      //  }
      //}

      var beforeSendSalaryReport = function (salaryReview) {
        var jobLevelIds = salaryReview.jobLevelIds;
        salaryReview.jobLevelIds = jsonValue.jobLevelsMap['' + jobLevelIds].ids;
        return salaryReview;
      }

      var afterSendSalaryReport = function (salaryReview) {
        //if (campaign && campaign.id) {
        //  salaryReview.campaign = campaign.id;
        //}
        //if (campaign && campaign.campaign) {
        //  salaryReview.campaign = campaign.campaign;
        //}
        return salaryReview;
      }

      scope.$watch("state", function (newVal) {
        if (!scope.state.ableCreateNewReport) {
          return;
        }

        var salaryReview = beforeSendSalaryReport($.extend(true, {}, scope.salaryReview));
        utils.sendNotification(jsonValue.notifications.switchScope);

        $http.post(jsonValue.httpUri.salaryReview, salaryReview)
          .success(function (data, status, headers, config) {
            scope.error = {};
            scope.salaryReview = afterSendSalaryReport(data);
            scope.salaryReview.location = vnwConfigService.getLocationText(scope.salaryReview.locationId);
            scope.salaryReport = scope.salaryReview.salaryReport;
            utils.sendNotification(jsonValue.notifications.loaded);
              var p = scope.salaryReport.percentRank * $(window).width()/100;
              if($(window).width() < 480){
                scope.myPosition = (p - 18.5) * 100 / $(window).width();
              }else{
                scope.myPosition = (p - 20) * 100 / $(window).width();
              }
              if (scope.salaryReview.campaign) {
              return;
            }

            //TODO: refactor flag to show/hide sub-views
            var hasCity = jsonValue.companyPromotion.AcceptedCity.indexOf(scope.salaryReview.locationId) >= 0;
            var enoughMoney = (scope.salaryReview.usdToVndRate * scope.salaryReview.netSalary) >= jsonValue.companyPromotion.minSalary;
            var hasDone = localStorage.getItem('PROMOTION-KEY') === 'yes';
            scope.state.showPromotion = hasCity && enoughMoney && !hasDone;
            scope.state.showAskPromotion = scope.state.showPromotion;
            //scope.state.showSendReport = true;
            scope.state.showJobAlert = $.type(scope.salaryReport.percentRank) === "number" && scope.salaryReview.topPaidJobs.length;
          });
      });
    }
  }
});