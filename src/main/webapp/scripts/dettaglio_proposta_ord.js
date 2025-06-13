function cambiaStatoDettaglioProposta(statoProposta) {
    let backgroundColor;

    switch (statoProposta) {
        case 'RIFIUTATO':
            backgroundColor = '#FFAEAE'; 
            break;
        case 'IN_ATTESA':
            backgroundColor = '#83b686'; 
            break;
        case 'ORDINATO':
            backgroundColor = '#aad4fc'; 
            break;
        case 'ACCETTATO':
            backgroundColor = '#ffc400'; 
            break;
        default:            
        backgroundColor = "#FFFFFF";
}
return `

            <div class="badge-stato w-auto py-2 px-3 rounded-md text-base font-semibold text-center" style="background-color: ${backgroundColor}!important;">
                ${statoProposta}
            </div> 
`;
}



document.addEventListener("DOMContentLoaded", function() {
    const proposta = document.querySelector('.badge-stato[data-stato]');
    if (proposta) {
        const statoProposta = proposta.getAttribute('data-stato');
        proposta.outerHTML = cambiaStatoDettaglioProposta(statoProposta);
    }
});
