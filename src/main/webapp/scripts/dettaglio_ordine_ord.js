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
            backgroundColor = '#FFAEAE';
            break;
        case 'RESPINTO_NON_FUNZIONANTE':
            backgroundColor = '#ffbf70';
            break;
        case 'IN_ATTESA':
            backgroundColor = '#FFE8A3';
            break;
        case 'ACCETTATO':
            backgroundColor = '#AFF4C6';
            break;
        default:            
        backgroundColor = "#FFFFFF";
}


return `
<div class="badge-statoOrdini w-1/5 py-2 px-3 rounded-md text-base font-semibold text-center" style="background-color: ${backgroundColor};">
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