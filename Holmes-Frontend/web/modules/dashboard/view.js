'use strict';

$("form#upload-form").submit(function(e) {
	e.preventDefault();
	var formData = new FormData(this);

	$.ajax({
	    url: "/upload",
	    type: 'POST',
	    data: formData,
	    success: function (data) {
			//console.log(data);
	    },
	    cache: false,
	    contentType: false,
	    processData: false
	});
});

function submit_form(){
    //console.log("submit_form called");
    $("form#upload-form").submit();
}
