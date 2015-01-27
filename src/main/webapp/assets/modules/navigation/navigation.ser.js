angular.module("Navigation").factory("navigationService", function (localStorageService, utils, jsonValue, $rootScope, $http, $location, tourService, userService) {

  var $$ = {
    naviControl: function () {
      //localStorageService.set(jsonValue.storage.navigation, 'close');
      $('.manager-navi').find('.fa-bars').on('tap click', function () {
        if ($(this).hasClass('active')) {
          $('.main-navi-block').animate({
            width: '0px'
          }, 300, function () {
            $(this).css('position', 'relative');
          });
          $('.techlooper-body').animate({
            'padding-left': 0
          });
          $('.sub-page-header').animate({
            'padding-left': '90px'
          });
          $('.navi-container').animate({
            'width': '0%'
          }, 300, function () {
            $(this).css('display', 'none');
          });

          $(this).removeClass('active');
          localStorageService.set(jsonValue.storage.navigation, 'close');
        }
        else {
          var view = utils.getView();
          if(view == jsonValue.views.jobsSearch){
            $('.main-navi-block').css('background','url(images/line-h1.png) #ccc right top repeat-y');
          }
          $('.main-navi-block').animate({
            width: '85px'
          }).css('position', 'fixed');
          $('.techlooper-body').animate({
            'padding-left': '85px'
          });
          $('.sub-page-header').animate({
            'padding-left': '20px'
          });
          $('.navi-container').animate({
            'width': '100%'
          }).css('display', 'block');
          $(this).addClass('active');
          localStorageService.set(jsonValue.storage.navigation, 'open');
        }
      });
    },

    updateChartButton: function () {
      switch (utils.getView()) {
        case jsonValue.views.pieChart:
          $(".m-chart").removeClass('m-pie-chart').addClass('m-bubble-chart');
            //.attr('href', "#" + jsonValue.routerUris.bubble);
          break;

        default:
          $('.m-chart').removeClass('m-bubble-chart').addClass('m-pie-chart');
            //.attr('href', "#" + jsonValue.routerUris.pie);
          break;
      }
    },

    registerEventListeners: function () {
      $(".m-chart").on("click tap", function() {
        $location.path($('.m-chart').hasClass("m-pie-chart") ? jsonValue.routerUris.pie : jsonValue.routerUris.bubble);
        $$.updateChartButton();
      });

      $("a.sign-out-sign-in").on("click tap", function () {
        if (userService.notLoggedIn()) {
          $location.path(jsonValue.routerUris.signIn);
        }
        else {
          $http.get(jsonValue.httpUri.logout).success(function () {
            utils.sendNotification(jsonValue.notifications.logoutSuccess);
          });
        }
      });
    },
    updateHighlight: function(){
      $('.navi-container').find('li').removeClass('active');
      switch (utils.getView()) {
        case jsonValue.views.pieChart:
          $('.navi-container').find('a.m-chart').parent().addClass('active');
          break;
        case jsonValue.views.bubbleChart:
          $('.navi-container').find('a.m-chart').parent().addClass('active');
          break;
        case jsonValue.views.jobsSearch:
          $('.navi-container').find('a.m-search-jobs').parent().addClass('active');
          $('.main-navi-block').css('background','url(images/line-h1.png) #ccc right top repeat-y');
          break;
        case jsonValue.views.jobsSearchText:
          $('.navi-container').find('a.m-search-jobs').parent().addClass('active');
          $('.main-navi-block').css('background','url(images/line-h1.png) #ccc right top repeat-y');
          break;
        case jsonValue.views.signIn:
          $('.navi-container').find('a.sign-out-sign-in').parent().addClass('active');
          break;
        default:
          $('.navi-container').find('a.m-chart').parent().addClass('active');
          break;
      }
      var menuItem = $('.navi-container').find('li a');
      menuItem.on('click', function(){
        //utils.hideNavigationBar();
        if(!$(this).hasClass('m-languages')){
          menuItem.parent().removeClass('active');
          $(this).parent().addClass('active');
          if($(this).hasClass('m-search-jobs')){
            $('.main-navi-block').css('background','url(images/line-h1.png) #ccc right top repeat-y');
          }else{
            $('.main-navi-block').css('background','url(images/line-h.png) #000 right top repeat-y');
            $('body').css('background-color','#201d1e');
          }
          if($(this).hasClass('m-sign-out')){
            $('.navi-container').find('a.sign-out-sign-in').parent().removeClass('active');
            $('.navi-container').find('a.m-chart').parent().addClass('active');
          }
          $('.techlooper-body').animate({
            'padding-left': '85px'
          });
          $('.sub-page-header').animate({
            'padding-left': '20px'
          });
        }
      });
    }
  }
  var instance = {
    initialize: function () {
      $$.updateChartButton();
      $$.updateHighlight();
      $$.registerEventListeners();
      $$.naviControl();
    },
    addSpaceforNavi: function(){
      var page = $('.techlooper-body');
      if($('.main-navi-block').width() == 85){
        page.css('padding-left', '85px');
        $('.sub-page-header').css('padding-left', '20px');
      }else{
        page.css('padding-left', '0');
        $('.sub-page-header').css('padding-left', '90px');
      }
    },
    reSetingPositionLangIcon: function(){
      if($('.navbar').length > 0){
        $('.languages-block').css('right','0');
      }else{
        $('.languages-block').css('right','55px');
      }
    },
    restoreNaviStyle: function(){
      $('.navi-container').find('a.m-chart').parent().addClass('active');
      $('.navi-container').find('a.m-search-jobs').parent().removeClass('active');
      $('.main-navi-block').css('background','url(images/line-h.png) #000 right top repeat-y');
    },
    keepNaviBar: function(){
      var status = localStorageService.get(jsonValue.storage.navigation);
      if(status == 'open'){
        $('.main-navi-block').css({
          'width': '85px',
          'position': 'fixed'
        });
        $('.navi-container').css({'width': '100%', 'display': 'block'});
        $('.manager-navi').find('.fa-bars').addClass('active');
        instance.addSpaceforNavi();
      }
    },
    backtoSearchPage: function(mItem){
      var view = utils.getView();
      if(view == jsonValue.views.jobsSearch || view == jsonValue.views.jobsSearchText){
        $('.navi-container').find('li').removeClass('active');
        $('.navi-container').find('a.'+ mItem).parent().addClass('active');
        $('.main-navi-block').css('background','url(images/line-h1.png) #ccc right top repeat-y');
      }else{
        $('.navi-container').find('a.'+ mItem).parent().removeClass('active');
        $('.navi-container').find('a.m-chart').parent().addClass('active');
        $('.main-navi-block').css('background','url(images/line-h.png) #000 right top repeat-y');
      }
    }
  }

  return instance;

});