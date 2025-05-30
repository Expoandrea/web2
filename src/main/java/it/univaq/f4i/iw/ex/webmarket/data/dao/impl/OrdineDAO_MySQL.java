package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;
import it.univaq.f4i.iw.ex.webmarket.data.dao.OrdineDAO;
import it.univaq.f4i.iw.ex.webmarket.data.dao.PropostaAcquistoDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Ordine;
import it.univaq.f4i.iw.ex.webmarket.data.model.PropostaAcquisto;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.OrdineProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrdineDAO_MySQL extends DAO implements OrdineDAO {
    
    private PreparedStatement sOrdineByID, sOrdiniByUtente, sOrdiniByTecnico, sAllOrdini, iOrdine, uOrdine, dOrdine, ordiniDaNotificare, ordiniDaNotificareOrd;

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public OrdineDAO_MySQL(DataLayer d) {
        super(d);
    }

    /**
     * Inizializza le PreparedStatement.
     * 
     * @throws DataException se si verifica un errore durante l'inizializzazione
     */
    @Override
    public void init() throws DataException {
        try {
            super.init();
            sOrdineByID = connection.prepareStatement("SELECT * FROM ordine WHERE ID = ?");
            sOrdiniByUtente = connection.prepareStatement("SELECT o.* FROM ordine o JOIN proposta_acquisto pa ON o.proposta_id = pa.ID JOIN richiesta_ordine ro ON pa.richiesta_id = ro.ID WHERE ro.utente = ?  ORDER BY CASE WHEN o.stato = 'IN_ATTESA' THEN 1 ELSE 2 END, o.data DESC");
            sOrdiniByTecnico = connection.prepareStatement("SELECT o.* FROM ordine o JOIN proposta_acquisto pa ON o.proposta_id = pa.ID JOIN richiesta_ordine ro ON pa.richiesta_id = ro.ID WHERE ro.tecnico = ? ORDER BY CASE WHEN (o.stato = 'RESPINTO_NON_CONFORME' OR o.stato = 'RESPINTO_NON_FUNZIONANTE') THEN 1 ELSE 2 END, o.data DESC");
            sAllOrdini = connection.prepareStatement("SELECT * FROM ordine");
            iOrdine = connection.prepareStatement("INSERT INTO ordine (stato, proposta_id, data) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uOrdine = connection.prepareStatement("UPDATE ordine SET stato=?, proposta_id=? , data=?, version=? WHERE ID=? AND version=?");
            dOrdine = connection.prepareStatement("DELETE FROM ordine WHERE ID=?");
            ordiniDaNotificare = connection.prepareStatement( "SELECT EXISTS( SELECT 1 FROM ordine o JOIN proposta_acquisto pa ON o.proposta_id = pa.ID JOIN richiesta_ordine ro ON pa.richiesta_id = ro.ID WHERE (o.stato = 'RESPINTO_NON_CONFORME' OR o.stato = 'RESPINTO_NON_FUNZIONANTE') AND ro.tecnico = ?) AS notifica_ordine;");
            ordiniDaNotificareOrd = connection.prepareStatement( "SELECT EXISTS( SELECT 1 FROM ordine o JOIN proposta_acquisto pa ON o.proposta_id = pa.ID JOIN richiesta_ordine ro ON pa.richiesta_id = ro.ID WHERE (o.stato = 'IN_ATTESA') AND ro.utente = ?) AS notifica_ordine;");

        } catch (SQLException ex) {
            throw new DataException("Error initializing ordine data layer", ex);
        }
    }

    /**
     * Chiude le PreparedStatement.
     * 
     * @throws DataException se si verifica un errore durante la chiusura
     */
    @Override
    public void destroy() throws DataException {
        try {
            sOrdineByID.close();
            sOrdiniByUtente.close();
            sAllOrdini.close();
            iOrdine.close();
            uOrdine.close();
            dOrdine.close();
        } catch (SQLException ex) {
            
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di Ordine.
     * 
     * @return una nuova istanza di OrdineProxy
     */
    @Override
    public Ordine createOrdine() {
        return new OrdineProxy(getDataLayer());
    }

    /**
     * Crea una OrdineProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la OrdineProxy
     * @return una nuova istanza di OrdineProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private OrdineProxy createOrdine(ResultSet rs) throws DataException {
        try {
            OrdineProxy o = (OrdineProxy) createOrdine();
             int id = rs.getInt("ID");
             o.setKey(id);
             o.setStato(StatoOrdine.valueOf(rs.getString("stato")));
              PropostaAcquistoDAO propostaAcquistoDAO = (PropostaAcquistoDAO) dataLayer.getDAO(PropostaAcquisto.class);
             o.setProposta(propostaAcquistoDAO.getPropostaAcquisto(rs.getInt("proposta_id")));
             o.setData(rs.getDate("data"));
             o.setVersion(rs.getLong("version"));
            return o;
        } catch (SQLException ex) {
            throw new DataException("Unable to create ordine object from ResultSet", ex);
        }
    }

    /**
     * Recupera un ordine dato il suo ID.
     * 
     * @param ordine_key l'ID dell'ordine
     * @return l'ordine corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public Ordine getOrdine(int ordine_key) throws DataException {
        Ordine o = null;
        if (dataLayer.getCache().has(Ordine.class, ordine_key)) {
            o = dataLayer.getCache().get(Ordine.class, ordine_key);
        } else {
            try {
                sOrdineByID.setInt(1, ordine_key);
                try (ResultSet rs = sOrdineByID.executeQuery()) {
                    if (rs.next()) {
                        o = createOrdine(rs);
                        dataLayer.getCache().add(Ordine.class, o);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load ordine by ID", ex);
            }
        }
        return o;
    }

    /**
     * Recupera gli ordini associati a un utente.
     * 
     * @param utente_key l'ID dell'utente
     * @return una lista di ordini associati all'utente
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Ordine> getOrdiniByUtente(int utente_key) throws DataException {
        List<Ordine> ordini = new ArrayList<>();
        try {
            sOrdiniByUtente.setInt(1, utente_key);
            try (ResultSet rs = sOrdiniByUtente.executeQuery()) {
                while (rs.next()) {
                    ordini.add(createOrdine(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load ordini by utente ID", ex);
        }
        return ordini;
    }

    /**
     * Recupera gli ordini associati a un tecnico.
     * 
     * @param tecnico_key l'ID del tecnico
     * @return una lista di ordini associati al tecnico
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Ordine> getOrdiniByTecnico(int tecnico_key) throws DataException {
        List<Ordine> ordini = new ArrayList<>();
        try {
            sOrdiniByTecnico.setInt(1, tecnico_key);
            try (ResultSet rs = sOrdiniByTecnico.executeQuery()) {
                while (rs.next()) {
                    ordini.add(createOrdine(rs));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load ordini by tecnico ID", ex);
        }
        return ordini;
    }

    /**
     * Recupera tutti gli ordini.
     * 
     * @return una lista di tutti gli ordini
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<Ordine> getAllOrdini() throws DataException {
        List<Ordine> ordini = new ArrayList<>();
        try (ResultSet rs = sAllOrdini.executeQuery()) {
            while (rs.next()) {
                ordini.add(createOrdine(rs));
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load all ordini", ex);
        }
        return ordini;
    }

    /**
     * Memorizza un ordine nel database.
     * 
     * @param ordine l'ordine da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeOrdine(Ordine ordine) throws DataException {
        try {
            if (ordine.getKey() != null && ordine.getKey() > 0) {
                 // Se l'ordine è un proxy e non è stato modificato, salta l'aggiornamento
                if (ordine instanceof OrdineProxy && !((OrdineProxy) ordine).isModified()) {
                    return;
                }
               

                uOrdine.setString(1, ordine.getStato().toString());
                uOrdine.setInt(2, ordine.getProposta().getKey());
                uOrdine.setDate(3, new java.sql.Date(ordine.getData().getTime()));
                long oldVersion = ordine.getVersion();
                long versione = oldVersion + 1;
                uOrdine.setLong(4, versione);
                uOrdine.setInt(5, ordine.getKey());
                uOrdine.setLong(6, oldVersion);
                if(uOrdine.executeUpdate() == 0){
                    throw new OptimisticLockException(ordine);
                }else {
                    ordine.setVersion(versione);
                }
            } else {
                // Inserisce un nuovo ordine nel database
                iOrdine.setString(1, ordine.getStato().toString());
                iOrdine.setInt(2, ordine.getProposta().getKey());
                iOrdine.setDate(3, new java.sql.Date(ordine.getData().getTime()));
                if (iOrdine.executeUpdate() == 1) {
                    try (ResultSet keys = iOrdine.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            ordine.setKey(key);
                            dataLayer.getCache().add(Ordine.class, ordine);
                        }
                    }
                }
            }
            if (ordine instanceof DataItemProxy) {
                ((DataItemProxy) ordine).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store ordine", ex);
        }
    }

    /**
     * Elimina un ordine dal database.
     * 
     * @param ordine_key l'ID dell'ordine da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteOrdine(int ordine_key) throws DataException {
        try {
            dOrdine.setInt(1, ordine_key);
            int rowsAffected = dOrdine.executeUpdate();
            if (rowsAffected == 0) {
                throw new DataException("No ordine found with the given ID.");
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to delete ordine", ex);
        }
    }
    
    /**
     * Verifica se ci sono ordini da notificare ad un tecnico.
     * 
     * @param tecnicoId l'ID del tecnico
     * @return true se ci sono ordini da notificare, false altrimenti
     * @throws DataException se si verifica un errore durante la verifica
     */
     @Override
    public boolean notificaOrdine(int tecnicoId) throws DataException {

        try {
            ordiniDaNotificare.setInt(1, tecnicoId);
            try (ResultSet rs = ordiniDaNotificare.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_ordine");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Non sia riusciti a controllare se ci sono ordini respinti", ex);
        }
        return false;
    }
    
    /**
     * Verifica se ci sono ordini da notificare per un utente.
     * 
     * @param utenteId l'ID dell'utente
     * @return true se ci sono ordini da notificare, false altrimenti
     * @throws DataException se si verifica un errore durante la verifica
     */
     @Override
    public boolean notificaOrdineOrd(int utenteId) throws DataException {

        try {
            ordiniDaNotificareOrd.setInt(1, utenteId);
            try (ResultSet rs = ordiniDaNotificareOrd.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("notifica_ordine");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Non sia riusciti a controllare se ci sono ordini respinti", ex);
        }
        return false;
    }
}