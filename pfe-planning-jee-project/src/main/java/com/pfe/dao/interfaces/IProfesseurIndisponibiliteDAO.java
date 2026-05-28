package com.pfe.dao.interfaces;

import com.pfe.model.ProfesseurIndisponibilite;
import java.time.LocalDate;
import java.util.List;

public interface IProfesseurIndisponibiliteDAO {
    void save(ProfesseurIndisponibilite indisponibilite);
    ProfesseurIndisponibilite findById(Long id);
    List<ProfesseurIndisponibilite> findAll();
    List<ProfesseurIndisponibilite> findByProfesseurId(Long professeurId);
    List<ProfesseurIndisponibilite> findByProfesseurIdAndDate(Long professeurId, LocalDate date);
    void delete(Long id);
}
