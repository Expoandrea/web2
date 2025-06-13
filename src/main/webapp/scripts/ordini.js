function cambiaStatoOrdini(statoOrdine, elemento) {
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

    // Imposta il background
    elemento.style.backgroundColor = backgroundColor;

    // Aggiorna solo il colore, testo lo lasciamo invariato (contiene "Stato: ...")
}

document.addEventListener("DOMContentLoaded", function() {
    const ordiniStato = document.querySelectorAll('.card-row-content[data-state]');
    ordiniStato.forEach(ordine => {
        const statoOrdine = ordine.getAttribute('data-state');
        cambiaStatoOrdini(statoOrdine, ordine);
    });

    // Hover per il container principale (card-row-salm proposta-container)
    const ordineContainers = document.querySelectorAll(".card-row-salm.proposta-container");

    ordineContainers.forEach(container => {
        const stato = container.getAttribute("data-stato");

        if (stato === "IN_ATTESA") {
            container.style.backgroundColor = "#d2b4f5";
            container.addEventListener('mouseenter', () => {
                container.style.backgroundColor = "#b682f1";
            });
            container.addEventListener('mouseleave', () => {
                container.style.backgroundColor = "#d2b4f5";
            });
        } else {
            container.style.backgroundColor = "#e6d9fb";
            container.addEventListener('mouseenter', () => {
                container.style.backgroundColor = "#d2b4f5";
            });
            container.addEventListener('mouseleave', () => {
                container.style.backgroundColor = "#e6d9fb";
            });
        }
    });

    // Filtri ricerca e stato
    const searchInput = document.getElementById('search');
    const filterSelect = document.getElementById('status');
    const ord = document.querySelectorAll('.proposta-container');

    function filterOrdini() {
        const searchTerm = searchInput.value.toLowerCase();
        const selectedStatus = filterSelect.value;

        ord.forEach(o => {
            const codice = o.getAttribute('data-codice').toLowerCase();
            const stato = o.getAttribute('data-stato');

            const matchCodice = codice.includes(searchTerm);
            const matchStato = (selectedStatus === 'tutti' || stato === selectedStatus);

            if (matchCodice && matchStato) {
                o.parentElement.style.display = ''; // o è il div dentro <a>, nascondiamo/mostriamo il <a>
            } else {
                o.parentElement.style.display = 'none';
            }
        });
    }

    searchInput.addEventListener('input', filterOrdini);
    filterSelect.addEventListener('change', filterOrdini);
});
