/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoRichiesta;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.SplitSlashesFmkExt;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class RichiestaInAttesa extends BaseController{
     private void action_default( HttpServletRequest request, HttpServletResponse response, int n) throws IOException, ServletException, TemplateManagerException, DataException {
        try {
            RichiestaOrdine richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiestaOrdine(n);
            if (richiesta != null) {
                request.setAttribute("richiesta", richiesta);
                
                List<CaratteristicaRichiesta> caratteristiche = ((ApplicationDataLayer) request.getAttribute("datalayer")).getCaratteristicaRichiestaDAO().getCaratteristicaRichiestaByRichiesta(n);
                request.setAttribute("caratteristiche", caratteristiche);
                request.setAttribute("page_title", "Richiesta in attesa");
                
                TemplateResult res = new TemplateResult(getServletContext());
               
                request.setAttribute("strip_slashes", new SplitSlashesFmkExt());
                res.activate("richiesta_in_attesa.ftl.html", request, response);
            } else {
                handleError("Unable to load request", request, response);
            }
        } catch (DataException ex) {
            handleError("Data access exception: " + ex.getMessage(), request, response);
        }
    }
     
     
    private void action_prendiInCarico(HttpServletRequest request, HttpServletResponse response, int n, int tecnico_id) throws IOException, ServletException, DataException, TemplateManagerException {
        Utente u = ((ApplicationDataLayer) request.getAttribute("datalayer")).getUtenteDAO().getUtente(tecnico_id);
        
        RichiestaOrdine richiesta = ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().getRichiestaOrdine(n);
        richiesta.setStato(StatoRichiesta.PRESA_IN_CARICO);
        richiesta.setTecnico(u);
        
        ((ApplicationDataLayer) request.getAttribute("datalayer")).getRichiestaOrdineDAO().storeRichiestaOrdine(richiesta);
        
        String email = richiesta.getUtente().getEmail();
        String codice = richiesta.getCodiceRichiesta();
    
        try {
            Session session = EmailSender.createMailgunSession();
    
            String subject = "Richiesta presa in carico";
            String body = "Gentile utente,\n\n" +
                          "La informiamo che la richiesta numero " + codice + " è stata presa in carico.\n\n" +
                          "Cordiali saluti,\n" +
                          "Il team di WebMarket";
    
            EmailSender.sendEmail(session, email, subject, body);
        } catch (Exception e) {
            Logger.getLogger(RichiestaInAttesa.class.getName()).log(Level.SEVERE, null, e);
            request.setAttribute("error", "Errore durante l'invio dell'email di notifica.");
            action_default(request, response, n);
            return;
        }
    
        response.sendRedirect("notifiche_tecnico");
    }
    
     

    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {

        int n;
        try {
            HttpSession session = SecurityHelpers.checkSession(request);
        if (session == null) {
            
            response.sendRedirect("login");
            return;

        } 
         // Recupero l'ID dell'utente dalla sessione
         int userId = (int) session.getAttribute("userid");
         
         
            n = SecurityHelpers.checkNumeric(request.getParameter("n"));
            String action = request.getParameter("action");
            if (action != null && action.equals("prendiInCarico")) {
                action_prendiInCarico(request, response, n, userId);
            } else{
                action_default(request, response, n);
            }
        } catch (NumberFormatException ex) {
            handleError("Invalid number specified", request, response);
        } catch (IOException | TemplateManagerException ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
             Logger.getLogger(CategoriaController.class.getName()).log(Level.SEVERE, null, ex);
         }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Richieste in attesa servlet";
    }// </editor-fold>
}

