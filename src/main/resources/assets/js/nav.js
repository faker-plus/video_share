$(function () {
    // Nav contraction unfolding
    $('.jq-nav-item>a').on('click', function () {
        if (!$('.jq-nav').hasClass('jq-nav-mini')) {
            if ($(this).next().css('display') === "none") {
                //Unfold Unfold
                $('.jq-nav-item').children('ul').slideUp(300);
                $(this).next('ul').slideDown(300);
                $(this).parent('li').addClass('jq-nav-show').siblings('li').removeClass('jq-nav-show');
            } else {
                //Shrink expanded
                $(this).next('ul').slideUp(300);
                $('.jq-nav-item.jq-nav-show').removeClass('jq-nav-show');
            }
        }
        var tab = $(this).attr("href");
        if (tab !== "filemanager") {
            $(".jq-tab").addClass("hide");
            $(tab).removeClass("hide");
            try {
                getTabInfo(tab);
                if (isMobile()) {
                    $("#mini").click();
                }
            } catch (e) {
                console.info("function 'getTabInfo' not found");
            }
        }
    });
    //nav-Mini switching
    $('#mini').on('click', function () {
        var nav = $(".jq-nav");
        if ($(nav).hasClass('jq-nav-mini')) {
            $(nav).removeClass('jq-nav-mini');
        } else {
            $('.jq-nav-item.jq-nav-show').removeClass('jq-nav-show');
            $('.jq-nav-item').children('ul').removeAttr('style');
            $(nav).addClass('jq-nav-mini');
        }
        calculateContentWidth();
    });
});

calculateContentWidth();

function calculateContentWidth() {
    setTimeout(function () {
        $("#container").css("margin-left", $(".jq-nav").width() + "px");
    }, 200);
}