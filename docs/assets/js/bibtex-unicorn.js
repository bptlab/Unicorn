/**
 * Edit to the original bibtex_js_draw function
 * Bibtex files need to be entered into individual <textarea>'s with class "reference"
 * Generated content replaces the inputted bibtex data in each <textarea>
 * Allows user to write notes/subheadings in between references
 */
$( document ).ready( function bibtex_js_draw_unicorn() {
  var clippy = '<svg height="18" class="octicon octicon-clippy" viewBox="0 0 14 16" version="1.1" width="28" aria-hidden="true"><path fill-rule="evenodd" d="M2 13h4v1H2v-1zm5-6H2v1h5V7zm2 3V8l-3 3 3 3v-2h5v-2H9zM4.5 9H2v1h2.5V9zM2 12h2.5v-1H2v1zm9 1h1v2c-.02.28-.11.52-.3.7-.19.18-.42.28-.7.3H1c-.55 0-1-.45-1-1V4c0-.55.45-1 1-1h3c0-1.11.89-2 2-2 1.11 0 2 .89 2 2h3c.55 0 1 .45 1 1v5h-1V6H1v9h10v-2zM2 5h8c0-.55-.45-1-1-1H8c-.55 0-1-.45-1-1s-.45-1-1-1-1 .45-1 1-.45 1-1 1H3c-.55 0-1 .45-1 1z"></path></svg>';
  var refs = document.querySelectorAll("textarea.reference");
  var bibtexDisplay = new BibtexDisplay();
  refs.forEach(ref => {

  	var reference = document.createElement("div");
    reference.className = "reference";
    bibtexDisplay.displayBibtex($(ref).val(), $(reference) ); // generate formatted content from bibtex-js.js
    reference.querySelector(".bibtex").innerHTML = ref.innerHTML; // add original bibtex content to reference
    $(reference.querySelector(".bibtex")).prepend("<h4>Bibtex</h4> <button class='btn' data-clipboard-text='" + ref.innerHTML + "'>"+ clippy +"</button><hr>");
    if (reference.querySelector(".abstract")) {
      $(reference.querySelector(".abstract")).prepend("<h4>Abstract</h4><hr>");
    }
    $(ref).replaceWith(reference);
    
  });
  document.querySelector(".references").style.display = "block"; // display page content when finished
  new ClipboardJS('.btn');
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


