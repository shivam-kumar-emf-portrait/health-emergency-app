package com.lifeline.emergency.service;

import com.lifeline.emergency.dto.DoctorDto;
import com.lifeline.emergency.entity.Doctor;
import com.lifeline.emergency.entity.Hospital;
import com.lifeline.emergency.repository.DoctorRepository;
import com.lifeline.emergency.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final HospitalRepository hospitalRepository;

    public DoctorService(DoctorRepository doctorRepository, HospitalRepository hospitalRepository) {
        this.doctorRepository = doctorRepository;
        this.hospitalRepository = hospitalRepository;
    }

    public List<DoctorDto> getHospitalDoctors(Long hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public DoctorDto addDoctor(Long hospitalId, DoctorDto dto) {
        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Hospital not found: " + hospitalId));

        Doctor doctor = new Doctor(
                dto.getName(),
                dto.getSpecialization(),
                dto.getQualification(),
                dto.getExperienceYears(),
                dto.isOnDuty(),
                dto.isPublicProfile(),
                hospital
        );

        Doctor saved = doctorRepository.save(doctor);
        return toDto(saved);
    }

    @Transactional
    public DoctorDto toggleDoctorDuty(Long hospitalId, Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized doctor update.");
        }

        doctor.setOnDuty(!doctor.isOnDuty());
        Doctor saved = doctorRepository.save(doctor);
        return toDto(saved);
    }

    @Transactional
    public void deleteDoctor(Long hospitalId, Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));

        if (!doctor.getHospital().getId().equals(hospitalId)) {
            throw new SecurityException("Unauthorized doctor deletion.");
        }

        doctorRepository.delete(doctor);
    }

    private DoctorDto toDto(Doctor doctor) {
        DoctorDto dto = new DoctorDto();
        dto.setId(doctor.getId());
        dto.setName(doctor.getName());
        dto.setSpecialization(doctor.getSpecialization());
        dto.setQualification(doctor.getQualification());
        dto.setExperienceYears(doctor.getExperienceYears());
        dto.setOnDuty(doctor.isOnDuty());
        dto.setPublicProfile(doctor.isPublicProfile());
        return dto;
    }
}

