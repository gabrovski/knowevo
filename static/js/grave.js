
function inputFocus(i) {
    if (i.value == i.defaultValue) {
	i.value = "";
	i.style.color = "#000";
    }
}

function inputBlur(i) {
    if (i.value == "") {
	i.value = i.defaultValue;
	i.style.color = "#888";
    }
}

function searchSubmit(form) {
    var inp = form.title_inp.value;
    //$('#res_container').load(inp);
    var dataString = 'title_inp='+inp;
    try {
    $.ajax({  
	type: "POST",  
	url: "",  
	data: dataString,  
	headers: {'X-CSRFToken': csrf_token},
	success: function(data) {  
	    $('#res_container').html(data).fadeIn('slow');  
	}
    });
    }
    catch (err) {
	alert(err.message);
    }
}
	

