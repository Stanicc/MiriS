(function ($) {
    "use strict";

    $(window).on('load', function () {
        if ($('#loading').length) {
            $('#loading').delay(3000).fadeOut('slow', function () {
                $(this).remove();
            });
        }
    });

})(jQuery);