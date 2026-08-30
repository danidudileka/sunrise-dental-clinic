package com.sunrisedental.controller;

import com.sunrisedental.dao.DentistDao;
import com.sunrisedental.dao.TreatmentDao;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Treatment;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller for fetching reference data (dentists and treatments).
 */
@WebServlet("/api/data/*")
public class DataController extends BaseController {
    private final DentistDao dentistDao;
    private final TreatmentDao treatmentDao;

    public DataController() {
        this.dentistDao = new DentistDao();
        this.treatmentDao = new TreatmentDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "dentists":
                    List<Dentist> dentists = dentistDao.findAllActive();
                    sendSuccess(response, "Dentists retrieved successfully", dentists);
                    break;

                case "treatments":
                    List<Treatment> treatments = treatmentDao.findAllActive();
                    sendSuccess(response, "Treatments retrieved successfully", treatments);
                    break;

                default:
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }
}