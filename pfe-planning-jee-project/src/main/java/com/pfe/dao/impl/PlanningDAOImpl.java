package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IPlanningDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional(readOnly = true)
public class PlanningDAOImpl implements IPlanningDAO {
    
    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    @Override
    public List<Soutenance> getDonneesPlanification() {
        return soutenanceDAO.findNonPlannifiees();
    }
}