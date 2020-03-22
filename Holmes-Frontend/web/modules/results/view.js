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
				// $.each(r.result.Result, function(k, v){
				// 	$('#results-get-form input[name="'+k+'"]').val(v);
				// });
				//
				// //$('#results-get-form textarea[name="results"]').val(r.result.Result.results);
				//
				// $('#results-get-form pre[name="results"]').html(syntaxHighlight(JSON.stringify(JSON.parse(r.result.Result.results), undefined, 4)));
				//
				// $('#results-get-form p[name="sha256"]').append('<a href="#module=objects&action=get&sha256='+r.result.Result.sha256+'">'+r.result.Result.sha256+'</a>');
				//
				// try {
				// 	render_result = undefined;
				// 	$.getScript( "modules/results/services/"+r.result.Result.service_name+".js" );
				// 	render_result(r.result.Result);
				// } catch(err) {
				// 	console.log(err);
				// 	console.log("No special renderer found.");
				// 	console.log("modules/results/services/"+r.result.Result.service_name+".js");
				// }
				//
			}
		},
	});
}

function syntaxHighlight(json) {
	json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
	return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
		var cls = 'JSONnumber';
		if (/^"/.test(match)) {
			if (/:$/.test(match)) {
				cls = 'JSONkey';
			} else {
				cls = 'JSONstring';
			}
		} else if (/true|false/.test(match)) {
			cls = 'JSONboolean';
		} else if (/null/.test(match)) {
			cls = 'JSONnull';
		}
		return '<span class="' + cls + '">' + match + '</span>';
	});
}

function render_result(j) {
	//dummy to fix "var not declared"
}

get_results();
