/**
 * Edit to the original bibtex_js_draw function
 * Bibtex files need to be entered into individual <textarea>'s with class "reference"
 * Generated content replaces the inputted bibtex data in each <textarea>
 * Allows user to write notes/subheadings in between references
 */
$( document ).ready( function bibtex_js_draw_unicorn() {
  var refs = document.querySelectorAll("textarea.reference");
  var bibtexDisplay = new BibtexDisplay();
  refs.forEach(ref => {

  	var reference = document.createElement("div");
    reference.className = "reference";
    bibtexDisplay.displayBibtex($(ref).val(), $(reference) ); // generate formatted content from bibtex-js.js
    reference.querySelector(".bibtex").innerHTML = ref.innerHTML; // add original bibtex content to reference
    $(reference.querySelector(".bibtex")).prepend("<h4>Bibtex</h4><hr>");
    if (reference.querySelector(".abstract")) {
      $(reference.querySelector(".abstract")).prepend("<h4>Abstract</h4><hr>");
    }
    $(ref).replaceWith(reference);
    
  });
  document.querySelector(".references").style.display = "block"; // display page content when finished
});

/**
 * Toggles the display of the abstract or bibtex below each reference
 */
function toggleMore(element,selector) {
	if (element && selector) {
		var ele = element.parentNode.querySelector(selector);
 		ele.style.display = (ele.style.display == "none") ? "block" : "none";
	}
 }
