package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IPlanningDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
import java.util.List;

public class PlanningDAOImpl implements IPlanningDAO {
    private final ISoutenanceDAO soutenanceDAO = new SoutenanceDAOImpl();

    @Override
    public List<Soutenance> getDonneesPlanification() {
        return soutenanceDAO.findNonPlannifiees();
    }
}