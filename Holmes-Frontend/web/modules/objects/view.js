'use strict';

function get_all_objects(){
    $.ajax({
        type: 'POST',
        url: current_env.get('api_url'),
        processData: false,
        contentType: 'application/json',
        data: JSON.stringify({
            module: "objects",
            action: "getAll",
        }),
        success: function(r) {
            if(r.error != ""){
                $.growl.warning({ title: "An error occured!", message: r.error, size: 'large' });
            } else {
                let tbody =  $('#object-table > tbody');
                tbody.empty();
                let cnt = 1;
                let sha256 = "";
                $.each(r.result.Object, function (key, object) {
                    tbody.append('<tr>');
                    tbody.append("<td>" + cnt + "</td>");
                    cnt++;

                    $.each(object, function(k, v){
                        if (k === "sha256") {
                            sha256 = v;
                        }
                        tbody.append("<td>" + v + "</td>");
                    });
                    tbody.append("<td><a class=\"nav-link\" href=\"#module=objects&action=get&sha256=" + sha256 + "\">View</a></td>");
                    tbody.append('</tr>');


                    /*$.each(object.submissions, function(k, v){
                        $('#objects-get-form p[name="submissions"]').append('<a href="#module=submissions&action=get&id='+v+'">'+v+'</a>');
                    });*/
                })
                // TODO ANALYZOVANIE OBJEKTOV
                /*analyze_modal_build([[r.result.Object.sha256, r.result.Object.obj_name, r.result.Object.source]]);*/
            }
        },
    });
}
// get object data
get_all_objects();
