/**
* Function to retrieve all h2's on current page and add them under the active page
* link in the nav bar as an unordered list
*/
$( document ).ready( function subnav() {
	var h_list = document.querySelectorAll("h2");
	var sublinks = "<ul class='subnav'>"
	h_list.forEach( h => {
		sublinks += "<li><a href='#" + h.id + "'>" + h.innerHTML + "</li>";
	});
	sublinks += "</ul>";
	$(".active").parent().append(sublinks);
}); 