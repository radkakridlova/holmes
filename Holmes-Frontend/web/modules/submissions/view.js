'use strict';

function get_submissions(){
	$.ajax({
		type: 'POST',
		url: current_env.get('api_url'),
		processData: false,
		contentType: 'application/json',
		data: JSON.stringify({
			module: "submissions",
			action: "getAll",
		}),
		success: function(r) {
			if(r.error != ""){
				$.growl.warning({ title: "An error occured!", message: r.error, size: 'large' });
			} else {
				let tbody =  $('#submissions-table > tbody');
				tbody.empty();
				let cnt = 1;
				let id = "";
				let sha256 = "";
				$.each(r.result.Submission, function (key, submission) {
					tbody.append('<tr>');
					tbody.append("<td>" + cnt + "</td>");
					cnt++;

					$.each(submission, function (k, v) {
						if (k === "id") {
							id = v; // this is here so we can view detail of submission
						}
						if (k === "sha256" || k === "date_time" || k === "obj_name") {
							if (k === "sha256") {
								sha256 = v;
							}
							tbody.append("<td>" + v + "</td>");
						}
					});
					tbody.append("<td><a class=\"nav-link\" href=\"#module=submissions&action=get&id=" + id + "&sha256=" + sha256 + "\">View</a></td>");
					tbody.append('</tr>');
				});
			}
		},
	});
}

get_submissions();
