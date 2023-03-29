'use strict';

// Get all results
function get_results(){
	$.ajax({
		type: 'POST',
		url: current_env.get('api_url'),
		processData: false,
		contentType: 'application/json',
		data: JSON.stringify({
			module: "results",
			action: "getAll",
		}),
		success: function(r) {
			if(r.error != ""){
				$.growl.warning({ title: "An error occured!", message: r.error, size: 'large' });
			} else {
				let tbody =  $('#results-table > tbody');
				tbody.empty();
				let cnt = 1;
				let id = "";
				let service_name = "";
				let object_type = "";
				$.each(r.result.Result, function (key, result) {
					tbody.append('<tr>');
					tbody.append("<td>" + cnt + "</td>");
					cnt++;

					$.each(result, function (k, v) {
						if (k === "id") {
							id = v; // this is here so we can view detail of result
						}
						if (k === "sha256" || k === "source_id" || k === "service_name" || k === "service_version" || k === "execution_time" || k === "object_type") {
							if (k === "service_name") {
								service_name = v;
							} else if (k === "object_type") {
								object_type = v;
							}
							tbody.append("<td>" + v + "</td>");
						}
					});
					tbody.append("<td><a class=\"nav-link\" href=\"#module=results&action=get&service_name=" + service_name +"&object_type=" + object_type + "&id=" + id + "\">View</a></td>");
					tbody.append('</tr>');
				});
			}
		},
	});
}

get_results();