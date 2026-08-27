package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDao;
import com.sunrisedental.dto.response.ApiResponse;
import com.sunrisedental.exception.AuthenticationException;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.service.UserService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/doctor/*")
public class DoctorController extends BaseController {
    private final UserService userService;
    private final AppointmentDao appointmentDao;

    public DoctorController() {
        this.userService = new UserService();
        this.appointmentDao = new AppointmentDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isAuthenticated(request)) {
            handleException(request, response, new AuthenticationException("User not authenticated"));
            return;
        }

        if (!hasRole(request, "DENTIST")) {
            handleException(request, response, new AuthenticationException("Doctor access required"));
            return;
        }

        String pathInfo = getPathInfo(request);

        try {
            switch (pathInfo) {
                case "appointments":
                    handleGetDoctorAppointments(request, response);
                    break;

                case "revenue":
                    handleGetDoctorRevenue(request, response);
                    break;

                case "patients":
                    handleGetDoctorPatients(request, response);
                    break;

                default:
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
                    break;
            }
        } catch (Exception e) {
            handleException(request, response, e);
        }
    }

    private void handleGetDoctorAppointments(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer userId = getCurrentUserId(request);
        Integer dentistId = userService.getDentistIdByUserId(userId);

        if (dentistId == null) {
            throw new AuthenticationException("Doctor mapping not found");
        }

        List<Appointment> appointments = appointmentDao.findByDentistId(dentistId);

        sendSuccess(response, "Doctor appointments retrieved successfully", appointments);
    }

    private void handleGetDoctorRevenue(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer userId = getCurrentUserId(request);
        Integer dentistId = userService.getDentistIdByUserId(userId);

        if (dentistId == null) {
            throw new AuthenticationException("Doctor mapping not found");
        }

        BigDecimal completedRevenue = appointmentDao.getCompletedRevenueByDentist(dentistId);
        BigDecimal upcomingRevenue = appointmentDao.getUpcomingRevenueByDentist(dentistId);
        BigDecimal totalRevenue = completedRevenue.add(upcomingRevenue);

        Map<String, Object> revenueData = new HashMap<>();
        revenueData.put("dentistId", dentistId);
        revenueData.put("completedRevenue", completedRevenue);
        revenueData.put("upcomingRevenue", upcomingRevenue);
        revenueData.put("totalRevenue", totalRevenue);

        sendSuccess(response, "Doctor revenue retrieved successfully", revenueData);
    }

    private void handleGetDoctorPatients(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Integer userId = getCurrentUserId(request);
        Integer dentistId = userService.getDentistIdByUserId(userId);

        if (dentistId == null) {
            throw new AuthenticationException("Doctor mapping not found");
        }

        List<Map<String, Object>> patients = appointmentDao.findPatientsByDentistId(dentistId);

        sendSuccess(response, "Doctor patients retrieved successfully", patients);
    }
}