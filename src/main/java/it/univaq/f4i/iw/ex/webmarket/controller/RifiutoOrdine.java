package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoProposta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;

import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;

public class RifiutoOrdine extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int n)
            throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdine(n);

        request.setAttribute("ordine", ordine);
        request.setAttribute("page_title", "Motivazione rifiuto ordine");
        res.activate("rifiuto_ordine.ftl.html", request, response);
    }

    private void action_rifiutaOrdine(HttpServletRequest request, HttpServletResponse response, int n)
            throws IOException, ServletException, TemplateManagerException, DataException {

        Ordine ordine = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                .getOrdineDAO().getOrdine(n);

        String motivoRifiuto = request.getParameter("azione");

        if (motivoRifiuto == null || motivoRifiuto.trim().isEmpty()) {
            request.setAttribute("ordine", ordine);
            request.setAttribute("errore", "Devi selezionare una motivazione per il rifiuto dell'ordine!");
            action_default(request, response, n);
            return;
        }

        // Set stato ordine
        if (motivoRifiuto.equals("RESPINTO_NON_CONFORME")) {
            ordine.setStato(StatoOrdine.RESPINTO_NON_CONFORME);
        } else if (motivoRifiuto.equals("RESPINTO_NON_FUNZIONANTE")) {
            ordine.setStato(StatoOrdine.RESPINTO_NON_FUNZIONANTE);
        }

        ((ApplicationDataLayer) request.getAttribute("datalayer")).getOrdineDAO().storeOrdine(ordine);

        // Stato proposta
        PropostaAcquisto proposta = ordine.getProposta();
        proposta.setStatoProposta(StatoProposta.ACCETTATO);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().storePropostaAcquisto(proposta);

        // Stato richiesta
        RichiestaOrdine richiesta = proposta.getRichiestaOrdine();
        richiesta.setStato(StatoRichiesta.IN_ATTESA);
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiestaOrdine(richiesta);

        // Invio email via Mailgun
        String email = richiesta.getTecnico().getEmail();
        String username = richiesta.getTecnico().getUsername();

        String subject = "Notifica Rifiuto Ordine";
        String text = "Ciao " + username + ",\n\n" +
                "La informiamo che l'ordine effettuato in data " + ordine.getData() + " è stato RIFIUTATO.\n" +
                "Motivo del rifiuto: " + motivoRifiuto.replace("_", " ").toLowerCase() + ".\n\n" +
                "La preghiamo gentilmente di procedere di conseguenza.\n\n" +
                "Saluti,\nIl team WebMarket";

        try {
            Session mailgunSession = EmailSender.createMailgunSession();  // Usa la tua classe helper
            EmailSender.sendEmail(mailgunSession, email, subject, text);
        } catch (Exception e) {
            request.setAttribute("errore", "Errore durante l'invio dell'email: " + e.getMessage());
            action_default(request, response, n);
            return;
        }

        response.sendRedirect("dettaglio_ordine_ord?n=" + n);
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

            if ("rifiutaOrdine".equals(action)) {
                action_rifiutaOrdine(request, response, n);
            } else {
                action_default(request, response, n);
            }

        } catch (IOException | TemplateManagerException | DataException ex) {
            handleError(ex, request, response);
        }
    }

    @Override
    public String getServletInfo() {
        return "Rifiuto Ordine Servlet";
    }
}
