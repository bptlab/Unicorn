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
    bibtexDisplay.displayBibtex($(ref).val(), $("#bibtex_display") );
    $(ref).replaceWith( $("#bibtex_display").html() );
  });
  document.querySelector(".references").style.display = "block"; // display page content when finished
});

/**
 * Function to toggle the display of each bibtex abstract
 */
function toggleAbstract(element) {
  var abstractElement = element.querySelector(".abstract");
  abstractElement.style.display = (abstractElement.style.display == "none") ? "block" : "none";
 }

