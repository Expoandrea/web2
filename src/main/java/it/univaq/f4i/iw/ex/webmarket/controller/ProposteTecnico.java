package it.univaq.f4i.iw.ex.webmarket.controller;

import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ProposteTecnico extends BaseController {

    private void action_default(HttpServletRequest request, HttpServletResponse response, int user) throws IOException, ServletException, TemplateManagerException, DataException {
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Proposte Tecnico");

        // Proposte inviate dal tecnico loggato
        request.setAttribute("proposte", ((ApplicationDataLayer) request.getAttribute("datalayer")).getPropostaAcquistoDAO().getProposteByTecnico(user));

        res.activate("proposte_tecnico.ftl.html", request, response);
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

        // Recupero l'ID del tecnico dalla sessione
        int tecnicoId = (int) session.getAttribute("userid");
        
        //ho aggiunto id perchè dobbiamo filtrare le proposte che ha inviato il tecnico loggato
        action_default(request, response, tecnicoId);

    } catch (IOException | TemplateManagerException ex) {
        handleError(ex, request, response);
    }    catch (DataException ex) {
            Logger.getLogger(ProposteTecnico.class.getName()).log(Level.SEVERE, null, ex);
        }
}


    // Descrizione della servlet
    @Override
    public String getServletInfo() {
        return "Servlet per la gestione delle proposte inviate dal tecnico";
    }

}