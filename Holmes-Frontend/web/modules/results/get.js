'use strict';

function get_result(service_name, object_type, id){
	$.ajax({
		type: 'POST',
		url: current_env.get('api_url'),
		processData: false,
		contentType: 'application/json',
		data: JSON.stringify({
			module: "results",
			action: "get",
			parameters: {"service_name": service_name, "object_type": object_type, "id":id}
		}),
		success: function(r) {
			if(r.error != ""){
				$.growl.warning({ title: "An error occured!", message: r.error, size: 'large' });
			} else {
				$.each(r.result.Result, function(k, v){
					$('#results-get-form input[name="'+k+'"]').val(v);
				});

				//$('#results-get-form textarea[id="results"]').append(r.result.RawResults);
				$('#results-get-form p[id="results"]').append(JSON.stringify(JSON.parse(r.result.RawResults), undefined, 4));

				//$('#results-get-form pre[name="results"]').html(syntaxHighlight(JSON.stringify(JSON.parse(r.result.Result.results), undefined, 4)));

				$('#results-get-form p[name="sha256"]').append('<a href="#module=objects&action=get&sha256='+r.result.Result.sha256+'">'+r.result.Result.sha256+'</a>');
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


function download_results(service_name, object_type, id){
	$.ajax({
		type: 'POST',
		url: current_env.get('api_url'),
		processData: false,
		contentType: 'application/json',
		data: JSON.stringify({
			module: "results",
			action: "download",
			parameters: {"service_name": service_name, "object_type": object_type, "id":id}
		}),
		success: function(r) {
			//console.log(r.result);
			if(r.error != ""){
				$.growl.warning({ title: "An error occured!", message: r.error, size: 'large' });
			} else {
				var link = document.createElement("a");
				link.download = service_name + "_report";
				link.target = "_blank";

				link.href = 'data:application/json;charset=utf-8,'+r.result.Results;
				document.body.appendChild(link);
				link.click();

				document.body.removeChild(link);
			}
		},
	});
}

function render_result(j) {
	//dummy to fix "var not declared"
}

get_result(current_env.get('url_hash').get('service_name'), current_env.get('url_hash').get('object_type'), current_env.get('url_hash').get('id'));

// bind functions to buttons
$('#results-get-btn-download').on('click', function(){
	download_results(current_env.get('url_hash').get('service_name'), current_env.get('url_hash').get('object_type'), current_env.get('url_hash').get('id'));
});
