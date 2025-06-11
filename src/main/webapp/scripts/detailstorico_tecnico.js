document.addEventListener("DOMContentLoaded", function() {
    /**
     * Elemento HTML che rappresenta il badge dello stato.
     * @type {HTMLElement}
     */
    var statoBadge = document.getElementById("statoBadge");

    /**
     * Testo dello stato ottenuto dal contenuto del badge.
     * @type {string}
     */
    var stato = statoBadge.textContent.trim();

    /**
     * Cambia il colore di sfondo del badge in base allo stato.
     */
    switch(stato) {
        case 'RESPINTO_NON_CONFORME':
            statoBadge.style.backgroundColor = '#aad4fc';
            break;

        case 'RESPINTO_NON_FUNZIONANTE':
            statoBadge.style.backgroundColor = '#aad4fc';
            break;

        case 'IN_ATTESA':
            statoBadge.style.backgroundColor = '#83b686';
            break;

        case 'RIFIUTATO':
            statoBadge.style.backgroundColor = '#FFAEAE';
            break;

        case 'ACCETTATO':
            statoBadge.style.backgroundColor = '#ffc400';
            break;
        
        default:
            statoBadge.style.backgroundColor = '#979dac';
    }
});