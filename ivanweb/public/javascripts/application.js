// Place your application-specific JavaScript functions and classes here
// This file is automatically included by javascript_include_tag :defaults

function toggleGuestSelection() {
    var restrictedField = $('game_restricted');
    var guestSelection = $('guest-selection');
    
    if( restrictedField.checked == true ) {           
        guestSelection.show();
    }
    else {
        guestSelection.hide();
    }
}

function addNameToGuestList() {
    var playerList = $("player-list");
    
    if( playerList.selectedIndex != -1 ) {
        var selectedPlayer = playerList.options[playerList.selectedIndex];
        var guestList = $("guest-list");
        
        // make sure this player isn't already on the list
        for( var i=0; i < guestList.options.length; i++ ) {
            var option = guestList.options[i];
            if( option.value == selectedPlayer.value ) return;
        }
        
        var option = document.createElement('option');
        option.text = selectedPlayer.text;
        option.value = selectedPlayer.value;
        guestList.options[guestList.options.length] = option;
        
        var codes = readGuestCodes();
        codes[codes.length] = selectedPlayer.value;
        $('game_guest_codes').value = codes.toJSON();         
    }
}

function readGuestCodes() {
    var codes = $('game_guest_codes').value;
    return (codes == null || codes.length == 0) ? [] : codes.evalJSON();
}

function removeNameFromGuestList() {
    var guestList = $("guest-list");
    
    if( guestList.selectedIndex != -1 ) {
        var selectedPlayer = guestList.options[guestList.selectedIndex];
        guestList.selectedIndex = -1;
        guestList.removeChild(selectedPlayer);
        var codes = readGuestCodes();
        codes = codes.without(selectedPlayer.value);
        $('game_guest_codes').value = codes.toJSON();         
    }
}
