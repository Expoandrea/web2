/**
 * Restituisce un elemento HTML con il colore di sfondo appropriato.
 * 
 * @param {string} statoOrdine - Lo stato dell'ordine.
 * @return {string} L'elemento HTML con il colore di sfondo appropriato.
 */
function cambiaStatoDettaglioOrdine(statoOrdine) {
    let backgroundColor;

    switch (statoOrdine) {
        case 'RESPINTO_NON_CONFORME':
            backgroundColor = '#aad4fc';
            break;
        case 'RESPINTO_NON_FUNZIONANTE':
            backgroundColor = '#aad4fc';
            break;
        case 'IN_ATTESA':
            backgroundColor = '#83b686';
            break;
        case 'ACCETTATO':
            backgroundColor = '#ffc400';
            break;
        case 'RIFIUTATO':
            backgroundColor = '#FFAEAE';
            break;
        default:            
        backgroundColor = "#FFFFFF";
}


return `
<div class="badge-statoOrdini w-auto py-2 px-3 rounded-md text-base font-semibold text-center" style="background-color: ${backgroundColor};">
  ${statoOrdine}  
</div> 
`;
}


document.addEventListener("DOMContentLoaded", function() {
    /**
     * Elemento HTML che rappresenta lo stato dell'ordine.
     * @type {HTMLElement}
     */
    const ordine = document.querySelector('.badge-statoOrdini[data-stato]');
    if (ordine) {
        /**
         * Stato dell'ordine ottenuto dall'attributo stato dell'elemento.
         * @type {string}
         */
        const statoOrdine = ordine.getAttribute('data-stato');
        console.log('Stato ordine:', statoOrdine); // Verifica il valore
        console.log('Background color:', cambiaStatoDettaglioOrdine(statoOrdine));
        ordine.outerHTML = cambiaStatoDettaglioOrdine(statoOrdine);
    }
});