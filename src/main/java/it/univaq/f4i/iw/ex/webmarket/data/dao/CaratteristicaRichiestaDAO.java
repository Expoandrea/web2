package it.univaq.f4i.iw.ex.webmarket.data.dao;

import it.univaq.f4i.iw.ex.webmarket.data.model.Caratteristica;
import it.univaq.f4i.iw.ex.webmarket.data.model.CaratteristicaRichiesta;
import it.univaq.f4i.iw.ex.webmarket.data.model.RichiestaOrdine;
import it.univaq.f4i.iw.framework.data.DataException;
import java.util.List;

public interface CaratteristicaRichiestaDAO {
    List<CaratteristicaRichiesta> getCaratteristicaRichiestaByRichiesta(int richiesta_key) throws DataException;


    CaratteristicaRichiesta createCaratteristicaRichiesta();

    // CaratteristicaRichiesta getCaratteristicaRichiesta(int caratteristica_key, int richiesta_key) throws DataException;
    
    CaratteristicaRichiesta getCaratteristicaRichiesta(int cr_key) throws DataException;

    List<Caratteristica> getCaratteristicheByRichiesta(int richiesta_key) throws DataException;

    List<RichiestaOrdine> getRichiesteByCaratteristica(int caratteristica_key) throws DataException;

    void storeCaratteristicaRichiesta(CaratteristicaRichiesta caratteristica) throws DataException;

}