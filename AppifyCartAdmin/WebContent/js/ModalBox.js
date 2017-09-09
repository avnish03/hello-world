// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];
var modalId ;
function ShowHelpBox(modal){
	modalId = modal ;
	modalId.style.display = "block";
}
function CloseModal(modal){
	modal.style.display = "none";
}
// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
    if (event.target == modalId) {
        modalId.style.display = "none";
    }
}