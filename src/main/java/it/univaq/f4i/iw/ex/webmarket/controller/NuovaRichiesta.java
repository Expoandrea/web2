package it.univaq.f4i.iw.ex.webmarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.univaq.f4i.iw.ex.webmarket.data.dao.impl.ApplicationDataLayer;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.framework.result.TemplateResult;
import it.univaq.f4i.iw.framework.security.SecurityHelpers;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.result.TemplateManagerException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NuovaRichiesta extends BaseController {

    // Metodo per gestire l'azione di default
    private void action_default(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, TemplateManagerException, DataException {
        //prendo le categorie generate da root
        request.setAttribute("categorie", ((ApplicationDataLayer) request.getAttribute("datalayer")).getCategoriaDAO().getCategorieRoot());

        
        TemplateResult res = new TemplateResult(getServletContext());
        request.setAttribute("page_title", "Nuova Richiesta Ordine");
        res.activate("nuova_richiesta.ftl.html", request, response);
    }
    
    //fetching delle subcategorie
    private void fetch_subcategories(HttpServletRequest request, HttpServletResponse response, int n) throws DataException{
        request.setAttribute("subcategorie", ((ApplicationDataLayer) request.getAttribute("datalayer")).getCategoriaDAO().getCategorieByPadre(n));

    }
    

    // Metodo per processare le richieste
    @Override
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            HttpSession session = SecurityHelpers.checkSession(request);
            if (session == null) {
                response.sendRedirect("login");
                return;
            }
            
            
            String nParam = request.getParameter("n");
        if (nParam != null) {
            int n = SecurityHelpers.checkNumeric(nParam);
            if (n != 0) {
                fetch_subcategories(request, response, n);
                List<Categoria> subCategories = ((ApplicationDataLayer) request.getAttribute("datalayer"))
                    .getCategoriaDAO().getCategorieByPadre(n);
                
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                ObjectMapper objectMapper = new ObjectMapper();
                response.getWriter().write(objectMapper.writeValueAsString(subCategories));                
                return;
            }
        }
        
        String action = request.getParameter("action");
        if (action != null && action.equals("avanti")) {
            int category = Integer.parseInt(request.getParameter("chosen-category"));
            System.out.println("chosen: " + category);
            request.setAttribute("chosen", category);
            String redirectURL = "nuova_richiesta_caratteristiche?n=" + category;
            response.sendRedirect(redirectURL);
            return;
        } else {
            action_default(request, response);
        }             
    
        } catch (IOException | TemplateManagerException /* | DataException */ ex) {
            handleError(ex, request, response);
        } catch (DataException ex) {
            Logger.getLogger(NuovaRichiesta.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    @Override
    public String getServletInfo() {
        return "Controller per la pagina Nuova Richiesta Ordine";
    }
}
