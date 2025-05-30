package it.univaq.f4i.iw.ex.webmarket.data.dao.impl;

import it.univaq.f4i.iw.ex.webmarket.data.dao.RichiestaOrdineDAO;
import it.univaq.f4i.iw.ex.webmarket.data.model.Categoria;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.ex.webmarket.data.model.Utente;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.StatoRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.impl.proxy.RichiestaOrdineProxy;
import it.univaq.f4i.iw.framework.data.DAO;
import it.univaq.f4i.iw.framework.data.DataException;
import it.univaq.f4i.iw.framework.data.DataItemProxy;
import it.univaq.f4i.iw.framework.data.DataLayer;
import it.univaq.f4i.iw.framework.data.OptimisticLockException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import java.sql.Statement;


public class RichiestaOrdineDAO_MySQL extends DAO implements RichiestaOrdineDAO {

    private PreparedStatement sRichiestaOrdineByID, sRichiesteByUtente, iRichiestaOrdine, uRichiestaOrdine, sRichiesteInoltrate, sRichiesteNonEvase, sRichiesteTecnico, sRichiesteRisolte, esisteRichiestaInAttesa;

    /**
     * Costruttore della classe.
     * 
     * @param d il DataLayer da utilizzare
     */
    public RichiestaOrdineDAO_MySQL(DataLayer d) {
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

            sRichiestaOrdineByID = connection.prepareStatement("SELECT * FROM richiesta_ordine WHERE ID = ?");
            sRichiesteByUtente = connection.prepareStatement("SELECT * FROM richiesta_ordine WHERE utente = ? ORDER BY data DESC");
            sRichiesteInoltrate = connection.prepareStatement("SELECT * FROM richiesta_ordine WHERE stato = ?");
            sRichiesteNonEvase = connection.prepareStatement(
                "SELECT r.ID, r.note, r.stato, r.data, r.codice_richiesta, r.utente, r.tecnico, r.categoria_id " +
                "FROM richiesta_ordine r " +
                "WHERE r.stato = ? AND r.tecnico = ? " +
                "AND NOT EXISTS (SELECT 1 FROM proposta_acquisto p WHERE p.richiesta_id = r.ID AND (p.stato = 'ACCETTATO' or p.stato = 'IN_ATTESA')) ORDER BY data ASC"
            );
            sRichiesteTecnico = connection.prepareStatement("SELECT * FROM richiesta_ordine WHERE tecnico_id = ?");
            sRichiesteRisolte = connection.prepareStatement("SELECT * FROM richiesta_ordine WHERE stato = ?");
            iRichiestaOrdine = connection.prepareStatement("INSERT INTO richiesta_ordine (note, stato, data, utente, categoria_id) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            uRichiestaOrdine = connection.prepareStatement("UPDATE richiesta_ordine SET note=?, stato=?, data=?, codice_richiesta=?, utente=?, tecnico=?, categoria_id=?, version=? WHERE ID=? AND version=?");
            esisteRichiestaInAttesa = connection.prepareStatement("SELECT EXISTS( SELECT 1 FROM richiesta_ordine WHERE stato = 'IN_ATTESA') AS esiste_richiesta_in_attesa;");
        } catch (SQLException ex) {
            throw new DataException("Error initializing RichiestaOrdine data layer", ex);
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
            sRichiestaOrdineByID.close();
            sRichiesteByUtente.close();
            sRichiesteInoltrate.close();
            sRichiesteNonEvase.close();
            sRichiesteTecnico.close();
            sRichiesteRisolte.close();
            iRichiestaOrdine.close();
            uRichiestaOrdine.close();
        } catch (SQLException ex) {
            
        }
        super.destroy();
    }

    /**
     * Crea una nuova istanza di RichiestaOrdine.
     * 
     * @return una nuova istanza di RichiestaOrdineProxy
     */
    @Override
    public RichiestaOrdine createRichiestaOrdine() {
        return new RichiestaOrdineProxy(getDataLayer());
    }

    /**
     * Crea una RichiestaOrdineProxy a partire da un ResultSet.
     * 
     * @param rs il ResultSet da cui creare la RichiestaOrdineProxy
     * @return una nuova istanza di RichiestaOrdineProxy
     * @throws DataException se si verifica un errore durante la creazione
     */
    private RichiestaOrdineProxy createRichiestaOrdine(ResultSet rs) throws DataException {
        try {
            RichiestaOrdineProxy richiesta = (RichiestaOrdineProxy) createRichiestaOrdine();
            richiesta.setKey(rs.getInt("ID"));
            richiesta.setNote(rs.getString("note"));
            richiesta.setStato(StatoRichiesta.valueOf(rs.getString("stato")));
            richiesta.setData(rs.getDate("data"));
            richiesta.setCodiceRichiesta(rs.getString("codice_richiesta"));
            richiesta.setVersion(rs.getLong("version"));
            
            int tecnicoId = rs.getInt("tecnico");
            Utente tecnico = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(tecnicoId);
            richiesta.setTecnico(tecnico);
            
            int categoriaId = rs.getInt("categoria_id");
            Categoria categoria = ((ApplicationDataLayer) getDataLayer()).getCategoriaDAO().getCategoria(categoriaId);
            richiesta.setCategoria(categoria);

            int utenteId = rs.getInt("utente");
            Utente utente = ((ApplicationDataLayer) getDataLayer()).getUtenteDAO().getUtente(utenteId);
            richiesta.setUtente(utente);

            return richiesta;
        } catch (SQLException ex) {
            throw new DataException("Unable to create RichiestaOrdine object from ResultSet", ex);
        }
    }

    /**
     * Recupera una richiesta dato il suo ID.
     * 
     * @param richiesta_key l'ID della richiesta
     * @return la richiesta corrispondente all'ID
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public RichiestaOrdine getRichiestaOrdine(int richiesta_key) throws DataException {
        RichiestaOrdine richiesta = null;
        if (dataLayer.getCache().has(RichiestaOrdine.class, richiesta_key)) {
            richiesta = dataLayer.getCache().get(RichiestaOrdine.class, richiesta_key);
        } else {
            try {
                sRichiestaOrdineByID.setInt(1, richiesta_key);
                try (ResultSet rs = sRichiestaOrdineByID.executeQuery()) {
                    if (rs.next()) {
                        richiesta = createRichiestaOrdine(rs);
                        dataLayer.getCache().add(RichiestaOrdine.class, richiesta);
                    }
                }
            } catch (SQLException ex) {
                throw new DataException("Unable to load RichiestaOrdine by ID", ex);
            }
        }
        return richiesta;
    }

    /**
     * Recupera le richieste associate a un utente.
     * 
     * @param utente_key l'ID dell'utente
     * @return una lista di richieste associate all'utente
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<RichiestaOrdine> getRichiesteByUtente(int utente_key) throws DataException {
        List<RichiestaOrdine> result = new ArrayList<>();
        try {
            sRichiesteByUtente.setInt(1, utente_key);
            try (ResultSet rs = sRichiesteByUtente.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiestaOrdine(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine by Utente", ex);
        }
        return result;
    }

    /**
     * Memorizza una richiesta nel database.
     * 
     * @param richiesta la richiesta da memorizzare
     * @throws DataException se si verifica un errore durante la memorizzazione
     */
    @Override
    public void storeRichiestaOrdine(RichiestaOrdine richiesta) throws DataException {
        try {
            if (richiesta.getKey() != null && richiesta.getKey() > 0) {
                if (richiesta instanceof RichiestaOrdineProxy && !((RichiestaOrdineProxy) richiesta).isModified()) {
                    return;
                }
                uRichiestaOrdine.setString(1, richiesta.getNote());
                uRichiestaOrdine.setString(2, richiesta.getStato().name());
                uRichiestaOrdine.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                uRichiestaOrdine.setString(4, richiesta.getCodiceRichiesta());
                uRichiestaOrdine.setInt(5, richiesta.getUtente().getKey());
                uRichiestaOrdine.setInt(6, richiesta.getTecnico().getKey());
                uRichiestaOrdine.setInt(7, richiesta.getCategoria().getKey());
                long oldVersion = richiesta.getVersion();
                long versione = oldVersion + 1;
                uRichiestaOrdine.setLong(8, versione);
                uRichiestaOrdine.setInt(9, richiesta.getKey());
                uRichiestaOrdine.setLong(10, oldVersion);
                if(uRichiestaOrdine.executeUpdate() == 0){
                    throw new OptimisticLockException(richiesta);
                }else {
                    richiesta.setVersion(versione);
                }
            } else {
                iRichiestaOrdine.setString(1, richiesta.getNote());
                iRichiestaOrdine.setString(2, richiesta.getStato().name());
                iRichiestaOrdine.setDate(3, new java.sql.Date(richiesta.getData().getTime()));
                iRichiestaOrdine.setInt(4, richiesta.getUtente().getKey());
                iRichiestaOrdine.setInt(5, richiesta.getCategoria().getKey());

                if (iRichiestaOrdine.executeUpdate() == 1) {
                    try (ResultSet keys = iRichiestaOrdine.getGeneratedKeys()) {
                        if (keys.next()) {
                            int key = keys.getInt(1);
                            richiesta.setKey(key);
                            dataLayer.getCache().add(RichiestaOrdine.class, richiesta);
                        }
                    }
                }
            }
            if (richiesta instanceof DataItemProxy) {
                ((DataItemProxy) richiesta).setModified(false);
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to store RichiestaOrdine", ex);
        }
    }

    /**
     * Recupera le richieste inoltrate.
     * 
     * @return una lista di richieste inoltrate
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<RichiestaOrdine> getRichiesteInoltrate() throws DataException {
        List<RichiestaOrdine> result = new ArrayList<>();
        try {
            sRichiesteInoltrate.setString(1, StatoRichiesta.IN_ATTESA.name());
            try (ResultSet rs = sRichiesteInoltrate.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiestaOrdine(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine Inoltrate", ex);
        }
        return result;
    }

    /**
     * Recupera le richieste non evase per un tecnico specifico.
     * 
     * @param tecnico_key l'ID del tecnico
     * @return una lista di richieste non evase
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<RichiestaOrdine> getRichiesteNonEvase(int tecnico_key) throws DataException {
        List<RichiestaOrdine> result = new ArrayList<>();
        try {
            sRichiesteNonEvase.setString(1, StatoRichiesta.PRESA_IN_CARICO.name());
            sRichiesteNonEvase.setInt(2, tecnico_key);
            try (ResultSet rs = sRichiesteNonEvase.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiestaOrdine(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine non evase", ex);
        }
        return result;
    }

    /**
     * Recupera le richieste associate a un tecnico specifico.
     * 
     * @param tecnico_key l'ID del tecnico
     * @return una lista di richieste associate al tecnico
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<RichiestaOrdine> getRichiesteTecnico(int tecnico_key) throws DataException {
        List<RichiestaOrdine> result = new ArrayList<>();
        try {
            sRichiesteTecnico.setInt(1, tecnico_key);
            try (ResultSet rs = sRichiesteTecnico.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiestaOrdine(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine by Tecnico", ex);
        }
        return result;
    }

    /**
     * Recupera le richieste risolte.
     * 
     * @return una lista di richieste risolte
     * @throws DataException se si verifica un errore durante il recupero
     */
    @Override
    public List<RichiestaOrdine> getRichiesteRisolte() throws DataException {
        List<RichiestaOrdine> result = new ArrayList<>();
        try {
            sRichiesteRisolte.setString(1, StatoRichiesta.RISOLTA.name());
            try (ResultSet rs = sRichiesteRisolte.executeQuery()) {
                while (rs.next()) {
                    result.add(getRichiestaOrdine(rs.getInt("ID")));
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to load RichiesteOrdine Risolte", ex);
        }
        return result;
    }

    /**
     * Invia una richiesta impostando il suo stato a "IN_ATTESA".
     * 
     * @param richiestaOrdine la richiesta da inviare
     * @throws DataException se si verifica un errore durante l'invio
     */
    @Override
    public void inviaRichiestaOrdine(RichiestaOrdine richiestaOrdine) throws DataException {
        if (richiestaOrdine != null && richiestaOrdine.getKey() != null) {
            richiestaOrdine.setStato(StatoRichiesta.IN_ATTESA);
            storeRichiestaOrdine(richiestaOrdine);
        } else {
            throw new DataException("Invalid RichiestaOrdine object or missing key.");
        }
    }

    /**
     * Elimina una richiesta dal database.
     * 
     * @param richiesta_key l'ID della richiesta da eliminare
     * @throws DataException se si verifica un errore durante l'eliminazione
     */
    @Override
    public void deleteRichiestaOrdine(int richiesta_key) throws DataException {
      try {
        PreparedStatement dRichiestaOrdine = connection.prepareStatement("DELETE FROM richiesta_ordine WHERE ID=?");
        dRichiestaOrdine.setInt(1, richiesta_key);
        dRichiestaOrdine.executeUpdate();
        dataLayer.getCache().delete(RichiestaOrdine.class, richiesta_key); 
        dRichiestaOrdine.close();
    } catch (SQLException ex) {
        throw new DataException("Unable to delete RichiestaOrdine", ex);
    }
}
    
    /**
     * Verifica se esiste una richiesta in stato "IN_ATTESA".
     * 
     * @return true se esiste almeno una richiesta in stato "IN_ATTESA", false altrimenti.
     * @throws DataException se si verifica un errore durante la verifica.
     */
    @Override
    public boolean esisteRichiestaInAttesa() throws DataException {
        try {
            try (ResultSet rs = esisteRichiestaInAttesa.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("esiste_richiesta_in_attesa");
                }
            }
        } catch (SQLException ex) {
            throw new DataException("Unable to check if there are requests in 'IN_ATTESA' state", ex);
        }
        return false;
    }

}
