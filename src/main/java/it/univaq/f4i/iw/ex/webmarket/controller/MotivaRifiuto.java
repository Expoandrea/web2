package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MotivaRifiuto extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        PropostaAcquisto proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().getPropostaAcquisto(n);
        request.setAttribute("proposta", proposta);
        
        request.setAttribute("page_title", "Motivazione rifiuto ordine");
        res.activate("motiva_rifiuto.ftl.html", request, response);
    }
    
    
    private void action_rifiutaProp(HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        String motivazione = request.getParameter("note");
        if (motivazione == null || motivazione.trim().isEmpty()) {
            PropostaAcquisto proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().getPropostaAcquisto(n);
            request.setAttribute("proposta", proposta);
            request.setAttribute("errore", "La motivazione del rifiuto deve essere indicata!");
            action_default(request, response, n);
            return; 
        }
    
        PropostaAcquisto proposta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().getPropostaAcquisto(n);
        proposta.setStatoProposta(StatoProposta.RIFIUTATO);
        proposta.setMotivazione(motivazione);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().storePropostaAcquisto(proposta);
        
        RichiestaOrdine richiesta = proposta.getRichiestaOrdine();
        richiesta.setStato(StatoRichiesta.PRESA_IN_CARICO);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiestaOrdine(richiesta);
        
        String email = proposta.getRichiestaOrdine().getTecnico().getEmail();
        String username = proposta.getRichiestaOrdine().getTecnico().getUsername();
    
        try {
            // Creazione sessione Mailgun
            Session session = EmailSender.createMailgunSession();
    
            String subject = "Notifica Proposta Rifiutata";
            String body = "Ciao " + username + ",\n\n" +
                          "La informiamo che la sua proposta numero " + proposta.getCodice() + " è stata RIFIUTATA.\n" +
                          "La preghiamo gentilmente di compilare una nuova proposta.\n\n" +
                          "Saluti,\nIl team WebMarket";
    
            EmailSender.sendEmail(session, email, subject, body);
        } catch (Exception e) {
            // Log dell'errore e gestione eccezione
            Logger.getLogger(MotivaRifiuto.class.getName()).log(Level.SEVERE, null, e);
            request.setAttribute("error", "Errore durante l'invio dell'email di notifica.");
            action_default(request, response, n);
            return;
        }
    
        response.sendRedirect("dettaglio_proposta_ord?n=" + n);
    }
    
    
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
    try {
        HttpSession session = SecurityHelpers.checkSession(request);
        if (session == null) {
            response.sendRedirect("login");
            return;
        }
        
        int n = SecurityHelpers.checkNumeric(request.getParameter("n"));

        String action = request.getParameter("action");
        if (action != null && action.equals("rifiutaProposta")) {
            action_rifiutaProp(request, response, n);
        } else{
   
        action_default(request, response, n);
        }

    } catch (IOException | TemplateManagerException /* | DataException */ ex) {
        handleError(ex, request, response);
    }   catch (DataException ex) {
            Logger.getLogger(MotivaRifiuto.class.getName()).log(Level.SEVERE, null, ex);
        }
}


    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Motivazione rifiuto ordine servlet";
    }// </editor-fold>

}