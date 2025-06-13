function cambiaStatoDettaglioRichiesta(statoRichiesta) {
    let backgroundColor;

    switch (statoRichiesta) {
        case 'IN_ATTESA':
            backgroundColor = '#83b686'; 
            break;
        case 'PRESA_IN_CARICO':
            backgroundColor = '#aad4fc'; 
            break;
        case 'RISOLTA':
            backgroundColor = '#FFAEAE'; 
            break;
        case 'ORDINATA':
            backgroundColor = '#ffc400'; 
            break;
        default:            
        backgroundColor = "#FFFFFF";
}
return `

            <div class="badge-stato w-auto py-2 px-3 rounded-md text-base font-semibold text-center" style="background-color: ${backgroundColor}!important;">
                ${statoRichiesta}
            </div> 
`;
}


document.addEventListener("DOMContentLoaded", function() {
    const richiesta = document.querySelector('.badge-stato[data-stato]');
    if (richiesta) {
        const statoRichiesta = richiesta.getAttribute('data-stato');
        console.log('Stato della richiesta:', statoRichiesta); // Verifica il valore
        console.log('Background color:', cambiaStatoDettaglioRichiesta(statoRichiesta));
        richiesta.outerHTML = cambiaStatoDettaglioRichiesta(statoRichiesta);
    }
});